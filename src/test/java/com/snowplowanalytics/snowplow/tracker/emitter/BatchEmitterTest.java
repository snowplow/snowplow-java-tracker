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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;

import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerEvent;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.events.PageView;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;

public class BatchEmitterTest {

    private HttpClientAdapter httpClientAdapter;
    private Tracker tracker;
    private BatchEmitter emitter;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        httpClientAdapter = mock(HttpClientAdapter.class);
        tracker = mock(Tracker.class);
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

        List<TrackerEvent> events = createEvents(2);

        // When
        for (TrackerEvent event : events) {
            emitter.emit(event);
        }

        // Then
        verify(emitter, never()).flushBuffer();
        verify(httpClientAdapter, never()).get(argumentCaptor.capture());

        Assert.assertEquals(2, emitter.getBuffer().size());
        Assert.assertEquals(events, emitter.getBuffer());
    }

    @Test
    public void addToBuffer_withMore10Payloads_shouldFlushBuffer() throws Exception {
        // Given
        ArgumentCaptor<SelfDescribingJson> argumentCaptor = ArgumentCaptor.forClass(SelfDescribingJson.class);
        List<TrackerEvent> events = createEvents(10);

        // When
        for (TrackerEvent event : events) {
            emitter.emit(event);
        }

        Thread.sleep(500);

        // Then
        verify(emitter).flushBuffer();
        verify(httpClientAdapter).post(argumentCaptor.capture());

        List<Map<String, String>> payloadMaps = new ArrayList<>();
        for (TrackerEvent event : events) {
            payloadMaps.add(event.getTrackerPayload().getMap());
        }

        Assert.assertEquals(payloadMaps, argumentCaptor.getValue().getMap().get("data"));
        Assert.assertTrue(emitter.getBuffer().size() == 0);
    }

    @Test
    public void setBufferSize_WithNegativeValue_ThrowInvalidArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        emitter.setBufferSize(-1);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getFinalPost_shouldAddSTMParameter() throws Exception {
        // Given
        ArgumentCaptor<SelfDescribingJson> argumentCaptor = ArgumentCaptor.forClass(SelfDescribingJson.class);
        List<TrackerEvent> events = createEvents(10);

        // When
        for (TrackerEvent event : events) {
            emitter.emit(event);
        }

        Thread.sleep(500);

        // Then
        verify(httpClientAdapter).post(argumentCaptor.capture());

        ArrayList<Map<String, String>> dataList = (ArrayList<Map<String, String>>) argumentCaptor.getValue().getMap().get(Parameter.DATA);
        for (Map<String, String> payloadMap : dataList) {
            Assert.assertTrue(payloadMap.containsKey(Parameter.DEVICE_SENT_TIMESTAMP));
        }
    }

    private List<TrackerEvent> createEvents(int numEvents) {
        final List<TrackerEvent> payloads = Lists.newArrayList();
        for (int i = 0; i < numEvents; i++) {
            payloads.add(createEvent());
        }
        return payloads;
    }

    private TrackerEvent createEvent() {
        PageView pv = PageView.builder()
        .pageUrl("https://www.snowplowanalytics.com/")
        .pageTitle("Snowplow")
        .referrer("https://www.google.com/")
        .build();

        return new TrackerEvent(tracker, pv);
    }
}
