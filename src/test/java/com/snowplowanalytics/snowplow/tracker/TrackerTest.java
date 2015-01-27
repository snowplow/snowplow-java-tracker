package com.snowplowanalytics.snowplow.tracker;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.emitter.RequestMethod;
import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TrackerTest {

    public static final String EXPECTED_BASE64_CONTEXTS = "eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy9jb250ZXh0cy9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6W3sic2NoZW1hIjoic2NoZW1hIiwiZGF0YSI6eyJmb28iOiJiYXIifX1dfQ==";
    public static final String EXPECTED_EVENT_ID = "15e9b149-6029-4f6e-8447-5b9797c9e6be";
    
    @Mock
    Emitter emitter;

    @Mock
    Provider provider;

    @Captor
    ArgumentCaptor<Map<String, Object>> captor;

    Tracker tracker;
    private List<SchemaPayload> contexts;

    
    @Before
    public void setUp() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
        when(provider.getTimestamp()).thenReturn(123456L);
        when(provider.getTransactionId()).thenReturn(1);
        when(provider.getUUID()).thenReturn(UUID.fromString("15e9b149-6029-4f6e-8447-5b9797c9e6be"));

        tracker = new Tracker(emitter, new Subject(), "AF003", "cloudfront");
        tracker.setProvider(provider);

        contexts = Arrays.asList(
                new SchemaPayload()
                        .setSchema("schema")
                        .setData(ImmutableMap.of("foo", "bar"))

        );
    }

    @Test
    public void testEcommerceEvent() {

        // Given
        List<TransactionItem> items = Arrays.<TransactionItem>asList(
                new TransactionItem("order_id", "sku", 1.0, 2, "name", "category", "currency", "12346", contexts)
        );

        // When
        tracker.trackEcommerceTransaction("order_id", 1.0, "affiliation", 2.0, 3.0, "city", "state", "country", "currency", items, contexts);

        // Then
        verify(emitter, times(2)).addToBuffer(captor.capture());
        List<Map<String, Object>> allValues = captor.getAllValues();
        assertEquals(ImmutableMap.<String, Object>builder()
                .put("ti_nm", "name")
                .put("ti_id", "order_id")
                .put("e", "ti")
                .put("cx", EXPECTED_BASE64_CONTEXTS)
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("ti_cu", "currency")
                .put("eid", EXPECTED_EVENT_ID)
                .put("dtm", "123456")
                .put("tz", "Etc/UTC")
                .put("ti_pr", "1.0")
                .put("ti_qu", "2.0")
                .put("p", "pc")
                .put("tv", Version.TRACKER)
                .put("ti_ca", "category")
                .put("ti_sk", "sku")
                .build(), allValues.get(0));

        assertEquals(ImmutableMap.<String, Object>builder()
                .put("e", "tr")
                .put("tr_cu", "currency")
                .put("cx", EXPECTED_BASE64_CONTEXTS)
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("eid", EXPECTED_EVENT_ID)
                .put("tr_sh", "3.0")
                .put("dtm", "123456")
                .put("tz", "Etc/UTC")
                .put("tr_co", "country")
                .put("tv", Version.TRACKER)
                .put("p", "pc")
                .put("tr_tx", "2.0")
                .put("tr_af", "affiliation")
                .put("tr_id", "order_id")
                .put("tr_tt", "1.0")
                .put("tr_ci", "city")
                .put("tr_st", "state")
                .build(), allValues.get(1));
    }

    @Test
    public void testUnstructuredEventWithContext() {

        // When
        tracker.trackUnstructuredEvent(new SchemaPayload()
                .setData(ImmutableMap.of("foo", "bar"))
                .setSchema("payload"), contexts);

        // Then
        verify(emitter).addToBuffer(captor.capture());
        assertEquals(ImmutableMap.<String, Object>builder()
                .put("dtm", "123456")
                .put("tz", "Etc/UTC")
                .put("e", "ue")
                .put("tv", Version.TRACKER)
                .put("p", "pc")
                .put("cx", EXPECTED_BASE64_CONTEXTS)
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("ue_px", "eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy91bnN0cnVjdF9ldmVudC9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6eyJzY2hlbWEiOiJwYXlsb2FkIiwiZGF0YSI6eyJmb28iOiJiYXIifX19")
                .put("eid", EXPECTED_EVENT_ID)
                .build(), captor.getValue());
    }

    @Test
    public void testUnstructuredEventWithoutContext() {

        // When
        tracker.trackUnstructuredEvent(new SchemaPayload()
                .setData(ImmutableMap.of("foo", "baær"))
                .setSchema("payload"));

        // Then
        verify(emitter).addToBuffer(captor.capture());
        assertEquals(ImmutableMap.<String, Object>builder()
                .put("dtm", "123456")
                .put("tz", "Etc/UTC")
                .put("e", "ue")
                .put("tv", Version.TRACKER)
                .put("p", "pc")
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("ue_px", "eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy91bnN0cnVjdF9ldmVudC9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6eyJzY2hlbWEiOiJwYXlsb2FkIiwiZGF0YSI6eyJmb28iOiJiYcOmciJ9fX0=")
                .put("eid", "15e9b149-6029-4f6e-8447-5b9797c9e6be")
                .build(), captor.getValue());
    }

    @Test
    public void testTrackPageView() {
        
        // When
        tracker.trackPageView("url", "title", "referer", contexts);
        
        // Then
        verify(emitter).addToBuffer(captor.capture());
        assertEquals(ImmutableMap.<String, Object>builder()
                .put("dtm", "123456")
                .put("tz", "Etc/UTC")
                .put("e", "pv")
                .put("page", "title")
                .put("tv", Version.TRACKER)
                .put("p", "pc")
                .put("cx", EXPECTED_BASE64_CONTEXTS)
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("eid", EXPECTED_EVENT_ID)
                .put("refr", "referer")
                .put("url", "url")
                .build(), captor.getValue());
    }

    @Test
    public void testTrackScreenView() {
        
        // When
        tracker.trackScreenView("name", "id", contexts);
        
        // Then
        verify(emitter).addToBuffer(captor.capture());
        assertEquals(ImmutableMap.<String, Object>builder()
                .put("dtm", "123456")
                .put("tz", "Etc/UTC")
                .put("e", "ue")
                .put("tv", Version.TRACKER)
                .put("p", "pc")
                .put("cx", EXPECTED_BASE64_CONTEXTS)
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("ue_px", "eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy91bnN0cnVjdF9ldmVudC9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy9zY3JlZW5fdmlldy9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6eyJpZCI6ImlkIiwibmFtZSI6Im5hbWUifX19")
                .put("eid", EXPECTED_EVENT_ID)
                .build(), captor.getValue());
    }

    @Test
    public void testTrackScreenViewWithDefaultContextAndTimestamp() {

        // When
        tracker.trackScreenView("name", "id");

        // Then
        verify(emitter).addToBuffer(captor.capture());
        assertEquals(ImmutableMap.<String, Object>builder()
                .put("dtm", "123456")
                .put("tz", "Etc/UTC")
                .put("e", "ue")
                .put("tv", Version.TRACKER)
                .put("p", "pc")
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("ue_px", "eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy91bnN0cnVjdF9ldmVudC9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy9zY3JlZW5fdmlldy9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6eyJpZCI6ImlkIiwibmFtZSI6Im5hbWUifX19")
                .put("eid", EXPECTED_EVENT_ID)
                .build(), captor.getValue());
    }


    @Test
    public void testTrackScreenViewWithTimestamp() {

        // When
        tracker.trackScreenView("name", "id", contexts, 654321L);

        // Then
        verify(emitter).addToBuffer(captor.capture());
        assertEquals(ImmutableMap.<String, Object>builder()
                .put("dtm", "654321")
                .put("tz", "Etc/UTC")
                .put("e", "ue")
                .put("tv", Version.TRACKER)
                .put("p", "pc")
                .put("cx", EXPECTED_BASE64_CONTEXTS)
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("ue_px", "eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy91bnN0cnVjdF9ldmVudC9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy9zY3JlZW5fdmlldy9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6eyJpZCI6ImlkIiwibmFtZSI6Im5hbWUifX19")
                .put("eid", EXPECTED_EVENT_ID)
                .build(), captor.getValue());
    }
    
    @Test
    public void testCompletePayloadSetCustomTrackerVersion() {
        
        // Given
        Map<String, Object> data = new HashMap<String, Object>(ImmutableMap.<String, Object>of("foo", "bar"));

        // When
        tracker.setTrackerVersion("abc");
        
        // Then
        Map<String, Object> payload = tracker.completePayload(data, Lists.<SchemaPayload>newArrayList(), 123465L);
        assertTrue(payload.containsKey("tv"));
        assertEquals("abc", payload.get("tv"));
    }

    
    @Test
    public void testDefaultPlatform() throws Exception {
        Subject subject = new Subject();
        Tracker tracker = new Tracker(emitter, subject, "AF003", "cloudfront");
        assertEquals(DevicePlatform.Desktop, tracker.getPlatform());
    }

    @Test
    public void testSetPlatform() throws Exception {
        Subject subject = new Subject();
        Tracker tracker = new Tracker(emitter, subject, "AF003", "cloudfront");
        tracker.setPlatform(DevicePlatform.ConnectedTV);
        assertEquals(DevicePlatform.ConnectedTV, tracker.getPlatform());
    }

    @Test
    public void testSetSubject() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
        Subject s1 = new Subject();
        Tracker tracker = new Tracker(emitter, s1, "AF003", "cloudfront");
        Subject s2 = new Subject();
        s2.setColorDepth(24);
        tracker.setSubject(s2);
        Map<String, String> subjectPairs = new HashMap<String, String>();
        subjectPairs.put("tz", "Etc/UTC");
        subjectPairs.put("cd", "24");
        assertEquals(subjectPairs, tracker.getSubject().getSubject());
    }

    @Test
    public void testTrackPageView3() throws Exception {
        Subject subject = new Subject();
        subject.setViewPort(320, 480);
        Tracker tracker = new Tracker(emitter, subject, "AF003", "cloudfront");
        emitter.setRequestMethod(RequestMethod.Asynchronous);

        SchemaPayload context = new SchemaPayload();
        Map<String, String> someContext = new HashMap<String, String>();
        someContext.put("someContextKey", "testTrackPageView3");
        context.setSchema("iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0");
        context.setData(someContext);
        ArrayList<SchemaPayload> contextList = new ArrayList<SchemaPayload>();
        contextList.add(context);

        tracker.trackPageView("www.mypage.com", "My Page", "www.me.com", contextList);

        emitter.flushBuffer();
    }

}