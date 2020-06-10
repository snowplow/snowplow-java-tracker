/*
 * Copyright (c) 2014-2020 Snowplow Analytics Ltd. All rights reserved.
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

import static org.hamcrest.MatcherAssert.assertThat; 
import static org.hamcrest.Matchers.*;

import com.snowplowanalytics.snowplow.tracker.DevicePlatform;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerEvent;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerParameters;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.events.PageView;
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
    public void addToBuffer_withLess10Payloads_shouldNotEmptyBuffer() throws Exception {
        // Given
        ArgumentCaptor<TrackerPayload> argumentCaptor = ArgumentCaptor.forClass(TrackerPayload.class);

        List<TrackerEvent> events = createEvents(2);

        // When
        for (TrackerEvent event : events) {
            emitter.emit(event);
        }

        Thread.sleep(500);

        // Then
        verify(httpClientAdapter, never()).get(argumentCaptor.capture());

        Assert.assertEquals(2, emitter.getBuffer().size());
        Assert.assertEquals(events, emitter.getBuffer());
    }

    @Test
    public void addToBuffer_withMore10Payloads_shouldEmptyBuffer() throws Exception {
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

        @SuppressWarnings("unchecked")
        List<Map<String, String>> capturedPayload = (List<Map<String, String>>) argumentCaptor.getValue().getMap().get("data");

        assertPayload(events, capturedPayload);
        
        Assert.assertEquals(0, emitter.getBuffer().size());
    }

    @Test
    public void flushBuffer_shouldEmptyBuffer() throws Exception {
        // Given
        ArgumentCaptor<SelfDescribingJson> argumentCaptor = ArgumentCaptor.forClass(SelfDescribingJson.class);

        List<TrackerEvent> events = createEvents(2);

        // When
        for (TrackerEvent event : events) {
            emitter.emit(event);
        }

        emitter.flushBuffer();

        Thread.sleep(500);

        // Then
        verify(httpClientAdapter).post(argumentCaptor.capture());

        @SuppressWarnings("unchecked")
        List<Map<String, String>> capturedPayload = (List<Map<String, String>>) argumentCaptor.getValue().getMap().get(Parameter.DATA);

        assertPayload(events, capturedPayload);

        Assert.assertEquals(0, emitter.getBuffer().size());
    }

    @Test
    public void setBufferSize_WithNegativeValue_ThrowInvalidArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        emitter.setBufferSize(-1);
    }

    @Test
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

        @SuppressWarnings("unchecked")
        List<Map<String, String>> capturedPayload = (List<Map<String, String>>) argumentCaptor.getValue().getMap().get(Parameter.DATA);
        
        for (Map<String, String> payloadMap : capturedPayload) {
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

        return new TrackerEvent(pv, new TrackerParameters("appId", DevicePlatform.ServerSideApp, "namespace", "0.0.0", false), null);
    }

    private void assertPayload(List<TrackerEvent> events, List<Map<String, String>> capturedPayload) {
        List<Map<String, String>> eventPayloads = new ArrayList<>();
        for (TrackerEvent event : events) {
            //All PageView events so we can get(0) from payloads
            eventPayloads.add(event.getTrackerPayloads().get(0).getMap());
        }

        //Iterate through all captured payloads
        for (Map<String,String> capturedMap : capturedPayload) {
            boolean matchFound = false;
            for (Map<String,String> eventMap : eventPayloads) {
                //Find the matching events
                if (capturedMap.get("eid") == eventMap.get("eid")) {
                    matchFound = true;

                    //Assert that all the entries in the event are in the captured payload
                    //There might be extra entries in capturedMap, such as the STM parameter
                    //check for these addtional parameters in other tests
                    assertThat(eventMap.entrySet(), everyItem(is(in(capturedMap.entrySet()))));
                }
            }
            assertThat(matchFound, is(true)); //Ensure every event was found
        }
    }
}
