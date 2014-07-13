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
        TrackerC t1 = new TrackerC("d3rkrsqld9gmqf.cloudfront.net",
                  "com.snowplowanalytics.snowplow.tracker", "JavaPlow", true);
//        TrackerC t1 = new TrackerC("segfault.ngrok.com", "com.snowplowanalytics.snowplow.tracker",
//                "JavaPlow", true);
        t1.setUserID("User1");
        t1.setLanguage("ital");
        t1.setPlatform("mob");
        t1.setScreenResolution(760, 610);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Zone", "UK");
        map.put("Phone", "iOS");
        map.put("Time", "2pm");

        t1.trackEcommerceTransactionItem("IT1023", "SKUVAL",
                29.99, 2, "boots",
                "Shoes", "USD",
                map, 0, 0);
    }

    @Test
    public void testTrackEcommerceTransactionItem2() throws Exception {
        TrackerC t1 = new TrackerC("d3rkrsqld9gmqf.cloudfront.net",
                "com.snowplowanalytics.snowplow.tracker", "JavaPlow", true);
//        TrackerC t1 = new TrackerC("segfault.ngrok.com", "com.snowplowanalytics.snowplow.tracker",
//                "JavaPlow", true);
        t1.setUserID("User1");
        t1.setLanguage("ital");
        t1.setPlatform("mob");
        t1.setScreenResolution(760, 610);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Zone", "UK");
        map.put("Phone", "Droid");
        map.put("Time", "2pm");

        t1.trackEcommerceTransactionItem("IT1023", "SKUVAL",
                29.99, 2, "boots",
                "Shoes", "USD",
                map, 0, 0);
    }

    @Test
    public void testTrackEcommerceTransaction() throws Exception {
        Tracker t1 = new TrackerC("d3rkrsqld9gmqf.cloudfront.net",
                "com.snowplowanalytics.snowplow.tracker", "JavaPlow", true);
//        Tracker t1 = new TrackerC("segfault.ngrok.com", "com.snowplowanalytics.snowplow.tracker",
//                                  "JavaPlow", true);
        t1.setUserID("User1");
        t1.setLanguage("ital");
        t1.setPlatform("mob");
        t1.setScreenResolution(760, 610);

        TransactionItem transactionItem = new TransactionItem("123","SKUVAL",100, 2,"Foo","Bar","USD", null);
        List<TransactionItem> lst = new LinkedList<TransactionItem>();
        lst.add(transactionItem);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Zone", "USA");
        map.put("Phone", "Droid");
        map.put("Time", "2pm");

        t1.trackEcommerceTransaction("OID", 19.99,
                                     "Kohls", 2.50,
                                     1.99, "Chagrin",
                                     "OH", "USA",
                                     "USD", lst, map, 0);
    }

    @Test
    public void testTrackUnstructEvent() throws Exception {
        Tracker t1 = new TrackerC("d3rkrsqld9gmqf.cloudfront.net",
                "com.snowplowanalytics.snowplow.tracker", "JavaPlow", true);
//        Tracker t1 = new TrackerC("segfault.ngrok.com", "com.snowplowanalytics.snowplow.tracker",
//                                  "JavaPlow", true);
        t1.setUserID("User1");
        t1.setLanguage("ital");
        t1.setPlatform("mob");
        t1.setScreenResolution(760, 610);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Zone", "UK");
        map.put("Phone", "iOS");
        map.put("Time", "2pm");

        Map<String, Object> dict = new LinkedHashMap<String, Object>();
        dict.put("Iteration", 1);

        t1.trackUnstructuredEvent("Lube Insights", "Data Loop", dict, map, 0);
    }

    @Test
    public void testTrackStructEvent() throws Exception {
        Tracker t1 = new TrackerC("d3rkrsqld9gmqf.cloudfront.net",
                "com.snowplowanalytics.snowplow.tracker", "JavaPlow", true);
//        Tracker t1 = new TrackerC("segfault.ngrok.com", "com.snowplowanalytics.snowplow.tracker",
//                                  "JavaPlow", true);
        t1.setUserID("User1");
        t1.setLanguage("ital");
        t1.setPlatform("mob");
        t1.setScreenResolution(760, 610);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Zone", "UK");
        map.put("Phone", "iOS");
        map.put("Time", "2pm");

        t1.trackStructuredEvent("Items", "Stuff", "Pants", "Green Blue", 3, "com.snowplow", map, 0);
    }

    @Test
    public void testTrackPageView() throws Exception {
        Tracker t1 = new TrackerC("d3rkrsqld9gmqf.cloudfront.net",
                "com.snowplowanalytics.snowplow.tracker", "JavaPlow", true);
//        Tracker t1 = new TrackerC("segfault.ngrok.com", "com.snowplowanalytics.snowplow.tracker",
//                                  "JavaPlow", true);
        t1.setUserID("User1");
        t1.setLanguage("eng");
        t1.setPlatform("mob");
        t1.setScreenResolution(760, 610);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Zone", "UK");
        map.put("Phone", "iOS");
        map.put("Time", "2pm");

        t1.trackPageView("www.saggezza.com", "Saggezza Home", "KG", map, 0);
    }

    @Test
    public void testTrackPageViewMapContext() throws Exception {
        Tracker t2 = new TrackerC("d3rkrsqld9gmqf.cloudfront.net",
                "com.snowplowanalytics.snowplow.tracker", "JavaPlow", true);
//        Tracker t2 = new TrackerC("segfault.ngrok.com", "com.snowplowanalytics.snowplow.tracker",
//                "JavaPlow", false);
        t2.setUserID("User2");
        t2.setLanguage("ital");
        t2.setPlatform("mob");
        t2.setScreenResolution(760, 610);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Zone", "USA");
        map.put("Phone", "Droid");
        map.put("Time", "2pm");

        t2.trackPageView("www.saggezza.com", "Saggezza Home", "KG", map, 0);
    }

    @Test
    public void testTrackScreenViewMapContext() throws Exception {
        Tracker t2 = new TrackerC("d3rkrsqld9gmqf.cloudfront.net",
                "com.snowplowanalytics.snowplow.tracker", "JavaPlow", true);
//        Tracker t2 = new TrackerC("segfault.ngrok.com", "com.snowplowanalytics.snowplow.tracker",
//                "JavaPlow", false);
        t2.setUserID("User2");
        t2.setLanguage("ital");
        t2.setPlatform("mob");
        t2.setScreenResolution(760, 610);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Zone", "USA");
        map.put("Phone", "Droid");
        map.put("Time", "2pm");

        t2.trackScreenView("www.saggezza.com", "some_id", map, 0);
    }

}