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

import com.snowplowanalytics.snowplow.tracker.emitter.BatchPayload;
import org.junit.Assert;
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
    public static final String EXPECTED_EVENT_ID = "15e9b149-6029-4f6e-8447-5b9797c9e6be";

    public static class MockEmitter implements Emitter {
        public ArrayList<TrackerPayload> eventList = new ArrayList<>();

        @Override
        public void add(TrackerPayload payload) {
            eventList.add(payload);
        }

        @Override
        public void setBufferSize(int bufferSize) {}

        @Override
        public void flushBuffer() {}

        @Override
        public int getBufferSize() {
            return 0;
        }

        @Override
        public List<TrackerPayload> getBuffer() {
            return null;
        }
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
                .deviceCreatedTimestamp(123456)
                .trueTimestamp(456789L)
                .eventId(EXPECTED_EVENT_ID)
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
                .deviceCreatedTimestamp(123456)
                .trueTimestamp(456789L)
                .eventId(EXPECTED_EVENT_ID)
                .build());

        // Then
        Thread.sleep(500);

        List<TrackerPayload> results = mockEmitter.eventList;
        assertEquals(2, results.size());

        Map<String, String> result1 = results.get(0).getMap();
        assertEquals(ImmutableMap.<String, String>builder()
                .put("e", "tr")
                .put("tr_cu", "currency")
                .put("co", EXPECTED_CONTEXTS)
                .put("eid", EXPECTED_EVENT_ID)
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("tr_sh", "3.0")
                .put("dtm", "123456")
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
                .build(), result1);

        Map<String, String> result2 = results.get(1).getMap();
        assertEquals(ImmutableMap.<String, String>builder()
                .put("ti_nm", "name")
                .put("ti_id", "order_id")
                .put("e", "ti")
                .put("co", EXPECTED_CONTEXTS)
                .put("eid", EXPECTED_EVENT_ID)
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("ti_cu", "currency")
                .put("dtm", "123456")
                .put("ttm", "456789")
                .put("tz", "Etc/UTC")
                .put("ti_pr", "1.0")
                .put("ti_qu", "2")
                .put("p", "srv")
                .put("tv", Version.TRACKER)
                .put("ti_ca", "category")
                .put("ti_sk", "sku")
                .build(), result2);
    }

    @Test
    public void testUnstructuredEventWithContext() throws InterruptedException {
        // When
        tracker.track(Unstructured.builder()
                .eventData(new SelfDescribingJson(
                        "payload",
                        ImmutableMap.of("foo", "bar")
                ))
                .customContext(contexts)
                .deviceCreatedTimestamp(123456)
                .trueTimestamp(456789L)
                .eventId(EXPECTED_EVENT_ID)
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        assertEquals(ImmutableMap.<String, String>builder()
                .put("p", "srv")
                .put("tv", Version.TRACKER)
                .put("e", "ue")
                .put("co", EXPECTED_CONTEXTS)
                .put("eid", EXPECTED_EVENT_ID)
                .put("tna", "AF003")
                .put("tz", "Etc/UTC")
                .put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"payload\",\"data\":{\"foo\":\"bar\"}}}")
                .put("dtm", "123456")
                .put("ttm", "456789")
                .put("aid", "cloudfront")
                .build(), result);
    }

    @Test
    public void testUnstructuredEventWithoutContext() throws InterruptedException {
        // When
        tracker.track(Unstructured.builder()
                .eventData(new SelfDescribingJson(
                        "payload",
                        ImmutableMap.of("foo", "baær")
                ))
                .deviceCreatedTimestamp(123456)
                .trueTimestamp(456789L)
                .eventId(EXPECTED_EVENT_ID)
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        assertEquals(ImmutableMap.<String, String>builder()
                .put("p", "srv")
                .put("tv", Version.TRACKER)
                .put("eid", EXPECTED_EVENT_ID)
                .put("e", "ue")
                .put("tna", "AF003")
                .put("tz", "Etc/UTC")
                .put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"payload\",\"data\":{\"foo\":\"baær\"}}}")
                .put("dtm", "123456")
                .put("ttm", "456789")
                .put("aid", "cloudfront")
                .build(), result);
    }

    @Test
    public void testUnstructuredEventWithoutTrueTimestamp() throws InterruptedException {
        // When
        tracker.track(Unstructured.builder()
                .eventData(new SelfDescribingJson(
                        "payload",
                        ImmutableMap.of("foo", "baær")
                ))
                .deviceCreatedTimestamp(123456)
                .eventId(EXPECTED_EVENT_ID)
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        assertEquals(ImmutableMap.<String, String>builder()
                .put("p", "srv")
                .put("tv", Version.TRACKER)
                .put("eid", EXPECTED_EVENT_ID)
                .put("e", "ue")
                .put("tna", "AF003")
                .put("tz", "Etc/UTC")
                .put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"payload\",\"data\":{\"foo\":\"baær\"}}}")
                .put("dtm", "123456")
                .put("aid", "cloudfront")
                .build(), result);
    }

    @Test
    public void testTrackPageView() throws InterruptedException {
        tracker = new Tracker.TrackerBuilder(this.mockEmitter, "AF003", "cloudfront")
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
                .deviceCreatedTimestamp(123456)
                .trueTimestamp(456789L)
                .eventId(EXPECTED_EVENT_ID)
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        assertEquals(ImmutableMap.<String, String>builder()
                .put("dtm", "123456")
                .put("ttm", "456789")
                .put("tz", "Etc/UTC")
                .put("e", "pv")
                .put("page", "title")
                .put("tv", Version.TRACKER)
                .put("p", "srv")
                .put("co", EXPECTED_CONTEXTS)
                .put("eid", EXPECTED_EVENT_ID)
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("refr", "referer")
                .put("url", "url")
                .build(), result);
    }

    @Test
    public void testTrackTwoEvents() throws InterruptedException {
        // When
        tracker.track(PageView.builder()
                .pageUrl("url")
                .pageTitle("title")
                .referrer("referer")
                .deviceCreatedTimestamp(123456)
                .trueTimestamp(456789L)
                .eventId("9783090a-dace-4c85-a75c-933b4596a6c5")
                .build());

        Thread.sleep(500);

        tracker.track(PageView.builder()
                .pageUrl("url")
                .pageTitle("title")
                .referrer("referer")
                .deviceCreatedTimestamp(123456)
                .trueTimestamp(456789L)
                .eventId("39139d43-ea13-4163-8559-adea258bf9c4")
                .build());

        // Then
        Thread.sleep(500);

        List<TrackerPayload> results = mockEmitter.eventList;
        assertEquals(2, results.size());

        Map<String, String> result1 = results.get(0).getMap();
        assertEquals(ImmutableMap.<String, String>builder()
                .put("dtm", "123456")
                .put("ttm", "456789")
                .put("tz", "Etc/UTC")
                .put("e", "pv")
                .put("page", "title")
                .put("tv", Version.TRACKER)
                .put("p", "srv")
                .put("eid", "9783090a-dace-4c85-a75c-933b4596a6c5")
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("refr", "referer")
                .put("url", "url")
                .build(), result1);

        Map<String, String> result2 = results.get(1).getMap();
        assertEquals(ImmutableMap.<String, String>builder()
                .put("dtm", "123456")
                .put("ttm", "456789")
                .put("tz", "Etc/UTC")
                .put("e", "pv")
                .put("page", "title")
                .put("tv", Version.TRACKER)
                .put("p", "srv")
                .put("eid", "39139d43-ea13-4163-8559-adea258bf9c4")
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("refr", "referer")
                .put("url", "url")
                .build(), result2);
    }

    @Test
    public void testTrackScreenView() throws InterruptedException {
        // When
        tracker.track(ScreenView.builder()
                .name("name")
                .id("id")
                .customContext(contexts)
                .deviceCreatedTimestamp(123456)
                .trueTimestamp(456789L)
                .eventId(EXPECTED_EVENT_ID)
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        assertEquals(ImmutableMap.<String, String>builder()
                .put("dtm", "123456")
                .put("ttm", "456789")
                .put("tz", "Etc/UTC")
                .put("e", "ue")
                .put("tv", Version.TRACKER)
                .put("p", "srv")
                .put("co", EXPECTED_CONTEXTS)
                .put("eid", EXPECTED_EVENT_ID)
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/screen_view/jsonschema/1-0-0\",\"data\":{\"id\":\"id\",\"name\":\"name\"}}}")
                .build(), result);
    }

    @Test
    public void testTrackScreenViewWithTimestamp() throws InterruptedException {
        // When
        tracker.track(ScreenView.builder()
                .name("name")
                .id("id")
                .deviceCreatedTimestamp(123456)
                .trueTimestamp(456789L)
                .eventId(EXPECTED_EVENT_ID)
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        assertEquals(ImmutableMap.<String, String>builder()
                .put("dtm", "123456")
                .put("ttm", "456789")
                .put("tz", "Etc/UTC")
                .put("e", "ue")
                .put("tv", Version.TRACKER)
                .put("p", "srv")
                .put("eid", EXPECTED_EVENT_ID)
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/screen_view/jsonschema/1-0-0\",\"data\":{\"id\":\"id\",\"name\":\"name\"}}}")
                .build(), result);
    }

    @Test
    public void testTrackScreenViewWithDefaultContextAndTimestamp() throws InterruptedException {
        // When
        tracker.track(ScreenView.builder()
                .name("name")
                .id("id")
                .customContext(contexts)
                .deviceCreatedTimestamp(123456)
                .trueTimestamp(456789L)
                .eventId(EXPECTED_EVENT_ID)
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        assertEquals(ImmutableMap.<String, String>builder()
                .put("p", "srv")
                .put("tv", Version.TRACKER)
                .put("e", "ue")
                .put("co", EXPECTED_CONTEXTS)
                .put("eid", EXPECTED_EVENT_ID)
                .put("tna", "AF003")
                .put("tz", "Etc/UTC")
                .put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/screen_view/jsonschema/1-0-0\",\"data\":{\"id\":\"id\",\"name\":\"name\"}}}")
                .put("dtm", "123456")
                .put("ttm", "456789")
                .put("aid", "cloudfront")
                .build(), result);
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
                .deviceCreatedTimestamp(123456)
                .trueTimestamp(456789L)
                .eventId(EXPECTED_EVENT_ID)
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        assertEquals(ImmutableMap.<String, String>builder()
                .put("p", "srv")
                .put("tv", Version.TRACKER)
                .put("e", "ue")
                .put("co", EXPECTED_CONTEXTS)
                .put("eid", EXPECTED_EVENT_ID)
                .put("tna", "AF003")
                .put("tz", "Etc/UTC")
                .put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/timing/jsonschema/1-0-0\",\"data\":{\"category\":\"category\",\"label\":\"label\",\"timing\":10,\"variable\":\"variable\"}}}")
                .put("dtm", "123456")
                .put("ttm", "456789")
                .put("aid", "cloudfront")
                .build(), result);
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
                .deviceCreatedTimestamp(123456)
                .trueTimestamp(456789L)
                .eventId(EXPECTED_EVENT_ID)
                .subject(s1)
                .build());

        // Then
        Thread.sleep(500);

        Map<String, String> result = mockEmitter.eventList.get(0).getMap();
        assertEquals(ImmutableMap.<String, String>builder()
                .put("p", "srv")
                .put("ue_pr", "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/timing/jsonschema/1-0-0\",\"data\":{\"category\":\"category\",\"label\":\"label\",\"timing\":10,\"variable\":\"variable\"}}}")
                .put("tv", Version.TRACKER)
                .put("e", "ue")
                .put("ip", "127.0.0.1")
                .put("co", EXPECTED_CONTEXTS)
                .put("eid", EXPECTED_EVENT_ID)
                .put("tna", "AF003")
                .put("tz", "Etc/UTC")
                .put("dtm", "123456")
                .put("ttm", "456789")
                .put("aid", "cloudfront")
                .build(), result);
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
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
        Subject s1 = new Subject.SubjectBuilder().build();
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
