package com.snowplowanalytics.snowplow.tracker;

import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.emitter.HttpMethod;
import com.snowplowanalytics.snowplow.tracker.emitter.RequestMethod;
import com.snowplowanalytics.snowplow.tracker.payload.Payload;
import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TrackerTest extends TestCase {

    private static String testURL = "segfault.ngrok.com";
//    private static String testURL = "d3rkrsqld9gmqf.cloudfront.net";

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
        Emitter emitter = new Emitter(testURL, HttpMethod.POST);
        Subject subject = new Subject();
        subject.setViewPort(320, 480);
        Tracker tracker = new Tracker(emitter, subject, "AF003", "cloudfront");
        emitter.setRequestMethod(RequestMethod.Asynchronous);

        SchemaPayload context = new SchemaPayload();
        Map<String, String> someContext = new HashMap<String, String>();
        someContext.put("someContextKey", "someContextValue");
        ArrayList<Map> contextList = new ArrayList<Map>();
        context.setSchema("setse");
        context.setData(someContext);
        contextList.add(context.getMap());

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