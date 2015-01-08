package com.snowplowanalytics.snowplow.tracker;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.snowplowanalytics.snowplow.tracker.emitter.BufferOption;
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.emitter.RequestMethod;
import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TrackerTest {

    @Mock
    Emitter emitter;
    
    @Mock
    Provider provider;
        
    @Captor
    ArgumentCaptor<Map<String, Object>> captor;
    
    Tracker tracker;

    @Before
    public void setUp() throws Exception {
        when(provider.getTimestamp()).thenReturn(123456L);
        when(provider.getTransactionId()).thenReturn(1);
        when(provider.getUUID()).thenReturn(UUID.fromString("15e9b149-6029-4f6e-8447-5b9797c9e6be"));

        tracker = new Tracker(emitter, new Subject(), "AF003", "cloudfront");
        tracker.setProvider(provider);

    }

    @Test
    public void testEcommerceEvent() {

        // Given
        List<SchemaPayload> contexts = Arrays.asList(
                new SchemaPayload()
                        .setSchema("schema")
                        .setData(ImmutableMap.of("foo", "bar"))

        );

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
                .put("cx", "eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy9jb250ZXh0cy9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6W3sic2NoZW1hIjoic2NoZW1hIiwiZGF0YSI6eyJmb28iOiJiYXIifX1dfQ==")
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("ti_cu", "currency")
                .put("eid", "15e9b149-6029-4f6e-8447-5b9797c9e6be")
                .put("dtm", "123456")
                .put("tz", "Europe/Paris")
                .put("ti_pr", "1.0")
                .put("ti_qu", "2.0")
                .put("p", "pc")
                .put("tv", "java-0.6.0")
                .put("ti_ca", "category")
                .put("ti_sk", "sku")
                .build(), allValues.get(0));
        
        assertEquals(ImmutableMap.<String, Object>builder()
                .put("e", "tr")
                .put("tr_cu", "currency")
                .put("cx", "eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy9jb250ZXh0cy9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6W3sic2NoZW1hIjoic2NoZW1hIiwiZGF0YSI6eyJmb28iOiJiYXIifX1dfQ==")
                .put("tna", "AF003")
                .put("aid", "cloudfront")
                .put("eid", "15e9b149-6029-4f6e-8447-5b9797c9e6be")
                .put("tr_sh", "3.0")
                .put("dtm", "123456")
                .put("tz", "Europe/Paris")
                .put("tr_co", "country")
                .put("tv", "java-0.6.0")
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
    public void testUnstructuredEvent() {

        // Given
        ArrayList<SchemaPayload> context = Lists.newArrayList(
                new SchemaPayload()
                        .setSchema("context")
                        .setData(ImmutableMap.of("bar", "foo"))
        );
        
        // When
        tracker.trackUnstructuredEvent(new SchemaPayload()
        .setData(ImmutableMap.of("foo", "bar"))
        .setSchema("payload"), context);
        
        // Then
        verify(emitter).addToBuffer(captor.capture());
        assertEquals(ImmutableMap.<String, Object>builder()
            .put("dtm", "123456")
            .put("tz", "Europe/Paris")
            .put("e", "ue")
            .put("tv", "java-0.6.0")
            .put("p", "pc")
            .put("cx", "eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy9jb250ZXh0cy9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6W3sic2NoZW1hIjoiY29udGV4dCIsImRhdGEiOnsiYmFyIjoiZm9vIn19XX0=")
            .put("tna", "AF003")
            .put("aid", "cloudfront")
            .put("ue_px", "eyJzY2hlbWEiOiJpZ2x1OmNvbS5zbm93cGxvd2FuYWx5dGljcy5zbm93cGxvdy91bnN0cnVjdF9ldmVudC9qc29uc2NoZW1hLzEtMC0wIiwiZGF0YSI6eyJzY2hlbWEiOiJwYXlsb2FkIiwiZGF0YSI6eyJmb28iOiJiYXIifX19")
            .put("eid", "15e9b149-6029-4f6e-8447-5b9797c9e6be")
            .build(), captor.getValue());
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


    @Test
    public void testTrackScreenView() throws Exception {
        Subject subject = new Subject();
        subject.setViewPort(320, 480);
        Tracker tracker = new Tracker(emitter, subject, "AF003", "cloudfront");
        emitter.setRequestMethod(RequestMethod.Asynchronous);
        emitter.setBufferOption(BufferOption.Instant);

        SchemaPayload context = new SchemaPayload();
        Map<String, String> someContext = new HashMap<String, String>();
        someContext.put("someContextKey", "testTrackPageView2");
        context.setSchema("iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0");
        context.setData(someContext);
        ArrayList<SchemaPayload> contextList = new ArrayList<SchemaPayload>();
        contextList.add(context);

        tracker.trackScreenView(null, "screen_1", contextList, 0);
    }
}