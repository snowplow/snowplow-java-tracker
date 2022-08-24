/*
 * Copyright (c) 2014-2022 Snowplow Analytics Ltd. All rights reserved.
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

import java.util.*;
import java.util.regex.Pattern;

import com.snowplowanalytics.snowplow.tracker.configuration.EmitterConfiguration;
import com.snowplowanalytics.snowplow.tracker.configuration.NetworkConfiguration;
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import com.snowplowanalytics.snowplow.tracker.events.PageView;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;

public class BatchEmitterTest {

    private MockHttpClientAdapter mockHttpClientAdapter;
    private BatchEmitter emitter;

    public static class MockHttpClientAdapter implements HttpClientAdapter {
        private final int statusCode;
        public boolean isGetCalled = false;
        public boolean isPostCalled = false;
        public int postCounter = 0;
        public SelfDescribingJson capturedPayload;

        public MockHttpClientAdapter(int statusCode) {
            this.statusCode = statusCode;
        }

        @Override
        public int post(SelfDescribingJson payload) {
            isPostCalled = true;
            postCounter++;
            capturedPayload = payload;
            return statusCode;
        }

        @Override
        public int get(TrackerPayload payload) {
            isGetCalled = true;
            return 0;
        }

        @Override
        public String getUrl() { return null; }

        @Override
        public Object getHttpClient() { return null; }
    }

    // this class fails to "send" the first 4 requests (status code 500)
    // but returns a successful result (200) subsequently
    static class FlakyHttpClientAdapter implements HttpClientAdapter {
        int failedPostCounter = 0;
        int successfulPostCounter = 0;
        @Override
        public int post(SelfDescribingJson payload) {
            if (failedPostCounter >= 4) {
                successfulPostCounter++;
                return 200;
            }

            failedPostCounter++;
            return 500;
        }

        @Override
        public int get(TrackerPayload payload) { return 0; }

        @Override
        public String getUrl() { return null; }

        @Override
        public Object getHttpClient() { return null; }
    }

    @Before
    public void setUp() {
        mockHttpClientAdapter = new MockHttpClientAdapter(200);
        EmitterConfiguration emitterConfig = new EmitterConfiguration().batchSize(10);
        emitter = new BatchEmitter(new NetworkConfiguration(mockHttpClientAdapter), emitterConfig);
    }

    @Test
    public void addToBuffer_withLess10Payloads_shouldNotEmptyBuffer() throws InterruptedException {
        TrackerPayload payload = createPayload();
        boolean result = emitter.add(payload);

        Thread.sleep(500);

        Assert.assertTrue(result);
        Assert.assertFalse(mockHttpClientAdapter.isPostCalled);
        Assert.assertEquals(1, emitter.getBuffer().size());
        Assert.assertEquals(payload, emitter.getBuffer().get(0));
    }

    @Test
    public void addToBuffer_withMore10Payloads_shouldEmptyBuffer() throws InterruptedException {
        List<TrackerPayload> payloads = createPayloads(10);
        for (TrackerPayload payload : payloads) {
            emitter.add(payload);
        }

        Thread.sleep(500);

        Assert.assertTrue(mockHttpClientAdapter.isPostCalled);

        Assert.assertEquals(0, emitter.getBuffer().size());
        Assert.assertEquals(1, mockHttpClientAdapter.postCounter);
    }

    @Test
    public void addToBuffer_doesNotAddEventIfBufferFull() {
        emitter = new BatchEmitter(
                new NetworkConfiguration(mockHttpClientAdapter),
                new EmitterConfiguration().bufferCapacity(1));
        emitter.add(createPayload());

        TrackerPayload differentPayload = createPayload();
        boolean result = emitter.add(differentPayload);

        Assert.assertFalse(emitter.getBuffer().contains(differentPayload));
        Assert.assertFalse(result);
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
    public void setBatchSize_WithNegativeValue_ThrowInvalidArgumentException() {
        Exception exception = Assert.assertThrows(IllegalArgumentException.class, () -> emitter.setBatchSize(-1));
        Assert.assertEquals("batchSize must be greater than 0", exception.getMessage());
    }

    @Test
    public void setAndGetBatchSizeWorksAsExpected() throws InterruptedException {
        emitter.setBatchSize(2);
        Assert.assertEquals(2, emitter.getBatchSize());

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
    public void threadsHaveExpectedNames() {
        // Calling flushBuffer() here to create a request thread for event sending
        emitter.flushBuffer();

        // Create a list of all live thread names
        List<Thread> threadList = new ArrayList<>(Thread.getAllStackTraces().keySet());
        List<String> threadNames = new ArrayList<>();
        for (Thread thread : threadList) {
            threadNames.add(thread.getName());
        }

        // Because the thread pools are named by a static ThreadFactory,
        // the pool number varies if this test is run in isolation or not
        boolean matchResult = false;
        for (String name : threadNames) {
            if (Pattern.matches("snowplow-emitter-pool-\\d+-request-thread-1", name)) {
                matchResult = true;
            }
        }

        Assert.assertTrue(matchResult);
    }

    @Test
    public void close_sendsEventsAndStopsThreads() throws InterruptedException {
        List<TrackerPayload> payloads = createPayloads(2);
        for (TrackerPayload payload : payloads) {
            emitter.add(payload);
        }
        Thread.sleep(500);

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

    @Test
    public void createEmitterWithConfiguration() {
        NetworkConfiguration networkConfig = new NetworkConfiguration("http://endpoint");
        EmitterConfiguration emitterConfig = new EmitterConfiguration().batchSize(5);
        BatchEmitter emitter = new BatchEmitter(networkConfig, emitterConfig);

        Assert.assertEquals(5, emitter.getBatchSize());
    }

    @Test
    public void eventsThatFailToSendAreReturnedToEventBuffer() throws InterruptedException {
        emitter = new BatchEmitter(
                new NetworkConfiguration(new FlakyHttpClientAdapter()),
                new EmitterConfiguration().batchSize(10));

        List<TrackerPayload> payloads = createPayloads(2);
        for (TrackerPayload payload : payloads) {
            emitter.add(payload);
        }
        emitter.flushBuffer();
        Thread.sleep(500);

        List<TrackerPayload> storedEvents = emitter.getBuffer();

        Assert.assertEquals(2, storedEvents.size());
        Assert.assertTrue(storedEvents.contains(payloads.get(0)));
        Assert.assertTrue(storedEvents.contains(payloads.get(1)));
    }

    @Test
    public void eventSendingFailureIncreasesBackoffTime() throws InterruptedException {
        emitter = new BatchEmitter(
                new NetworkConfiguration(new MockHttpClientAdapter(500)),
                new EmitterConfiguration().batchSize(1));

        emitter.add(createPayload());
        Thread.sleep(500);

        int firstDelay = emitter.getRetryDelay();
        Assert.assertNotEquals(0, firstDelay);

        emitter.add(createPayload());
        Thread.sleep(500);

        int secondDelay = emitter.getRetryDelay();
        Assert.assertTrue(secondDelay > firstDelay);
    }

    @Test
    public void successfulSendAfterFailureResetsBackoffTime() throws InterruptedException {
        // the FlakyHttpClientAdapter returns 500 for the first 4 requests
        // then subsequently returns 200
        FlakyHttpClientAdapter flakyHttpClientAdapter = new FlakyHttpClientAdapter();
        emitter = new BatchEmitter(
                new NetworkConfiguration(flakyHttpClientAdapter),
                new EmitterConfiguration().batchSize(1).threadCount(1));

        List<TrackerPayload> payloads = createPayloads(6);
        for (TrackerPayload payload : payloads) {
            emitter.add(payload);
        }

        Thread.sleep(500);

        Assert.assertEquals(2, flakyHttpClientAdapter.successfulPostCounter);
        Assert.assertEquals(0, emitter.getRetryDelay());
    }

    @Test
    public void retryWithCustomRulesOverridingDefault() throws InterruptedException {
        Map<Integer, Boolean> customRetry = new HashMap<>();
        customRetry.put(403, true);

        // by default 403 isn't retried
        BatchEmitter emitter = new BatchEmitter(
                new NetworkConfiguration(new MockHttpClientAdapter(403)),
                new EmitterConfiguration().batchSize(2).customRetryForStatusCodes(customRetry));

        List<TrackerPayload> payloads = createPayloads(4);
        for (TrackerPayload payload : payloads) {
            emitter.add(payload);
        }

        Thread.sleep(500);
        Assert.assertNotEquals(0, emitter.getRetryDelay());
        Assert.assertEquals(4, emitter.getBuffer().size());
    }

    @Test
    public void noRetryWithCustomRulesOverridingDefault() throws InterruptedException {
        Map<Integer, Boolean> customRetry = new HashMap<>();
        customRetry.put(500, false);

        // by default, requests with code 500 are retried
        BatchEmitter emitter = new BatchEmitter(
                new NetworkConfiguration(new MockHttpClientAdapter(500)),
                new EmitterConfiguration().batchSize(2).customRetryForStatusCodes(customRetry));

        List<TrackerPayload> payloads = createPayloads(4);
        for (TrackerPayload payload : payloads) {
            emitter.add(payload);
        }

        Thread.sleep(500);

        Assert.assertEquals(0, emitter.getRetryDelay());
        Assert.assertEquals(0, emitter.getBuffer().size());
    }

    @Test
    public void callsSuccessCallbackAfterSending() throws InterruptedException {
        class TestCallback implements EmitterCallback {
            List<TrackerPayload> payloads;
            final List<FailureType> failureTypes = new ArrayList<>();

            @Override
            public void onSuccess(List<TrackerPayload> payloads) {
                this.payloads = payloads;
            }

            @Override
            public void onFailure(FailureType failureType, boolean willRetry, List<TrackerPayload> payloads) {
                failureTypes.add(failureType);
            }
        }

        TestCallback callback = new TestCallback();
        BatchEmitter emitter = new BatchEmitter(
                new NetworkConfiguration(new MockHttpClientAdapter(200)),
                new EmitterConfiguration().batchSize(1).callback(callback));

        TrackerPayload payload = createPayload();
        emitter.add(payload);
        Thread.sleep(500);

        Assert.assertEquals(callback.payloads.get(0), payload);
        Assert.assertTrue(callback.failureTypes.isEmpty());
    }

    @Test
    public void callsFailureCallbackWhenRejectedNeedsRetry() throws InterruptedException {
        class TestCallback implements EmitterCallback {
            boolean willRetry;
            List<TrackerPayload> payloads;
            final List<FailureType> failureTypes = new ArrayList<>();

            @Override
            public void onSuccess(List<TrackerPayload> payloads) {
            }

            @Override
            public void onFailure(FailureType failureType, boolean willRetry, List<TrackerPayload> payloads) {
                failureTypes.add(failureType);
                this.willRetry = willRetry;
                this.payloads = payloads;
            }
        }

        TestCallback callback = new TestCallback();
        BatchEmitter emitter = new BatchEmitter(
                new NetworkConfiguration(new MockHttpClientAdapter(500)),
                new EmitterConfiguration().batchSize(1).callback(callback));

        TrackerPayload payload = createPayload();
        emitter.add(payload);
        Thread.sleep(500);

        Assert.assertEquals(FailureType.REJECTED_BY_COLLECTOR, callback.failureTypes.get(0));
        Assert.assertTrue(callback.willRetry);
        Assert.assertEquals(callback.payloads.get(0), payload);
    }

    @Test
    public void callsFailureCallbackWhenRejectedNoRetry() throws InterruptedException {
        class TestCallback implements EmitterCallback {
            boolean willRetry;
            List<TrackerPayload> payloads;
            final List<FailureType> failureTypes = new ArrayList<>();

            @Override
            public void onSuccess(List<TrackerPayload> payloads) {
            }

            @Override
            public void onFailure(FailureType failureType, boolean willRetry, List<TrackerPayload> payloads) {
                failureTypes.add(failureType);
                this.willRetry = willRetry;
                this.payloads = payloads;
            }
        }

        TestCallback callback = new TestCallback();
        BatchEmitter emitter = new BatchEmitter(
                new NetworkConfiguration(new MockHttpClientAdapter(403)),
                new EmitterConfiguration().batchSize(1).callback(callback));

        TrackerPayload payload = createPayload();
        emitter.add(payload);
        Thread.sleep(500);

        Assert.assertEquals(FailureType.REJECTED_BY_COLLECTOR, callback.failureTypes.get(0));
        Assert.assertEquals(1, callback.failureTypes.size());
        Assert.assertFalse(callback.willRetry);
        Assert.assertEquals(callback.payloads.get(0), payload);
    }

    @Test
    public void callsFailureCallbackIfStorageIsFull() throws InterruptedException {
        class TestCallback implements EmitterCallback {
            boolean willRetry;
            List<TrackerPayload> payloads;
            final List<FailureType> failureTypes = new ArrayList<>();

            @Override
            public void onSuccess(List<TrackerPayload> payloads) {
            }

            @Override
            public void onFailure(FailureType failureType, boolean willRetry, List<TrackerPayload> payloads) {
                failureTypes.add(failureType);
                this.willRetry = willRetry;
                this.payloads = payloads;
            }
        }

        TestCallback callback = new TestCallback();
        BatchEmitter emitter = new BatchEmitter(
                new NetworkConfiguration(new MockHttpClientAdapter(200)),
                new EmitterConfiguration().bufferCapacity(1).callback(callback));

        emitter.add(createPayload());
        TrackerPayload payload = createPayload();
        emitter.add(payload);
        Thread.sleep(500);

        Assert.assertEquals(FailureType.TRACKER_STORAGE_FULL, callback.failureTypes.get(0));
        Assert.assertEquals(1, callback.failureTypes.size());
        Assert.assertFalse(callback.willRetry);
        Assert.assertEquals(callback.payloads.get(0), payload);
    }

    @Test
    public void callsFailureCallbackIfHttpClientAdapterFailsToSend() throws InterruptedException {
        class TestCallback implements EmitterCallback {
            boolean willRetry;
            List<TrackerPayload> payloads;
            final List<FailureType> failureTypes = new ArrayList<>();

            @Override
            public void onSuccess(List<TrackerPayload> payloads) {
            }

            @Override
            public void onFailure(FailureType failureType, boolean willRetry, List<TrackerPayload> payloads) {
                failureTypes.add(failureType);
                this.willRetry = willRetry;
                this.payloads = payloads;
            }
        }

        TestCallback callback = new TestCallback();
        BatchEmitter emitter = new BatchEmitter(
                new NetworkConfiguration(new MockHttpClientAdapter(-1)),
                new EmitterConfiguration().bufferCapacity(1).callback(callback));

        TrackerPayload payload = createPayload();
        emitter.add(payload);
        emitter.flushBuffer();
        Thread.sleep(500);

        Assert.assertEquals(FailureType.HTTP_CONNECTION_FAILURE, callback.failureTypes.get(0));
        Assert.assertEquals(1, callback.failureTypes.size());
        Assert.assertTrue(callback.willRetry);
        Assert.assertEquals(callback.payloads.get(0), payload);
    }

    @Test
    public void callsFailureCallbackIfStorageIsFullWhenReturningEventsForRetry() throws InterruptedException {
        class TestCallback implements EmitterCallback {
            boolean willRetry;
            List<TrackerPayload> payloads;
            final List<FailureType> failureTypes = new ArrayList<>();

            @Override
            public void onSuccess(List<TrackerPayload> payloads) {
            }

            @Override
            public void onFailure(FailureType failureType, boolean willRetry, List<TrackerPayload> payloads) {
                failureTypes.add(failureType);
                this.willRetry = willRetry;
                this.payloads = payloads;
            }
        }

        TestCallback callback = new TestCallback();
        BatchEmitter emitter = new BatchEmitter(
                new NetworkConfiguration(new MockHttpClientAdapter(500)),
                new EmitterConfiguration().bufferCapacity(2).callback(callback));

        TrackerPayload payload1 = createPayload();
        TrackerPayload payload2 = createPayload();
        TrackerPayload payload3 = createPayload();

        emitter.add(payload1);
        emitter.flushBuffer();
        Thread.sleep(10);

        emitter.add(payload2);
        emitter.add(payload3);
        Thread.sleep(500);

        Assert.assertEquals(FailureType.REJECTED_BY_COLLECTOR, callback.failureTypes.get(0));
        Assert.assertEquals(FailureType.TRACKER_STORAGE_FULL, callback.failureTypes.get(1));
        Assert.assertEquals(2, callback.failureTypes.size());
        Assert.assertFalse(callback.willRetry);
        Assert.assertEquals(callback.payloads.get(0), payload3);
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
        final List<TrackerPayload> payloads = new ArrayList<>();
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
                    Assert.assertTrue(capturedMap.entrySet().containsAll(eventMap.entrySet()));
                }
            }
            Assert.assertTrue(matchFound); //Ensure every event was found
        }
    }
}

