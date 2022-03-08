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
package com.snowplowanalytics.snowplow.tracker;

import java.util.*;
import static java.util.Collections.singletonList;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.common.collect.ImmutableMap;

import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.events.*;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;

public class TrackerTest {

    public static final String EXPECTED_CONTEXTS = "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/contexts/jsonschema/1-0-1\",\"data\":[{\"schema\":\"schema\",\"data\":{\"foo\":\"bar\"}}]}";

    public static class MockEmitter implements Emitter {
        public ArrayList<TrackerPayload> eventList = new ArrayList<>();

        @Override
        public boolean add(TrackerPayload payload) {
            eventList.add(payload);
            return true;
        }
        @Override
        public void setBatchSize(int batchSize) {}
        @Override
        public void flushBuffer() {}
        @Override
        public int getBatchSize() { return 0; }
        @Override
        public List<TrackerPayload> getBuffer() { return null; }
    }

    MockEmitter mockEmitter;
    Tracker tracker;
    private List<SelfDescribingJson> contexts;

    @Before
    public void setUp() {
        mockEmitter = new MockEmitter();
        tracker = new Tracker.TrackerBuilder(mockEmitter, "AF003", "cloudfront")
                .subject(new Subject.SubjectBuilder().build())
                .base64(false)
                .build();
        tracker.getSubject().setTimezone("Etc/UTC");
        contexts = singletonList(new SelfDescribingJson("schema", ImmutableMap.of("foo", "bar")));
    }

    // --- Event Tests

    @Test
    public void testTrackReturnsEventIdIfSuccessful() throws InterruptedException {
        // a list to allow for eCommerceTransaction
        List<String> result = tracker.track(Unstructured.builder()
                .eventData(new SelfDescribingJson(
                        "iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0",
                        ImmutableMap.of("foo", "bar")
                ))
                .build());

        Thread.sleep(500);

        boolean isValidEventId = true;
        try {
            UUID.fromString(result.get(0));
        } catch (Exception e) {
            isValidEventId = false;
        }

        assertTrue(isValidEventId);
    }

    @Test
    public void testTrackReturnsNullIfEventWasDropped() throws InterruptedException {
        class FailingMockEmitter implements Emitter {
            @Override
            public boolean add(TrackerPayload payload) { return false; }
            @Override
            public void setBatchSize(int batchSize) {}
            @Override
            public void flushBuffer() {}
            @Override
            public int getBatchSize() { return 0; }
            @Override
            public List<TrackerPayload> getBuffer() { return null; }
        }
        FailingMockEmitter failingMockEmitter = new FailingMockEmitter();
        tracker = new Tracker.TrackerBuilder(failingMockEmitter, "AF003", "cloudfront").build();

        List<String> result = tracker.track(Unstructured.builder()
                .eventData(new SelfDescribingJson(
                        "iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0",
                        ImmutableMap.of("foo", "bar")
                ))
                .build());

        Thread.sleep(500);

        assertNull(result.get(0));
    }

    @Test
    public void testEcommerceEvent() throws InterruptedException {
        // Given
        EcommerceTransactionItem item = EcommerceTransactionItem.builder()
                .itemId("order_id")
                .sku("sku")
                .price(1.0)
                .quantity(2)
                .name("name")
                .category("category")
                .currency("currency")
                .customContext(contexts)
                .trueTimestamp(456789L)
                .build();

        // When
        tracker.track(EcommerceTransaction.builder()
                .orderId("order_id")
                .totalValue(1.0)
                .affiliation("affiliation")
                .taxValue(2.0)
                .shipping(3.0)
                .city("city")
                .state("state")
                .country("country")
                .currency("currency")
                .items(item)
                .customContext(contexts)
                .trueTimestamp(456789L)
                .build());

        // Then
        Thread.sleep(500);

        List<TrackerPayload> results = mockEmitter.eventList;
        assertEquals(2, results.size());

        Map<String, String> result1 = results.get(0).getMap();
        Map<String, String> expected1 = ImmutableMap.<String, String>builder()
                .put("e", "tr")
                .put("tr_cu", "currency")
                .put("co", EXPECTED_CONTEXTS)
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("tr_sh", "3.0")
                .put("ttm", "456789")
                .put("tz", "Etc/UTC")
                .put("tr_co", "country")
                .put("tv", Version.TRACKER)
                .put("p", "srv")
                .put("tr_tx", "2.0")
                .put("tr_af", "affiliation")
                .put("tr_id", "order_id")
                .put("tr_tt", "1.0")
                .put("tr_ci", "city")
                .put("tr_st", "state")
                .build();

        assertTrue(result1.entrySet().containsAll(expected1.entrySet()));

        Map<String, String> result2 = results.get(1).getMap();
        Map<String, String> expected2 = ImmutableMap.<String, String>builder()
                .put("ti_nm", "name")
                .put("ti_id", "order_id")
                .put("e", "ti")
                .put("co", EXPECTED_CONTEXTS)
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("ti_cu", "currency")
                .put("ttm", "456789")
                .put("tz", "Etc/UTC")
                .put("ti_pr", "1.0")
                .put("ti_qu", "2")
                .put("p", "srv")
                .put("tv", Version.TRACKER)
                .put("ti_ca", "category")
                .put("ti_sk", "sku")
                .build();

        assertTrue(result2.entrySet().containsAll(expected2.entrySet()));
    }

    @Test
    public void testUnstructuredEventWithContext() throws InterruptedException {
        // When
        tracker.track(Unstructured.builder()
                .eventData(new SelfDescribingJson(
                        "iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0",
                        ImmutableMap.of("foo", "bar")
                ))
                .customContext(contexts)
                .trueTimestamp(456789L)
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        Map<String, String> expected = ImmutableMap.<String, String>builder()
                .put("p", "srv")
                .put("tv", Version.TRACKER)
                .put("e", "ue")
                .put("co", EXPECTED_CONTEXTS)
                .put("tna", "AF003")
                .put("tz", "Etc/UTC")
                .put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0\",\"data\":{\"foo\":\"bar\"}}}")
                .put("ttm", "456789")
                .put("aid", "cloudfront")
                .build();

        assertTrue(result.entrySet().containsAll(expected.entrySet()));
    }

    @Test
    public void testUnstructuredEventWithoutContext() throws InterruptedException {
        // When
        tracker.track(Unstructured.builder()
                .eventData(new SelfDescribingJson(
                        "iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0",
                        ImmutableMap.of("foo", "baær")
                ))
                .trueTimestamp(456789L)
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        Map<String, String> expected = ImmutableMap.<String, String>builder()
                .put("p", "srv")
                .put("tv", Version.TRACKER)
                .put("e", "ue")
                .put("tna", "AF003")
                .put("tz", "Etc/UTC")
                .put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0\",\"data\":{\"foo\":\"baær\"}}}")
                .put("ttm", "456789")
                .put("aid", "cloudfront")
                .build();

        assertTrue(result.entrySet().containsAll(expected.entrySet()));
    }

    @Test
    public void testUnstructuredEventWithoutTrueTimestamp() throws InterruptedException {
        // When
        tracker.track(Unstructured.builder()
                .eventData(new SelfDescribingJson(
                        "iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0",
                        ImmutableMap.of("foo", "bar")
                ))
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        Map<String, String> expected = ImmutableMap.<String, String>builder()
                .put("p", "srv")
                .put("tv", Version.TRACKER)
                .put("e", "ue")
                .put("tna", "AF003")
                .put("tz", "Etc/UTC")
                .put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0\",\"data\":{\"foo\":\"bar\"}}}")
                .put("aid", "cloudfront")
                .build();

        assertTrue(result.entrySet().containsAll(expected.entrySet()));
    }

    @Test
    public void testTrackPageView() throws InterruptedException {
        tracker = new Tracker.TrackerBuilder(mockEmitter, "AF003", "cloudfront")
                .subject(new Subject.SubjectBuilder().build())
                .base64(false)
                .build();
        tracker.getSubject().setTimezone("Etc/UTC");

        // When
        tracker.track(PageView.builder()
                .pageUrl("url")
                .pageTitle("title")
                .referrer("referer")
                .customContext(contexts)
                .trueTimestamp(456789L)
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        Map<String, String> expected = ImmutableMap.<String, String>builder()
                .put("ttm", "456789")
                .put("tz", "Etc/UTC")
                .put("e", "pv")
                .put("page", "title")
                .put("tv", Version.TRACKER)
                .put("p", "srv")
                .put("co", EXPECTED_CONTEXTS)
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("refr", "referer")
                .put("url", "url")
                .build();

        assertTrue(result.entrySet().containsAll(expected.entrySet()));
    }

    @Test
    public void testTrackTwoEvents() throws InterruptedException {
        // When
        tracker.track(PageView.builder()
                .pageUrl("url")
                .pageTitle("title")
                .referrer("referer")
                .trueTimestamp(123456L)
                .build());

        tracker.track(Unstructured.builder()
                .eventData(new SelfDescribingJson(
                        "iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0",
                        ImmutableMap.of("foo", "bar")
                ))
                .trueTimestamp(456789L)
                .build());

        // Then
        Thread.sleep(500);

        List<TrackerPayload> results = mockEmitter.eventList;
        assertEquals(2, results.size());

        Map<String, String> result1 = results.get(0).getMap();
        Map<String, String> expected1 = ImmutableMap.<String, String>builder()
                .put("ttm", "123456")
                .put("tz", "Etc/UTC")
                .put("e", "pv")
                .put("page", "title")
                .put("tv", Version.TRACKER)
                .put("p", "srv")
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("refr", "referer")
                .put("url", "url")
                .build();

        assertTrue(result1.entrySet().containsAll(expected1.entrySet()));

        Map<String, String> result2 = results.get(1).getMap();
        Map<String, String> expected2 = ImmutableMap.<String, String>builder()
                .put("ttm", "456789")
                .put("p", "srv")
                .put("tv", Version.TRACKER)
                .put("e", "ue")
                .put("tna", "AF003")
                .put("tz", "Etc/UTC")
                .put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0\",\"data\":{\"foo\":\"bar\"}}}")
                .put("aid", "cloudfront")
                .build();

        assertTrue(result2.entrySet().containsAll(expected2.entrySet()));
    }

    @Test
    public void testTrackScreenView() throws InterruptedException {
        // When
        tracker.track(ScreenView.builder()
                .name("name")
                .id("id")
                .customContext(contexts)
                .trueTimestamp(456789L)
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        Map<String, String> expected = ImmutableMap.<String, String>builder()
                .put("ttm", "456789")
                .put("tz", "Etc/UTC")
                .put("e", "ue")
                .put("tv", Version.TRACKER)
                .put("p", "srv")
                .put("co", EXPECTED_CONTEXTS)
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/screen_view/jsonschema/1-0-0\",\"data\":{\"id\":\"id\",\"name\":\"name\"}}}")
                .build();

        assertTrue(result.entrySet().containsAll(expected.entrySet()));
    }

    @Test
    public void testTrackScreenViewWithTimestamp() throws InterruptedException {
        // When
        tracker.track(ScreenView.builder()
                .name("name")
                .id("id")
                .trueTimestamp(456789L)
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        Map<String, String> expected = ImmutableMap.<String, String>builder()
                .put("ttm", "456789")
                .put("tz", "Etc/UTC")
                .put("e", "ue")
                .put("tv", Version.TRACKER)
                .put("p", "srv")
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/screen_view/jsonschema/1-0-0\",\"data\":{\"id\":\"id\",\"name\":\"name\"}}}")
                .build();

        assertTrue(result.entrySet().containsAll(expected.entrySet()));
    }

    @Test
    public void testTrackScreenViewWithDefaultContextAndTimestamp() throws InterruptedException {
        // When
        tracker.track(ScreenView.builder()
                .name("name")
                .id("id")
                .customContext(contexts)
                .trueTimestamp(456789L)
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        Map<String, String> expected = ImmutableMap.<String, String>builder()
                .put("p", "srv")
                .put("tv", Version.TRACKER)
                .put("e", "ue")
                .put("co", EXPECTED_CONTEXTS)
                .put("tna", "AF003")
                .put("tz", "Etc/UTC")
                .put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/screen_view/jsonschema/1-0-0\",\"data\":{\"id\":\"id\",\"name\":\"name\"}}}")
                .put("ttm", "456789")
                .put("aid", "cloudfront")
                .build();

        assertTrue(result.entrySet().containsAll(expected.entrySet()));
    }

    @Test
    public void testTrackTiming() throws InterruptedException {
        // When
        tracker.track(Timing.builder()
                .category("category")
                .label("label")
                .variable("variable")
                .timing(10)
                .customContext(contexts)
                .trueTimestamp(456789L)
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        Map<String, String> expected = ImmutableMap.<String, String>builder()
                .put("p", "srv")
                .put("tv", Version.TRACKER)
                .put("e", "ue")
                .put("co", EXPECTED_CONTEXTS)
                .put("tna", "AF003")
                .put("tz", "Etc/UTC")
                .put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/timing/jsonschema/1-0-0\",\"data\":{\"category\":\"category\",\"label\":\"label\",\"timing\":10,\"variable\":\"variable\"}}}")
                .put("ttm", "456789")
                .put("aid", "cloudfront")
                .build();

        assertTrue(result.entrySet().containsAll(expected.entrySet()));
    }

    @Test
    public void testTrackTimingWithSubject() throws InterruptedException {
        // Make Subject
        Subject s1 = new Subject.SubjectBuilder().build();
        s1.setIpAddress("127.0.0.1");
        s1.setTimezone("Etc/UTC");

        // When
        tracker.track(Timing.builder()
                .category("category")
                .label("label")
                .variable("variable")
                .timing(10)
                .customContext(contexts)
                .trueTimestamp(456789L)
                .subject(s1)
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        Map<String, String> expected = ImmutableMap.<String, String>builder()
                .put("p", "srv")
                .put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/timing/jsonschema/1-0-0\",\"data\":{\"category\":\"category\",\"label\":\"label\",\"timing\":10,\"variable\":\"variable\"}}}")
                .put("tv", Version.TRACKER)
                .put("e", "ue")
                .put("ip", "127.0.0.1")
                .put("co", EXPECTED_CONTEXTS)
                .put("tna", "AF003")
                .put("tz", "Etc/UTC")
                .put("ttm", "456789")
                .put("aid", "cloudfront")
                .build();

        assertTrue(result.entrySet().containsAll(expected.entrySet()));

    }

    // --- Tracker Setter & Getter Tests

    @Test
    public void testGetTrackerVersion() {
        Tracker tracker = new Tracker.TrackerBuilder(mockEmitter, "namespace", "an-app-id").build();
        assertEquals("java-0.12.0-alpha.1", tracker.getTrackerVersion());
    }

    @Test
    public void testSetDefaultPlatform() {
        Tracker tracker = new Tracker.TrackerBuilder(mockEmitter, "AF003", "cloudfront")
                .platform(DevicePlatform.Desktop)
                .build();
        assertEquals(DevicePlatform.Desktop, tracker.getPlatform());
    }

    @Test
    public void testSetSubject() {
        // Subject objects always have timezone set
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));

        Subject s1 = new Subject.SubjectBuilder().build();
        s1.setLanguage("EN");
        Tracker tracker = new Tracker.TrackerBuilder(mockEmitter, "AF003", "cloudfront")
                .subject(s1)
                .build();

        Subject s2 = new Subject.SubjectBuilder().build();
        s2.setColorDepth(24);
        tracker.setSubject(s2);

        Map<String, String> subjectPairs = new HashMap<>();
        subjectPairs.put("tz", "Etc/UTC");
        subjectPairs.put("cd", "24");

        assertEquals(subjectPairs, tracker.getSubject().getSubject());
    }

    @Test
    public void testSetBase64Encoded() {
        Tracker tracker = new Tracker.TrackerBuilder(mockEmitter, "AF003", "cloudfront")
                .base64(false)
                .build();
        assertFalse(tracker.getBase64Encoded());
    }

    @Test
    public void testSetAppId() {
        Tracker tracker = new Tracker.TrackerBuilder(mockEmitter, "AF003", "an-app-id").build();
        assertEquals("an-app-id", tracker.getAppId());
    }

    @Test
    public void testSetNamespace() {
        Tracker tracker = new Tracker.TrackerBuilder(mockEmitter, "namespace", "an-app-id").build();
        assertEquals("namespace", tracker.getNamespace());
    }
}
