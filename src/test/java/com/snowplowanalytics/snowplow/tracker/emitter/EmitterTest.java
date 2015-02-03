package com.snowplowanalytics.snowplow.tracker.emitter;

import com.google.common.collect.Lists;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class EmitterTest {

    private HttpClientAdapter httpClientAdapter;

    @Before
    public void setUp() throws Exception {
        httpClientAdapter = mock(HttpClientAdapter.class);
    }

    @Test
    public void addToBuffer_withGetMethod_withLess10Payloads_shouldNotFlushBuffer() throws Exception {
        // Given
        Emitter emitter = createEmitter(HttpMethod.GET);
        ArgumentCaptor<Payload> argumentCaptor = ArgumentCaptor.forClass(Payload.class);

        List<Payload> payloads = createPayloads(2);

        // When
        for (Payload payload : payloads) {
            emitter.addToBuffer(payload);
        }

        // Then
        verify(emitter, never()).flushBuffer();
        verify(httpClientAdapter, never()).get(argumentCaptor.capture());

        Assert.assertEquals(2, emitter.getBuffer().size());
        Assert.assertEquals(payloads, emitter.getBuffer());
    }

    @Test
    public void addToBuffer_withGetMethod_withMore10Payloads_shouldFlushBuffer() throws Exception {
        // Given
        Emitter emitter = createEmitter(HttpMethod.GET);
        ArgumentCaptor<Payload> argumentCaptor = ArgumentCaptor.forClass(Payload.class);

        List<Payload> payloads = createPayloads(11);

        // When
        for (Payload payload : payloads) {
            emitter.addToBuffer(payload);
        }

        // Then
        verify(emitter).flushBuffer();
        verify(httpClientAdapter, times(10)).get(argumentCaptor.capture());

        Assert.assertEquals(payloads.subList(0, payloads.size() - 1), argumentCaptor.getAllValues());

        Payload lastPayload = payloads.get(payloads.size() - 1);

        verify(httpClientAdapter, never()).get(lastPayload);

        Assert.assertTrue(emitter.getBuffer().size() == 1);
        Assert.assertEquals(lastPayload, emitter.getBuffer().get(0));

    }

    @Test
    public void addToBuffer_withPostMethod_withLess10Payloads_shouldNotFlushBuffer() throws Exception {
        // Given
        Emitter emitter = createEmitter(HttpMethod.POST);
        ArgumentCaptor<Payload> argumentCaptor = ArgumentCaptor.forClass(Payload.class);

        List<Payload> payloads = createPayloads(2);

        // When
        for (Payload payload : payloads) {
            emitter.addToBuffer(payload);
        }

        // Then
        verify(emitter, never()).flushBuffer();
        verify(httpClientAdapter, never()).get(argumentCaptor.capture());

        Assert.assertEquals(2, emitter.getBuffer().size());
        Assert.assertEquals(payloads, emitter.getBuffer());
    }

    @Test
    public void addToBuffer_withPostMethod_withMore10Payloads_shouldFlushBuffer() throws Exception {
        // Given
        Emitter emitter = createEmitter(HttpMethod.POST);
        ArgumentCaptor<SchemaPayload> argumentCaptor = ArgumentCaptor.forClass(SchemaPayload.class);

        List<Payload> payloads = createPayloads(11);

        // When
        for (Payload payload : payloads) {
            emitter.addToBuffer(payload);
        }

        // Then
        verify(emitter).flushBuffer();
        verify(httpClientAdapter).post(argumentCaptor.capture());
        Assert.assertEquals(payloads.subList(0, payloads.size() - 1), argumentCaptor.getValue().getData());

        Payload lastPayload = payloads.get(payloads.size() - 1);
        Assert.assertTrue(emitter.getBuffer().size() == 1);
        Assert.assertEquals(lastPayload, emitter.getBuffer().get(0));

    }

    private Emitter createEmitter(HttpMethod httpMethod) {
        return spy(new Emitter(
                httpMethod,
                new RequestCallback() {

                    @Override
                    public void onSuccess(int successCount) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    @Override
                    public void onFailure(int successCount, List<Map<String, Object>> failedEvent) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }
                },
                httpClientAdapter
        ));
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
