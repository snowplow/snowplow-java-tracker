package com.snowplowanalytics.snowplow.tracker;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TrackerCTest extends TestCase {

    @Test
    public void testTrackEcommerceTransactionItem() throws Exception {
//        Tracker t1 = new TrackerC("d3rkrsqld9gmqf.cloudfront.net",
//                "com.snowplowanalytics.snowplow.tracker", "JavaPlow", "com.snowplow", true, true);
        Tracker t1 = new TrackerC("segfault.ngrok.com", "com.snowplowanalytics.snowplow.tracker",
                                  "JavaPlow", true, true);
        t1.setUserID("User1");
        t1.setLanguage("ital");
        t1.setPlatform("mob");
        t1.setScreenResolution(760, 610);
        String context = "{'Zone':'USA', 'Phone':'Droid', 'Time':'2pm'}";

        t1.trackEcommerceTransactionItem("IT1023", "SKUVAL",
                                         29.99, 2, "boots",
                                         "Shoes", "USD",
                                         null, context);
    }

    @Test
    public void testTrackEcommerceTransactionItem2() throws Exception {
        Tracker t1 = new TrackerC("segfault.ngrok.com", "com.snowplowanalytics.snowplow.tracker",
                                  "JavaPlow", true, true);
        t1.setUserID("User1");
        t1.setLanguage("ital");
        t1.setPlatform("mob");
        t1.setScreenResolution(760, 610);
        String context = "{'Zone':'USA', 'Phone':'Droid', 'Time':'2pm'}";

        t1.trackEcommerceTransactionItem("IT1023", "SKUVAL",
                                         29.99, 2, "boots",
                                         "Shoes", "USD",
                                         null, null);
    }

    @Test
    public void testTrackEcommerceTransaction() throws Exception {
        Tracker t1 = new TrackerC("segfault.ngrok.com", "com.snowplowanalytics.snowplow.tracker",
                                  "JavaPlow", true, true);
        t1.setUserID("User1");
        t1.setLanguage("ital");
        t1.setPlatform("mob");
        t1.setScreenResolution(760, 610);
        String context = "{'Zone':'USA', 'Phone':'Droid', 'Time':'2pm'}";

        Map<String,String> items = new HashMap<String, String>();
        items.put("sku", "SKUVAL"); items.put("quantity","2"); items.put("price","19.99");
        List<Map<String,String>> lst = new LinkedList<Map<String, String>>();
        lst.add(items);

        t1.trackEcommerceTransaction("OID", 19.99,
                                     "Kohls", 2.50,
                                     1.99, "Chagrin",
                                     "OH", "USA",
                                     "USD", lst, context);
    }

    @Test
    public void testTrackUnstructEvent() throws Exception {
        Tracker t1 = new TrackerC("segfault.ngrok.com", "com.snowplowanalytics.snowplow.tracker",
                                  "JavaPlow", true, true);
        t1.setUserID("User1");
        t1.setLanguage("ital");
        t1.setPlatform("mob");
        t1.setScreenResolution(760, 610);
        String context = "{'Zone':'USA', 'Phone':'Droid', 'Time':'2pm'}";

        Map<String, Object> dict = new LinkedHashMap<String, Object>();
        dict.put("Iteration", 1);

        t1.trackUnstructEvent("Lube Insights", "Data Loop", dict, context);
    }

    @Test
    public void testTrackStructEvent() throws Exception {
        Tracker t1 = new TrackerC("segfault.ngrok.com", "com.snowplowanalytics.snowplow.tracker",
                                  "JavaPlow", true, true);
        t1.setUserID("User1");
        t1.setLanguage("ital");
        t1.setPlatform("mob");
        t1.setScreenResolution(760, 610);
        String context = "{'Zone':'USA', 'Phone':'Droid', 'Time':'2pm'}";

        t1.trackStructEvent("Items", "Stuff", "Pants", "Green Blue", 3, "com.snowplow", context);
    }

    @Test
    public void testTrackPageView() throws Exception {
        Tracker t1 = new TrackerC("segfault.ngrok.com", "com.snowplowanalytics.snowplow.tracker",
                                  "JavaPlow", true, true);
        t1.setUserID("User1");
        t1.setLanguage("ital");
        t1.setPlatform("mob");
        t1.setScreenResolution(760, 610);
        String context = "{'Zone':'USA', 'Phone':'Droid', 'Time':'2pm'}";

        t1.trackPageView("www.saggezza.com", "Saggezza Home", "KG", null);
    }
}