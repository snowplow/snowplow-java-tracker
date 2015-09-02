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
package com.snowplowanalytics.snowplow.tracker;

// Java
import java.util.*;
import static java.util.Collections.singletonList;

// JUnit
import com.snowplowanalytics.snowplow.tracker.events.*;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;

// Google
import com.google.common.collect.ImmutableMap;

// Mockito
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.*;

// This library
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;

@RunWith(MockitoJUnitRunner.class)
public class TrackerTest {

    public static final String EXPECTED_BASE64_CONTEXTS = "eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy9jb250ZXh0cy9qc29uc2NoZW1hLzEtMC0xIiwiZGF0YSI6W3sic2NoZW1hIjoic2NoZW1hIiwiZGF0YSI6eyJmb28iOiJiYXIifX1dfQ==";
    public static final String EXPECTED_EVENT_ID = "15e9b149-6029-4f6e-8447-5b9797c9e6be";

    @Mock
    Emitter emitter;

    @Captor
    ArgumentCaptor<TrackerPayload> captor;

    Tracker tracker;
    private List<SelfDescribingJson> contexts;

    @Before
    public void setUp() throws Exception {
        tracker = new Tracker.TrackerBuilder(emitter, "AF003", "cloudfront")
                .subject(new Subject())
                .build();
        tracker.getSubject().setTimezone("Etc/UTC");
        contexts = singletonList(new SelfDescribingJson("schema", ImmutableMap.of("foo", "bar")));
    }

    @Test
    public void testEcommerceEvent() {
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
                .timestamp(123456)
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
                .timestamp(123456)
                .eventId(EXPECTED_EVENT_ID)
                .build());

        // Then
        verify(emitter, times(2)).emit(captor.capture());
        List<TrackerPayload> allValues = captor.getAllValues();

        Map result1 = allValues.get(1).getMap();
        assertEquals(ImmutableMap.<String, Object>builder()
                .put("ti_nm", "name")
                .put("ti_id", "order_id")
                .put("e", "ti")
                .put("cx", EXPECTED_BASE64_CONTEXTS)
                .put("eid", EXPECTED_EVENT_ID)
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("ti_cu", "currency")
                .put("dtm", "123456")
                .put("tz", "Etc/UTC")
                .put("ti_pr", "1.0")
                .put("ti_qu", "2")
                .put("p", "srv")
                .put("tv", Version.TRACKER)
                .put("ti_ca", "category")
                .put("ti_sk", "sku")
                .build(), result1);

        Map result2 = allValues.get(0).getMap();
        assertEquals(ImmutableMap.<String, Object>builder()
                .put("e", "tr")
                .put("tr_cu", "currency")
                .put("cx", EXPECTED_BASE64_CONTEXTS)
                .put("eid", EXPECTED_EVENT_ID)
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("tr_sh", "3.0")
                .put("dtm", "123456")
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
                .build(), result2);
    }

    @Test
    public void testUnstructuredEventWithContext() {
        // When
        tracker.track(Unstructured.builder()
                .eventData(new SelfDescribingJson(
                        "payload",
                        ImmutableMap.of("foo", "bar")
                ))
                .customContext(contexts)
                .timestamp(123456)
                .eventId(EXPECTED_EVENT_ID)
                .build());

        // Then
        verify(emitter).emit(captor.capture());

        Map result = captor.getValue().getMap();
        assertEquals(ImmutableMap.<String, Object>builder()
                .put("p", "srv")
                .put("tv", Version.TRACKER)
                .put("e", "ue")
                .put("cx", EXPECTED_BASE64_CONTEXTS)
                .put("eid", EXPECTED_EVENT_ID)
                .put("tna", "AF003")
                .put("tz", "Etc/UTC")
                .put("ue_px", "eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy91bnN0cnVjdF9ldmVudC9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6eyJzY2hlbWEiOiJwYXlsb2FkIiwiZGF0YSI6eyJmb28iOiJiYXIifX19")
                .put("dtm", "123456")
                .put("aid", "cloudfront")
                .build(), result);
    }

    @Test
    public void testUnstructuredEventWithoutContext() {
        // When
        tracker.track(Unstructured.builder()
                .eventData(new SelfDescribingJson(
                        "payload",
                        ImmutableMap.of("foo", "baær")
                ))
                .timestamp(123456)
                .eventId(EXPECTED_EVENT_ID)
                .build());

        // Then
        verify(emitter).emit(captor.capture());
        Map result = captor.getValue().getMap();
        assertEquals(ImmutableMap.<String, Object>builder()
                .put("p", "srv")
                .put("tv", Version.TRACKER)
                .put("eid", EXPECTED_EVENT_ID)
                .put("e", "ue")
                .put("tna", "AF003")
                .put("tz", "Etc/UTC")
                .put("ue_px", "eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy91bnN0cnVjdF9ldmVudC9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6eyJzY2hlbWEiOiJwYXlsb2FkIiwiZGF0YSI6eyJmb28iOiJiYcOmciJ9fX0=")
                .put("dtm", "123456")
                .put("aid", "cloudfront")
                .build(), result);
    }

    @Test
    public void testTrackPageView() {
        // When
        tracker.track(PageView.builder()
                .pageUrl("url")
                .pageTitle("title")
                .referrer("referer")
                .customContext(contexts)
                .timestamp(123456)
                .eventId(EXPECTED_EVENT_ID)
                .build());

        // Then
        verify(emitter).emit(captor.capture());
        Map result = captor.getValue().getMap();
        assertEquals(ImmutableMap.<String, Object>builder()
                .put("dtm", "123456")
                .put("tz", "Etc/UTC")
                .put("e", "pv")
                .put("page", "title")
                .put("tv", Version.TRACKER)
                .put("p", "srv")
                .put("cx", EXPECTED_BASE64_CONTEXTS)
                .put("eid", EXPECTED_EVENT_ID)
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("refr", "referer")
                .put("url", "url")
                .build(), result);
    }

    @Test
    public void testTrackScreenView() {
        // When
        tracker.track(ScreenView.builder()
                .name("name")
                .id("id")
                .customContext(contexts)
                .timestamp(123456)
                .eventId(EXPECTED_EVENT_ID)
                .build());

        // Then
        verify(emitter).emit(captor.capture());
        Map result = captor.getValue().getMap();
        assertEquals(ImmutableMap.<String, Object>builder()
                .put("dtm", "123456")
                .put("tz", "Etc/UTC")
                .put("e", "ue")
                .put("tv", Version.TRACKER)
                .put("p", "srv")
                .put("cx", EXPECTED_BASE64_CONTEXTS)
                .put("eid", EXPECTED_EVENT_ID)
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("ue_px", "eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy91bnN0cnVjdF9ldmVudC9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy9zY3JlZW5fdmlldy9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6eyJlaWQiOiIxNWU5YjE0OS02MDI5LTRmNmUtODQ0Ny01Yjk3OTdjOWU2YmUiLCJuYW1lIjoibmFtZSIsImlkIjoiaWQiLCJkdG0iOiIxMjM0NTYifX19")
                .build(), result);
    }

    @Test
    public void testTrackScreenViewWithDefaultContextAndTimestamp() {
        // When
        tracker.track(ScreenView.builder()
                .name("name")
                .id("id")
                .timestamp(123456)
                .eventId(EXPECTED_EVENT_ID)
                .build());

        // Then
        verify(emitter).emit(captor.capture());
        Map result = captor.getValue().getMap();
        assertEquals(ImmutableMap.<String, Object>builder()
                .put("dtm", "123456")
                .put("tz", "Etc/UTC")
                .put("e", "ue")
                .put("tv", Version.TRACKER)
                .put("p", "srv")
                .put("eid", EXPECTED_EVENT_ID)
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("ue_px", "eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy91bnN0cnVjdF9ldmVudC9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy9zY3JlZW5fdmlldy9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6eyJlaWQiOiIxNWU5YjE0OS02MDI5LTRmNmUtODQ0Ny01Yjk3OTdjOWU2YmUiLCJuYW1lIjoibmFtZSIsImlkIjoiaWQiLCJkdG0iOiIxMjM0NTYifX19")
                .build(), result);
    }


    @Test
    public void testTrackScreenViewWithTimestamp() {
        // When
        tracker.track(ScreenView.builder()
                .name("name")
                .id("id")
                .customContext(contexts)
                .timestamp(123456)
                .eventId(EXPECTED_EVENT_ID)
                .build());

        // Then
        verify(emitter).emit(captor.capture());
        Map result = captor.getValue().getMap();
        assertEquals(ImmutableMap.<String, Object>builder()
                .put("p", "srv")
                .put("tv", Version.TRACKER)
                .put("e", "ue")
                .put("cx", EXPECTED_BASE64_CONTEXTS)
                .put("eid", EXPECTED_EVENT_ID)
                .put("tna", "AF003")
                .put("tz", "Etc/UTC")
                .put("ue_px", "eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy91bnN0cnVjdF9ldmVudC9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy9zY3JlZW5fdmlldy9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6eyJlaWQiOiIxNWU5YjE0OS02MDI5LTRmNmUtODQ0Ny01Yjk3OTdjOWU2YmUiLCJuYW1lIjoibmFtZSIsImlkIjoiaWQiLCJkdG0iOiIxMjM0NTYifX19")
                .put("dtm", "123456")
                .put("aid", "cloudfront")
                .build(), result);
    }

    @Test
    public void testTrackTimingWithCategory() {
        // When
        tracker.track(TimingWithCategory.builder()
                .category("category")
                .label("label")
                .variable("variable")
                .timing(10)
                .customContext(contexts)
                .timestamp(123456)
                .eventId(EXPECTED_EVENT_ID)
                .build());

        // Then
        verify(emitter).emit(captor.capture());
        Map result = captor.getValue().getMap();
        assertEquals(ImmutableMap.<String, Object>builder()
                .put("p", "srv")
                .put("tv", Version.TRACKER)
                .put("e", "ue")
                .put("cx", EXPECTED_BASE64_CONTEXTS)
                .put("eid", EXPECTED_EVENT_ID)
                .put("tna", "AF003")
                .put("tz", "Etc/UTC")
                .put("ue_px", "eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy91bnN0cnVjdF9ldmVudC9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy90aW1pbmcvanNvbnNjaGVtYS8xLTAtMCIsImRhdGEiOnsiZWlkIjoiMTVlOWIxNDktNjAyOS00ZjZlLTg0NDctNWI5Nzk3YzllNmJlIiwidGltaW5nIjoiMTAiLCJ2YXJpYWJsZSI6InZhcmlhYmxlIiwibGFiZWwiOiJsYWJlbCIsImNhdGVnb3J5IjoiY2F0ZWdvcnkiLCJkdG0iOiIxMjM0NTYifX19")
                .put("dtm", "123456")
                .put("aid", "cloudfront")
                .build(), result);
    }

    @Test
    public void testDefaultPlatform() throws Exception {
        Subject subject = new Subject();
        Tracker tracker = new Tracker.TrackerBuilder(emitter, "AF003", "cloudfront")
                .subject(subject)
                .build();

        assertEquals(DevicePlatform.ServerSideApp, tracker.getPlatform());
    }

    @Test
    public void testSetPlatform() throws Exception {
        Subject subject = new Subject();
        Tracker tracker = new Tracker.TrackerBuilder(emitter, "AF003", "cloudfront")
                .subject(subject)
                .build();

        tracker.setPlatform(DevicePlatform.ConnectedTV);

        assertEquals(DevicePlatform.ConnectedTV, tracker.getPlatform());
    }

    @Test
    public void testSetSubject() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));

        Subject s1 = new Subject();
        Tracker tracker = new Tracker.TrackerBuilder(emitter, "AF003", "cloudfront")
                .subject(s1)
                .build();

        Subject s2 = new Subject();
        s2.setColorDepth(24);
        tracker.setSubject(s2);

        Map<String, String> subjectPairs = new HashMap<>();
        subjectPairs.put("tz", "Etc/UTC");
        subjectPairs.put("cd", "24");

        assertEquals(subjectPairs, tracker.getSubject().getSubject());
    }
}
