package com.snowplowanalytics.snowplow.tracker.emitter;

import com.google.common.collect.Lists;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class EmitterTest {

    private HttpClientAdapter httpClientAdapter;

    private BatchEmitter emitter;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        httpClientAdapter = mock(HttpClientAdapter.class);

        emitter = spy(new BatchEmitter(httpClientAdapter));
    }

    @Test
    public void addToBuffer_withLess10Payloads_shouldNotFlushBuffer() throws Exception {
        // Given
        ArgumentCaptor<Payload> argumentCaptor = ArgumentCaptor.forClass(Payload.class);

        List<Payload> payloads = createPayloads(2);

        // When
        for (Payload payload : payloads) {
            emitter.emit(payload);
        }

        // Then
        verify(emitter, never()).flushBuffer();
        verify(httpClientAdapter, never()).get(argumentCaptor.capture());

        Assert.assertEquals(2, emitter.getBuffer().size());
        Assert.assertEquals(payloads, emitter.getBuffer());
    }

    @Test
    public void addToBuffer_withMore10Payloads_shouldFlushBuffer() throws Exception {

        // Given
        ArgumentCaptor<SchemaPayload> argumentCaptor = ArgumentCaptor.forClass(SchemaPayload.class);
        when(httpClientAdapter.post(any(SchemaPayload.class))).thenReturn(200);

        List<Payload> payloads = createPayloads(11);

        // When
        for (Payload payload : payloads) {
            emitter.emit(payload);
        }

        // Then
        verify(emitter).flushBuffer();
        verify(httpClientAdapter).post(argumentCaptor.capture());
        Assert.assertEquals(payloads.subList(0, payloads.size() - 1), argumentCaptor.getValue().getData());

        Payload lastPayload = payloads.get(payloads.size() - 1);
        Assert.assertTrue(emitter.getBuffer().size() == 1);
        Assert.assertEquals(lastPayload, emitter.getBuffer().get(0));
    }

    @Test
    public void addToBuffer_withNot200ResponseStatus_shouldThrowRuntimeException() {

        // Given
        when(httpClientAdapter.post(any(SchemaPayload.class))).thenReturn(500);
        expectedException.expectMessage("Failed to emit event");

        List<Payload> payloads = createPayloads(11);

        // When
        for (Payload payload : payloads) {
            emitter.emit(payload);
        }
    }

    private List<Payload> createPayloads(int nbPayload) {
        final List<Payload> payloads = Lists.newArrayList();
        for (int i = 0; i < nbPayload; i++) {
            payloads.add(createPayload());
        }
        return payloads;
    }

    private Payload createPayload() {
        Payload payload = new Payload();
        payload.put("id", UUID.randomUUID());
        return payload;
    }

    private static class Payload extends HashMap<String,Object> { }
}
