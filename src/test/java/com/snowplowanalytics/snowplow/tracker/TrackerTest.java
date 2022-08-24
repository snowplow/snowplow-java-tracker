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
package com.snowplowanalytics.snowplow.tracker;

import java.util.*;
import static java.util.Collections.singletonList;

import com.snowplowanalytics.snowplow.tracker.configuration.NetworkConfiguration;
import com.snowplowanalytics.snowplow.tracker.configuration.SubjectConfiguration;
import com.snowplowanalytics.snowplow.tracker.configuration.TrackerConfiguration;
import com.snowplowanalytics.snowplow.tracker.emitter.BatchEmitter;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

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
        TrackerConfiguration trackerConfig = new TrackerConfiguration("AF003", "cloudfront").base64Encoded(false);
        tracker = new Tracker(trackerConfig, mockEmitter, new Subject(new SubjectConfiguration()));
        tracker.getSubject().setTimezone("Etc/UTC");
        contexts = singletonList(new SelfDescribingJson("schema", Collections.singletonMap("foo", "bar")));
    }

    // --- Event Tests

    @Test
    public void testTrackReturnsEventIdIfSuccessful() throws InterruptedException {
        // a list to allow for eCommerceTransaction
        List<String> result = tracker.track(SelfDescribing.builder()
                .eventData(new SelfDescribingJson(
                        "iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0",
                        Collections.singletonMap("foo", "bar")
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
        tracker = new Tracker("AF003", "cloudfront", failingMockEmitter);

        List<String> result = tracker.track(SelfDescribing.builder()
                .eventData(new SelfDescribingJson(
                        "iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0",
                        Collections.singletonMap("foo", "bar")
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
        Map<String, String> expected1 = new HashMap<>();
        expected1.put("e", "tr");
        expected1.put("tr_cu", "currency");
        expected1.put("co", EXPECTED_CONTEXTS);
        expected1.put("tna", "AF003");
        expected1.put("aid", "cloudfront");
        expected1.put("tr_sh", "3.0");
        expected1.put("ttm", "456789");
        expected1.put("tz", "Etc/UTC");
        expected1.put("tr_co", "country");
        expected1.put("tv", Version.TRACKER);
        expected1.put("p", "srv");
        expected1.put("tr_tx", "2.0");
        expected1.put("tr_af", "affiliation");
        expected1.put("tr_id", "order_id");
        expected1.put("tr_tt", "1.0");
        expected1.put("tr_ci", "city");
        expected1.put("tr_st", "state");

        assertTrue(result1.entrySet().containsAll(expected1.entrySet()));

        Map<String, String> result2 = results.get(1).getMap();
        Map<String, String> expected2 = new HashMap<>();
        expected2.put("ti_nm", "name");
        expected2.put("ti_id", "order_id");
        expected2.put("e", "ti");
        expected2.put("co", EXPECTED_CONTEXTS);
        expected2.put("tna", "AF003");
        expected2.put("aid", "cloudfront");
        expected2.put("ti_cu", "currency");
        expected2.put("ttm", "456789");
        expected2.put("tz", "Etc/UTC");
        expected2.put("ti_pr", "1.0");
        expected2.put("ti_qu", "2");
        expected2.put("p", "srv");
        expected2.put("tv", Version.TRACKER);
        expected2.put("ti_ca", "category");
        expected2.put("ti_sk", "sku");

        assertTrue(result2.entrySet().containsAll(expected2.entrySet()));
    }

    @Test
    public void testEcommerceTransactionItemAlone() throws InterruptedException {
        // Although surprising, EcommerceTransactionItems are valid events and
        // can be sent separately from EcommerceTransactions.

        tracker.track(EcommerceTransactionItem.builder()
                .itemId("order_id")
                .sku("sku")
                .price(1.0)
                .quantity(2)
                .name("name")
                .category("category")
                .currency("currency")
                .customContext(contexts)
                .trueTimestamp(456789L)
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        Map<String, String> expected = new HashMap<>();
        expected.put("ti_nm", "name");
        expected.put("ti_id", "order_id");
        expected.put("e", "ti");
        expected.put("co", EXPECTED_CONTEXTS);
        expected.put("tna", "AF003");
        expected.put("aid", "cloudfront");
        expected.put("ti_cu", "currency");
        expected.put("ttm", "456789");
        expected.put("tz", "Etc/UTC");
        expected.put("ti_pr", "1.0");
        expected.put("ti_qu", "2");
        expected.put("p", "srv");
        expected.put("tv", Version.TRACKER);
        expected.put("ti_ca", "category");
        expected.put("ti_sk", "sku");

        assertTrue(result.entrySet().containsAll(expected.entrySet()));
    }

    @Test
    public void testSelfDescribingEventWithContext() throws InterruptedException {
        // When
        tracker.track(SelfDescribing.builder()
                .eventData(new SelfDescribingJson(
                        "iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0",
                        Collections.singletonMap("foo", "bar")
                ))
                .customContext(contexts)
                .trueTimestamp(456789L)
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        Map<String, String> expected = new HashMap<>();
        expected.put("p", "srv");
        expected.put("tv", Version.TRACKER);
        expected.put("e", "ue");
        expected.put("co", EXPECTED_CONTEXTS);
        expected.put("tna", "AF003");
        expected.put("tz", "Etc/UTC");
        expected.put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0\",\"data\":{\"foo\":\"bar\"}}}");
        expected.put("ttm", "456789");
        expected.put("aid", "cloudfront");

        assertTrue(result.entrySet().containsAll(expected.entrySet()));
    }

    @Test
    public void testSelfDescribingEventWithoutContext() throws InterruptedException {
        // When
        tracker.track(SelfDescribing.builder()
                .eventData(new SelfDescribingJson(
                        "iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0",
                        Collections.singletonMap("foo", "baær")
                ))
                .trueTimestamp(456789L)
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        Map<String, String> expected = new HashMap<>();
        expected.put("p", "srv");
        expected.put("tv", Version.TRACKER);
        expected.put("e", "ue");
        expected.put("tna", "AF003");
        expected.put("tz", "Etc/UTC");
        expected.put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0\",\"data\":{\"foo\":\"baær\"}}}");
        expected.put("ttm", "456789");
        expected.put("aid", "cloudfront");

        assertTrue(result.entrySet().containsAll(expected.entrySet()));
    }

    @Test
    public void testSelfDescribingEventWithoutTrueTimestamp() throws InterruptedException {
        // When
        tracker.track(SelfDescribing.builder()
                .eventData(new SelfDescribingJson(
                        "iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0",
                        Collections.singletonMap("foo", "bar")
                ))
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        Map<String, String> expected = new HashMap<>();
        expected.put("p", "srv");
        expected.put("tv", Version.TRACKER);
        expected.put("e", "ue");
        expected.put("tna", "AF003");
        expected.put("tz", "Etc/UTC");
        expected.put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0\",\"data\":{\"foo\":\"bar\"}}}");
        expected.put("aid", "cloudfront");

        assertTrue(result.entrySet().containsAll(expected.entrySet()));
    }

    @Test
    public void testTrackPageView() throws InterruptedException {
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
        Map<String, String> expected = new HashMap<>();
        expected.put("ttm", "456789");
        expected.put("tz", "Etc/UTC");
        expected.put("e", "pv");
        expected.put("page", "title");
        expected.put("tv", Version.TRACKER);
        expected.put("p", "srv");
        expected.put("co", EXPECTED_CONTEXTS);
        expected.put("tna", "AF003");
        expected.put("aid", "cloudfront");
        expected.put("refr", "referer");
        expected.put("url", "url");

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

        tracker.track(SelfDescribing.builder()
                .eventData(new SelfDescribingJson(
                        "iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0",
                        Collections.singletonMap("foo", "bar")
                ))
                .trueTimestamp(456789L)
                .build());

        // Then
        Thread.sleep(500);

        List<TrackerPayload> results = mockEmitter.eventList;
        assertEquals(2, results.size());

        Map<String, String> result1 = results.get(0).getMap();
        Map<String, String> expected = new HashMap<>();
        expected.put("ttm", "123456");
        expected.put("tz", "Etc/UTC");
        expected.put("e", "pv");
        expected.put("page", "title");
        expected.put("tv", Version.TRACKER);
        expected.put("p", "srv");
        expected.put("tna", "AF003");
        expected.put("aid", "cloudfront");
        expected.put("refr", "referer");
        expected.put("url", "url");

        assertTrue(result1.entrySet().containsAll(expected.entrySet()));

        Map<String, String> result2 = results.get(1).getMap();
        Map<String, String> expected2 = new HashMap<>();
        expected2.put("ttm", "456789");
        expected2.put("p", "srv");
        expected2.put("tv", Version.TRACKER);
        expected2.put("e", "ue");
        expected2.put("tna", "AF003");
        expected2.put("tz", "Etc/UTC");
        expected2.put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0\",\"data\":{\"foo\":\"bar\"}}}");
        expected2.put("aid", "cloudfront");

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
        Map<String, String> expected = new HashMap<>();
        expected.put("ttm", "456789");
        expected.put("tz", "Etc/UTC");
        expected.put("e", "ue");
        expected.put("tv", Version.TRACKER);
        expected.put("p", "srv");
        expected.put("co", EXPECTED_CONTEXTS);
        expected.put("tna", "AF003");
        expected.put("aid", "cloudfront");
        expected.put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/screen_view/jsonschema/1-0-0\",\"data\":{\"id\":\"id\",\"name\":\"name\"}}}");

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
        Map<String, String> expected = new HashMap<>();
        expected.put("ttm", "456789");
        expected.put("tz", "Etc/UTC");
        expected.put("e", "ue");
        expected.put("tv", Version.TRACKER);
        expected.put("p", "srv");
        expected.put("tna", "AF003");
        expected.put("aid", "cloudfront");
        expected.put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/screen_view/jsonschema/1-0-0\",\"data\":{\"id\":\"id\",\"name\":\"name\"}}}");

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
        Map<String, String> expected = new HashMap<>();
        expected.put("p", "srv");
        expected.put("tv", Version.TRACKER);
        expected.put("e", "ue");
        expected.put("co", EXPECTED_CONTEXTS);
        expected.put("tna", "AF003");
        expected.put("tz", "Etc/UTC");
        expected.put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/screen_view/jsonschema/1-0-0\",\"data\":{\"id\":\"id\",\"name\":\"name\"}}}");
        expected.put("ttm", "456789");
        expected.put("aid", "cloudfront");

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
        Map<String, String> expected = new HashMap<>();
        expected.put("p", "srv");
        expected.put("tv", Version.TRACKER);
        expected.put("e", "ue");
        expected.put("co", EXPECTED_CONTEXTS);
        expected.put("tna", "AF003");
        expected.put("tz", "Etc/UTC");
        expected.put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/timing/jsonschema/1-0-0\",\"data\":{\"category\":\"category\",\"label\":\"label\",\"timing\":10,\"variable\":\"variable\"}}}");
        expected.put("ttm", "456789");
        expected.put("aid", "cloudfront");

        assertTrue(result.entrySet().containsAll(expected.entrySet()));
    }

    @Test
    public void testTrackTimingWithSubject() throws InterruptedException {
        // Make Subject
        Subject s1 = new Subject(new SubjectConfiguration());
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
        Map<String, String> expected = new HashMap<>();
        expected.put("p", "srv");
        expected.put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/timing/jsonschema/1-0-0\",\"data\":{\"category\":\"category\",\"label\":\"label\",\"timing\":10,\"variable\":\"variable\"}}}");
        expected.put("tv", Version.TRACKER);
        expected.put("e", "ue");
        expected.put("ip", "127.0.0.1");
        expected.put("co", EXPECTED_CONTEXTS);
        expected.put("tna", "AF003");
        expected.put("tz", "Etc/UTC");
        expected.put("ttm", "456789");
        expected.put("aid", "cloudfront");

        assertTrue(result.entrySet().containsAll(expected.entrySet()));

    }

    // --- Tracker Setter & Getter Tests

    @Test
    public void testCreateWithConfiguration() {
        TrackerConfiguration trackerConfig = new TrackerConfiguration("namespace", "appId");
        trackerConfig.base64Encoded(false);
        trackerConfig.platform(DevicePlatform.General);

        BatchEmitter emitter = new BatchEmitter(new NetworkConfiguration("http://collector"));
        Tracker tracker = new Tracker(trackerConfig, emitter);

        assertEquals("namespace", tracker.getNamespace());
        assertEquals(emitter, tracker.getEmitter());
    }

    @Test
    public void testGetTrackerVersion() {
        Tracker tracker = new Tracker("namespace", "an-app-id", mockEmitter);
        assertEquals("java-0.12.2", tracker.getTrackerVersion());
    }

    @Test
    public void testSetDefaultPlatform() {
        TrackerConfiguration trackerConfig = new TrackerConfiguration("AF003", "cloudfront")
                .platform(DevicePlatform.Desktop);

        Tracker tracker = new Tracker(trackerConfig, mockEmitter);
        assertEquals(DevicePlatform.Desktop, tracker.getPlatform());
    }

    @Test
    public void testSetSubject() {
        // Subject objects always have timezone set
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));

        Subject s1 = new Subject(new SubjectConfiguration());
        s1.setLanguage("EN");
        Tracker tracker = new Tracker(new TrackerConfiguration("AF003", "cloudfront"), mockEmitter, s1);

        Subject s2 = new Subject(new SubjectConfiguration());
        s2.setColorDepth(24);
        tracker.setSubject(s2);

        Map<String, String> subjectPairs = new HashMap<>();
        subjectPairs.put("tz", "Etc/UTC");
        subjectPairs.put("cd", "24");

        assertEquals(subjectPairs, tracker.getSubject().getSubject());
    }

    @Test
    public void testSetBase64Encoded() {
        TrackerConfiguration trackerConfig = new TrackerConfiguration("AF003", "cloudfront").base64Encoded(false);
        tracker = new Tracker(trackerConfig, mockEmitter);

        assertFalse(tracker.getBase64Encoded());
    }

    @Test
    public void testSetAppId() {
        Tracker tracker = new Tracker(new TrackerConfiguration("AF003", "an-app-id"), mockEmitter);
        assertEquals("an-app-id", tracker.getAppId());
    }

    @Test
    public void testSetNamespace() {
        Tracker tracker = new Tracker(new TrackerConfiguration("namespace", "an-app-id"), mockEmitter);

        assertEquals("namespace", tracker.getNamespace());
    }
}
