/*
 * Copyright (c) 2014-2021 Snowplow Analytics Ltd. All rights reserved.
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
import java.util.Objects;

import com.google.common.collect.Lists;

import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import com.snowplowanalytics.snowplow.tracker.events.PageView;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;

public class BatchEmitterTest {

    private MockHttpClientAdapter mockHttpClientAdapter;
    private BatchEmitter emitter;

    public static class MockHttpClientAdapter implements HttpClientAdapter {
        public boolean isGetCalled = false;
        public boolean isPostCalled = false;
        public int postCounter = 0;
        public SelfDescribingJson capturedPayload;

        @Override
        public int post(SelfDescribingJson payload) {
            System.out.println("post called");
            isPostCalled = true;
            postCounter++;
            capturedPayload = payload;
            return 200;
        }

        @Override
        public int get(TrackerPayload payload) {
            isGetCalled = true;
            return 0;
        }

        @Override
        public String getUrl() {
            return null;
        }

        @Override
        public Object getHttpClient() {
            return null;
        }
    }

    @Before
    public void setUp() {
        mockHttpClientAdapter = new MockHttpClientAdapter();
        emitter = BatchEmitter.builder()
                .httpClientAdapter(mockHttpClientAdapter)
                .bufferSize(10)
                .build();
    }

    @Test
    public void addToBuffer_withLess10Payloads_shouldNotEmptyBuffer() throws InterruptedException {
        List<TrackerPayload> payloads = createPayloads(2);
        for (TrackerPayload payload : payloads) {
            emitter.add(payload);
        }

        Thread.sleep(500);

        List<TrackerPayload> storedPayloads = emitter.getBuffer();

        Assert.assertFalse(mockHttpClientAdapter.isPostCalled);
        Assert.assertEquals(2, emitter.getBuffer().size());
        Assert.assertEquals(payloads, storedPayloads);
    }

    @Test
    public void addToBuffer_withMore10Payloads_shouldEmptyBuffer() throws InterruptedException {
        List<TrackerPayload> payloads = createPayloads(10);
        for (TrackerPayload payload : payloads) {
            emitter.add(payload);
        }

        Thread.sleep(500);

        Assert.assertTrue(mockHttpClientAdapter.isPostCalled);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> capturedPayload = (List<Map<String, String>>) mockHttpClientAdapter.capturedPayload.getMap().get("data");

//        assertPayload(payloads, capturedPayload);
        Assert.assertEquals(0, emitter.getBuffer().size());
        Assert.assertEquals(1, mockHttpClientAdapter.postCounter);
    }

    @Test
    public void flushBuffer_shouldEmptyBuffer() throws InterruptedException {
        List<TrackerPayload> payloads = createPayloads(2);
        for (TrackerPayload payload : payloads) {
            emitter.add(payload);
        }
        emitter.flushBuffer();

        Thread.sleep(500);

        Assert.assertTrue(mockHttpClientAdapter.isPostCalled);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> capturedPayload = (List<Map<String, String>>) mockHttpClientAdapter.capturedPayload.getMap().get("data");

        assertPayload(payloads, capturedPayload);
        Assert.assertEquals(0, emitter.getBuffer().size());
    }

    @Test
    public void setBufferSize_WithNegativeValue_ThrowInvalidArgumentException() {
        Exception exception = Assert.assertThrows(IllegalArgumentException.class, () -> emitter.setBufferSize(-1));
        Assert.assertEquals("bufferSize must be greater than 0", exception.getMessage());
    }

    @Test
    public void setAndGetBufferSizeWorksAsExpected() throws InterruptedException {
        emitter.setBufferSize(2);
        Assert.assertEquals(2, emitter.getBufferSize());

        List<TrackerPayload> payloads = createPayloads(2);
        for (TrackerPayload payload : payloads) {
            emitter.add(payload);
        }

        Thread.sleep(500);

        Assert.assertTrue(mockHttpClientAdapter.isPostCalled);
        Assert.assertEquals(0, emitter.getBuffer().size());
    }

    @Test
    public void getFinalPost_shouldAddSTMParameter() throws InterruptedException {
        List<TrackerPayload> payloads = createPayloads(10);
        for (TrackerPayload payload : payloads) {
            emitter.add(payload);
        }

        Thread.sleep(500);

        Assert.assertTrue(mockHttpClientAdapter.isPostCalled);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> capturedPayload = (List<Map<String, String>>) mockHttpClientAdapter.capturedPayload.getMap().get("data");

        for (Map<String, String> payloadMap : capturedPayload) {
            Assert.assertTrue(payloadMap.containsKey(Parameter.DEVICE_SENT_TIMESTAMP));
        }
    }

    @Test
    public void emitterThreadFactory_correctlyNamesThreads() {
        class MyRunnable implements Runnable {
            @Override
            public void run() {}
        }

        BatchEmitter.EmitterThreadFactory threadFactory = new BatchEmitter.EmitterThreadFactory();
        String threadName = threadFactory.newThread(new MyRunnable()).getName();

        // It's pool-2 because pool-1 was created during emitter instantiation
        Assert.assertEquals("snowplow-emitter-pool-2-request-thread-1", threadName);
    }

    @Test
    public void threadsHaveExpectedNames() {
        // A checkForEventsToSend thread is created on BatchEmitter instantiation.
        // Calling flushBuffer() here to require another thread - causing
        // creation of a request thread within the scheduledThreadPool.
        emitter.flushBuffer();

        // Create a list of all live thread names
        List<Thread> threadList = new ArrayList<>(Thread.getAllStackTraces().keySet());
        List<String> threadNames = new ArrayList<>();
        for (Thread thread : threadList) {
            threadNames.add(thread.getName());
        }

        Assert.assertTrue(threadNames.contains("snowplow-emitter-checkForEvents-thread-1"));
        Assert.assertTrue(threadNames.contains("snowplow-emitter-pool-1-request-thread-1"));
    }

    @Test
    public void close_sendsEventsAndStopsThreads() throws InterruptedException {
        List<TrackerPayload> payloads = createPayloads(2);
        for (TrackerPayload payload : payloads) {
            emitter.add(payload);
        }
        emitter.close();

        Thread.sleep(500);

        // close() calls flushBuffer() to send all remaining stored events
        Assert.assertTrue(mockHttpClientAdapter.isPostCalled);
        Assert.assertEquals(0, emitter.getBuffer().size());

        // these events can be added to storage but should not be sent
        List<TrackerPayload> morePayloads = createPayloads(20);
        for (TrackerPayload payload : morePayloads) {
            emitter.add(payload);
        }
        Assert.assertEquals(20, emitter.getBuffer().size());
    }

    private TrackerPayload createPayload() {
        PageView pv = PageView.builder()
                .pageUrl("https://www.snowplowanalytics.com/")
                .pageTitle("Snowplow")
                .referrer("https://www.google.com/")
                .build();

        return pv.getPayload();
    }

    private List<TrackerPayload> createPayloads(int numPayloads) {
        final List<TrackerPayload> payloads = Lists.newArrayList();
        for (int i = 0; i < numPayloads; i++) {
            payloads.add(createPayload());
        }
        return payloads;
    }

    private void assertPayload(List<TrackerPayload> payloads, List<Map<String, String>> capturedPayload) {
        List<Map<String, String>> eventPayloads = new ArrayList<>();
        for (TrackerPayload payload : payloads) {
            eventPayloads.add(payload.getMap());
        }

        //Iterate through all captured payloads
        for (Map<String,String> capturedMap : capturedPayload) {
            boolean matchFound = false;
            for (Map<String,String> eventMap : eventPayloads) {
                //Find the matching events
                if (Objects.equals(capturedMap.get("eid"), eventMap.get("eid"))) {
                    matchFound = true;

                    //Assert that all the entries in the event are in the captured payload
                    //There might be extra entries in capturedMap, such as the STM parameter
                    //check for these additional parameters in other tests
                    assertThat(eventMap.entrySet(), everyItem(is(in(capturedMap.entrySet()))));
                }
            }
            assertThat(matchFound, is(true)); //Ensure every event was found
        }
    }
}

