package com.snowplowanalytics.snowplow.tracker;

import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.emitter.HttpMethod;
import com.snowplowanalytics.snowplow.tracker.emitter.RequestMethod;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TrackerTest extends TestCase {

    private static String localURL = "segfault.ngrok.com";
    private static String testURL = "d3rkrsqld9gmqf.cloudfront.net";

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
        Emitter emitter = new Emitter(localURL, HttpMethod.GET);
        Subject subject = new Subject();
        subject.setViewPort(320, 480);
        Tracker tracker = new Tracker(emitter, subject, "AF003", "cloudfront", false);
        emitter.setRequestMethod(RequestMethod.Asynchronous);

        Map<String, String> context = new HashMap<String, String>();
        ArrayList<Map> contextList = new ArrayList<Map>();
        context.put("somekey", "somevalue");
        contextList.add(context);

        tracker.trackPageView("www.mypage.com", "My Page", "www.me.com", contextList, 0);

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