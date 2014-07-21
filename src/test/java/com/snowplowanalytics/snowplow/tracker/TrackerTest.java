package com.snowplowanalytics.snowplow.tracker;

import com.snowplowanalytics.snowplow.tracker.emitter.HttpMethod;
import com.snowplowanalytics.snowplow.tracker.emitter.HttpOption;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TrackerTest extends TestCase {

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
        Emitter emitter = new Emitter("segfault.ngrok.com", HttpMethod.GET);
        Tracker tracker = new Tracker(emitter, "AF003", true, "cloudfront");
        emitter.setHttpOption(HttpOption.Asynchronous);

        Map<String, String> context = new HashMap<String, String>();
        context.put("some key", "some value");

        tracker.trackPageView("www.mypage.com", "My Page", "www.me.com", context, 0);

        emitter.flushBuffer();
    }
}