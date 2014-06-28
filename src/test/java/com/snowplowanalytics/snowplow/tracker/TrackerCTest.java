package com.snowplowanalytics.snowplow.tracker;

import junit.framework.TestCase;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
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
        String context = "{'Zone':'USA', 'Phone':'Droid', 'Time':'2pm'}";

        t1.trackEcommerceTransactionItem("IT1023", "SKUVAL",
                29.99, 2, "boots",
                "Shoes", "USD",
                context, 0);
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
        String context = "{'Zone':'USA', 'Phone':'Droid', 'Time':'2pm'}";

        t1.trackEcommerceTransactionItem("IT1023", "SKUVAL",
                29.99, 2, "boots",
                "Shoes", "USD",
                null, 0);
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
        String context = "{'Zone':'USA', 'Phone':'Droid', 'Time':'2pm'}";

        TransactionItem transactionItem = new TransactionItem("123","SKUVAL",100, 2,"Foo","Bar","USD", null);
        List<TransactionItem> lst = new LinkedList<TransactionItem>();
        lst.add(transactionItem);

        t1.trackEcommerceTransaction("OID", 19.99,
                                     "Kohls", 2.50,
                                     1.99, "Chagrin",
                                     "OH", "USA",
                                     "USD", lst, null);
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
        String context = "{'Zone':'USA', 'Phone':'Droid', 'Time':'2pm'}";

        Map<String, Object> dict = new LinkedHashMap<String, Object>();
        dict.put("Iteration", 1);

        t1.trackUnstructuredEvent("Lube Insights", "Data Loop", dict, context);
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
        String context = "{'Zone':'USA', 'Phone':'Droid', 'Time':'2pm'}";

        t1.trackStructuredEvent("Items", "Stuff", "Pants", "Green Blue", 3, "com.snowplow", context);
    }

    @Test
    public void testTrackPageView() throws Exception {
        Tracker t1 = new TrackerC("d3rkrsqld9gmqf.cloudfront.net",
                "com.snowplowanalytics.snowplow.tracker", "JavaPlow", true);
//        Tracker t1 = new TrackerC("segfault.ngrok.com", "com.snowplowanalytics.snowplow.tracker",
//                                  "JavaPlow", true);
        t1.setUserID("User1");
        t1.setLanguage("ital");
        t1.setPlatform("mob");
        t1.setScreenResolution(760, 610);
        String context = "{'Zone':'USA', 'Phone':'Droid', 'Time':'2pm'}";

        t1.trackPageView("www.saggezza.com", "Saggezza Home", "KG", context);
    }

}