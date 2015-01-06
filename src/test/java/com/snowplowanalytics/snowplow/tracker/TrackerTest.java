package com.snowplowanalytics.snowplow.tracker;

import com.snowplowanalytics.snowplow.tracker.emitter.BufferOption;
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.emitter.HttpMethod;
import com.snowplowanalytics.snowplow.tracker.emitter.RequestMethod;
import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class TrackerTest {

    private static String TESTURL = "d3rkrsqld9gmqf.cloudfront.net";

    @Test
    public void testDefaultPlatform() throws Exception {
        Emitter emitter = new Emitter(TESTURL, HttpMethod.POST);
        Subject subject = new Subject();
        Tracker tracker = new Tracker(emitter, subject, "AF003", "cloudfront", false);
        assertEquals(DevicePlatform.Desktop, tracker.getPlatform());
    }

    @Test
    public void testSetPlatform() throws Exception {
        Emitter emitter = new Emitter(TESTURL, HttpMethod.POST);
        Subject subject = new Subject();
        Tracker tracker = new Tracker(emitter, subject, "AF003", "cloudfront", false);
        tracker.setPlatform(DevicePlatform.ConnectedTV);
        assertEquals(DevicePlatform.ConnectedTV, tracker.getPlatform());
    }

    @Test
    public void testSetSubject() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
        Emitter emitter = new Emitter(TESTURL, HttpMethod.POST);
        Subject s1 = new Subject();
        Tracker tracker = new Tracker(emitter, s1, "AF003", "cloudfront", false);
        Subject s2 = new Subject();
        s2.setColorDepth(24);
        tracker.setSubject(s2);
        Map<String, String> subjectPairs = new HashMap<String, String>();
        subjectPairs.put("tz", "Etc/UTC");
        subjectPairs.put("cd", "24");
        assertEquals(subjectPairs, tracker.getSubject().getSubject());
    }

    @Test
    public void testSetSchema() throws Exception {

    }

    @Test
    public void testTrackPageView() throws Exception {

    }

    @Test
    public void testTrackPageView1() throws Exception {

    }

    @Test
    public void testTrackPageView2() throws Exception {

    }

    @Test
    public void testTrackPageView3() throws Exception {
        Emitter emitter = new Emitter(TESTURL, HttpMethod.POST);
        Subject subject = new Subject();
        subject.setViewPort(320, 480);
        Tracker tracker = new Tracker(emitter, subject, "AF003", "cloudfront", false);
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
    public void testTrackStructuredEvent() throws Exception {

    }

    @Test
    public void testTrackStructuredEvent1() throws Exception {

    }

    @Test
    public void testTrackStructuredEvent2() throws Exception {

    }

    @Test
    public void testTrackStructuredEvent3() throws Exception {

    }

    @Test
    public void testTrackUnstructuredEvent() throws Exception {

    }

    @Test
    public void testTrackUnstructuredEvent1() throws Exception {

    }

    @Test
    public void testTrackUnstructuredEvent2() throws Exception {

    }

    @Test
    public void testTrackUnstructuredEvent3() throws Exception {

    }

    @Test
    public void testTrackEcommerceTransactionItem() throws Exception {

    }

    @Test
    public void testTrackEcommerceTransaction() throws Exception {
        Emitter emitter = new Emitter(TESTURL, HttpMethod.POST);
        Tracker tracker = new Tracker(emitter, "AF003", "cloudfront", false);
        emitter.setRequestMethod(RequestMethod.Asynchronous);

        SchemaPayload context = new SchemaPayload();
        Map<String, String> someContext = new HashMap<String, String>();
        someContext.put("someContextKey", "testTrackPageView2");
        context.setSchema("iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0");
        context.setData(someContext);
        ArrayList<SchemaPayload> contextList = new ArrayList<SchemaPayload>();
        contextList.add(context);

        TransactionItem transactionItem = new TransactionItem("order-8", "no_sku",
                34.0, 1, "Big Order", "Food", "USD", contextList);
        LinkedList<TransactionItem> transactionItemLinkedList = new LinkedList<TransactionItem>();
        transactionItemLinkedList.add(transactionItem);
        tracker.trackEcommerceTransaction("order-7", 25.0, "no_affiliate", 0.0, 0.0, "Dover",
                "Delaware", "US", "USD", transactionItemLinkedList);

        emitter.flushBuffer();
    }

    @Test
    public void testTrackEcommerceTransaction1() throws Exception {

    }

    @Test
    public void testTrackEcommerceTransaction2() throws Exception {

    }

    @Test
    public void testTrackEcommerceTransaction3() throws Exception {

    }

    @Test
    public void testTrackScreenView() throws Exception {
        Emitter emitter = new Emitter(TESTURL, HttpMethod.POST);
        Subject subject = new Subject();
        subject.setViewPort(320, 480);
        Tracker tracker = new Tracker(emitter, subject, "AF003", "cloudfront", false);
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

    @Test
    public void testTrackScreenView1() throws Exception {

    }

    @Test
    public void testTrackScreenView2() throws Exception {

    }

    @Test
    public void testTrackScreenView3() throws Exception {

    }
}