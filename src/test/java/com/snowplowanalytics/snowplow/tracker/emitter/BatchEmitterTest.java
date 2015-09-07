/*
 * Copyright (c) 2015 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.snowplowanalytics.snowplow.tracker.emitter;

// Java
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Google
import com.google.common.collect.Lists;

// JUnit
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

// Mockito
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;

// This library
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;

public class BatchEmitterTest {

    private HttpClientAdapter httpClientAdapter;
    private BatchEmitter emitter;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        httpClientAdapter = mock(HttpClientAdapter.class);
        emitter = spy(BatchEmitter.builder()
                .httpClientAdapter(httpClientAdapter)
                .bufferSize(10)
                .build());
    }

    @Test
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public void addToBuffer_withLess10Payloads_shouldNotFlushBuffer() throws Exception {
        // Given
        ArgumentCaptor<TrackerPayload> argumentCaptor = ArgumentCaptor.forClass(TrackerPayload.class);

        List<TrackerPayload> payloads = createPayloads(2);

        // When
        for (TrackerPayload payload : payloads) {
            emitter.emit(payload);
        }

        // Then
        verify(emitter, never()).flushBuffer();
        verify(httpClientAdapter, never()).get(argumentCaptor.capture());

        Assert.assertEquals(2, emitter.getBuffer().size());
        Assert.assertEquals(payloads, emitter.getBuffer());
    }

    @Test
    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
    public void addToBuffer_withMore10Payloads_shouldFlushBuffer() throws Exception {
        // Given
        ArgumentCaptor<SelfDescribingJson> argumentCaptor = ArgumentCaptor.forClass(SelfDescribingJson.class);
        List<TrackerPayload> payloads = createPayloads(10);

        // When
        for (TrackerPayload payload : payloads) {
            emitter.emit(payload);
        }

        Thread.sleep(500);

        // Then
        verify(emitter).flushBuffer();
        verify(httpClientAdapter).post(argumentCaptor.capture());

        List<Map> payloadMaps = new ArrayList<>();
        for (TrackerPayload payload : payloads) {
            payloadMaps.add(payload.getMap());
        }

        Assert.assertEquals(payloadMaps, argumentCaptor.getValue().getMap().get("data"));
        Assert.assertTrue(emitter.getBuffer().size() == 0);
    }

    @Test
    public void setBufferSize_WithNegativeValue_ThrowInvalidArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        emitter.setBufferSize(-1);
    }

    private List<TrackerPayload> createPayloads(int nbPayload) {
        final List<TrackerPayload> payloads = Lists.newArrayList();
        for (int i = 0; i < nbPayload; i++) {
            payloads.add(createPayload());
        }
        return payloads;
    }

    private TrackerPayload createPayload() {
        TrackerPayload payload = new TrackerPayload();
        payload.add("id", UUID.randomUUID().toString());
        return payload;
    }
}
