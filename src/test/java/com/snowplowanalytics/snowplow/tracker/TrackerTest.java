package com.snowplowanalytics.snowplow.tracker;

import com.snowplowanalytics.snowplow.tracker.emitter.BufferOption;
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.emitter.HttpMethod;
import com.snowplowanalytics.snowplow.tracker.emitter.RequestMethod;
import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class TrackerTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private static String TESTURL = "localhost:8080";

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
        subject.setTimezone("Etc/UTC");
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

        verify(postRequestedFor(urlEqualTo("/com.snowplowanalytics.snowplow/tp2"))
                .withHeader("Content-Type", equalTo("application/json; charset=utf-8"))
                .withRequestBody(equalToJson("{\"schema\":\"iglu:com.snowplowanalytics." +
                        "snowplow/payload_data/jsonschema/1-0-0\",\"data\":[{\"e\":\"pv\"," +
                        "\"url\":\"www.mypage.com\",\"page\":\"My Page\",\"refr\":" +
                        "\"www.me.com\",\"aid\":\"cloudfront\",\"tna\":\"AF003\"," +
                        "\"tv\":\"java-0.7.0\",\"co\":\"{\\\"schema\\\":" +
                        "\\\"iglu:com.snowplowanalytics.snowplow/contexts/jsonschema/1-0-0\\\"," +
                        "\\\"data\\\":[{\\\"schema\\\":\\\"iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0\\\"," +
                        "\\\"data\\\":{\\\"someContextKey\\\":\\\"testTrackPageView3\\\"}}]}\"," +
                        "\"tz\":\"Etc/UTC\",\"p\":\"pc\",\"vp\":\"320x480\"}]}",
                        JSONCompareMode.LENIENT)));
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

        // Verifying this JSON:
        // {
        //     "schema": "iglu:com.snowplowanalytics.snowplow/payload_data/jsonschema/1-0-0",
        //     "data": [{
        //         "e": "ti",
        //         "ti_id": "order-8",
        //         "ti_sk": "no_sku",
        //         "ti_nm": "Big Order",
        //         "ti_ca": "Food",
        //         "ti_pr": "34.0",
        //         "ti_qu": "1.0",
        //         "ti_cu": "USD",
        //         "tna": "AF003",
        //         "tv": "java-0.7.0",
        //         "dtm": "1414607597877",
        //         "co": "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/contexts/jsonschema/1-0-0\",\"data\":[{\"schema\":\"iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0\",\"data\":{\"someContextKey\":\"testTrackPageView2\"}}]}"
        //     }, {
        //         "e": "tr",
        //         "tr_id": "order-7",
        //         "tr_tt": "25.0",
        //         "tr_af": "no_affiliate",
        //         "tr_tx": "0.0",
        //         "tr_sh": "0.0",
        //         "tr_ci": "Dover",
        //         "tr_st": "Delaware",
        //         "tr_co": "US",
        //         "tr_cu": "USD",
        //         "tna": "AF003",
        //         "tv": "java-0.7.0",
        //         "dtm": "1414607597877"
        //     }]
        // }
        verify(postRequestedFor(urlEqualTo("/com.snowplowanalytics.snowplow/tp2"))
                .withHeader("Content-Type", equalTo("application/json; charset=utf-8"))
                .withRequestBody(equalToJson("{\"schema\":\"iglu:com.snowplowanalytics.snowplow/payload_data/jsonschema/1-0-0\",\"data\":[{\"e\":\"ti\",\"ti_id\":\"order-8\",\"ti_sk\":\"no_sku\",\"ti_nm\":\"Big Order\",\"ti_ca\":\"Food\",\"ti_pr\":\"34.0\",\"ti_qu\":\"1.0\",\"ti_cu\":\"USD\",\"aid\":\"cloudfront\",\"tna\":\"AF003\",\"tv\":\"java-0.7.0\",\"co\":\"{\\\"schema\\\":\\\"iglu:com.snowplowanalytics.snowplow/contexts/jsonschema/1-0-0\\\",\\\"data\\\":[{\\\"schema\\\":\\\"iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0\\\",\\\"data\\\":{\\\"someContextKey\\\":\\\"testTrackPageView2\\\"}}]}\"},{\"e\":\"tr\",\"tr_id\":\"order-7\",\"tr_tt\":\"25.0\",\"tr_af\":\"no_affiliate\",\"tr_tx\":\"0.0\",\"tr_sh\":\"0.0\",\"tr_ci\":\"Dover\",\"tr_st\":\"Delaware\",\"tr_co\":\"US\",\"tr_cu\":\"USD\",\"aid\":\"cloudfront\",\"tna\":\"AF003\",\"tv\":\"java-0.7.0\"}]}",
                        JSONCompareMode.LENIENT)));
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
        subject.setTimezone("Etc/UTC");
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

        // Verifying this JSON:
        // {
        //     "schema": "iglu:com.snowplowanalytics.snowplow/payload_data/jsonschema/1-0-0",
        //     "data": [{
        //         "e": "ue",
        //         "ue_pr": "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0\",\"data\":{\"schema\":\"iglu:com.snowplowanalytics.snowplow/contexts/jsonschema/1-0-0\",\"data\":{\"id\":\"screen_1\"}}}",
        //         "tna": "AF003",
        //         "tv": "java-0.7.0",
        //         "co": "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/screen_view/jsonschema/1-0-0\",\"data\":[{\"schema\":\"iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0\",\"data\":{\"someContextKey\":\"testTrackPageView2\"}}]}",
        //         "tz": "Etc/UTC",
        //         "p": "pc",
        //         "vp": "320x480"
        //     }]
        // }
        verify(postRequestedFor(urlEqualTo("/com.snowplowanalytics.snowplow/tp2"))
                .withHeader("Content-Type", equalTo("application/json; charset=utf-8"))
                .withRequestBody(equalToJson("{\"schema\":\"iglu:com.snowplowanalytics.snowplow/" +
                                "payload_data/jsonschema/1-0-0\",\"data\":[{\"e\":\"ue\",\"ue_pr\":" +
                                "\"{\\\"schema\\\":\\\"iglu:com.snowplowanalytics.snowplow/" +
                                "unstruct_event/jsonschema/1-0-0\\\",\\\"data\\\":{\\\"schema\\\":" +
                                "\\\"iglu:com.snowplowanalytics.snowplow/screen_view/jsonschema/1-0-0\\\"," +
                                "\\\"data\\\":{\\\"id\\\":\\\"screen_1\\\"}}}\",\"aid\":\"cloudfront\"," +
                                "\"tna\":\"AF003\",\"tv\":\"java-0.7.0\",\"co\":\"{\\\"schema\\\":" +
                                "\\\"iglu:com.snowplowanalytics.snowplow/contexts/jsonschema/1-0-0\\\"," +
                                "\\\"data\\\":[{\\\"schema\\\":" +
                                "\\\"iglu:com.snowplowanalytics.snowplow/example/jsonschema/1-0-0\\\"," +
                                "\\\"data\\\":{\\\"someContextKey\\\":\\\"testTrackPageView2\\\"}}]}\"," +
                                "\"tz\":\"Etc/UTC\",\"p\":\"pc\",\"vp\":\"320x480\"}]}",
                        JSONCompareMode.LENIENT)));
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