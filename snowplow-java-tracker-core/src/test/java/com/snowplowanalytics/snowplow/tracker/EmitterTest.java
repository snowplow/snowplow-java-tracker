package com.snowplowanalytics.snowplow.tracker;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.snowplowanalytics.snowplow.tracker.core.emitter.BufferOption;
import com.snowplowanalytics.snowplow.tracker.core.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.core.emitter.HttpMethod;
import com.snowplowanalytics.snowplow.tracker.core.emitter.RequestCallback;
import com.snowplowanalytics.snowplow.tracker.core.emitter.RequestMethod;
import com.snowplowanalytics.snowplow.tracker.core.payload.Payload;
import com.snowplowanalytics.snowplow.tracker.core.payload.TrackerPayload;

import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class EmitterTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private static String testURL = "localhost:8080";

    @Test
    public void testEmitterConstructor() throws Exception {
        Emitter emitter = new Emitter(testURL, HttpMethod.POST);
    }

    @Test
    public void testEmitterConstructor2() throws Exception {
        Emitter emitter = new Emitter(testURL);
    }

    @Test
    public void testFlushGet() throws Exception {
        Emitter emitter = new Emitter(testURL);

        TrackerPayload payload;
        LinkedHashMap<String, Object> foo = new LinkedHashMap<String, Object>();
        foo.put("test", "testFlushBuffer");
        payload = new TrackerPayload();
        payload.addMap(foo);

        emitter.addToBuffer(payload);

        emitter.flushBuffer();

        verify(getRequestedFor(urlEqualTo("/i?test=testFlushBuffer")));
    }

    @Test
    public void testFlushPost() throws Exception {
        Emitter emitter = new Emitter(testURL, HttpMethod.POST);

        TrackerPayload payload = new TrackerPayload();
        LinkedHashMap<String, Object> foo = new LinkedHashMap<String, Object>();
        payload.add("someValue", "someKey");
        ArrayList<String> anArray = new ArrayList<String>();
        anArray.add("value1");
        anArray.add("value2");
        payload.add("values", anArray.toString());
        payload.addMap(foo);

        emitter.addToBuffer(payload);

        emitter.flushBuffer();

        verify(postRequestedFor(urlEqualTo("/com.snowplowanalytics.snowplow/tp2"))
                .withHeader("Content-Type", equalTo("application/json; charset=utf-8"))
                .withRequestBody(equalToJson("{\"schema\":\"iglu:com.snowplowanalytics.snowplow/" +
                        "payload_data/jsonschema/1-0-0\",\"data\":[{\"someValue\":\"someKey\"," +
                        "\"values\":\"[value1, value2]\"}]}")));
    }

    @Test
    public void testBufferOption() throws Exception {
        Emitter emitter = new Emitter(testURL);
        emitter.setBufferOption(BufferOption.Instant);
    }

    @Test
    public void testFlushBuffer() throws Exception {
        stubFor(get(urlEqualTo("/i?test=testFlushBuffer"))
                .willReturn(aResponse()
                        .withStatus(200)));

        Emitter emitter = new Emitter(testURL, HttpMethod.GET, new RequestCallback() {
            @Override
            public void onSuccess(int successCount) {
                System.out.println("Buffer length for successful POST/GET:" + successCount);
            }

            @Override
            public void onFailure(int successCount, List<Payload> failedEvent) {
                System.out.println("Failure, successCount: " + successCount +
                        "\nfailedEvent:\n" + failedEvent.toString());
            }
        });

        emitter.setRequestMethod(RequestMethod.Asynchronous);
        for (int i=0; i < 5; i++) {
            TrackerPayload payload;
            LinkedHashMap<String, Object> foo = new LinkedHashMap<String, Object>();
            foo.put("test", "testFlushBuffer");
            payload = new TrackerPayload();
            payload.addMap(foo);

            emitter.addToBuffer(payload);
        }
        emitter.flushBuffer();

        verify(getRequestedFor(urlEqualTo("/i?test=testFlushBuffer")));
    }

    @Test
    public void testMaxBuffer() throws Exception {
        Emitter emitter = new Emitter(testURL, HttpMethod.GET, null);
        emitter.setRequestMethod(RequestMethod.Asynchronous);
        for (int i=0; i < 10; i++) {
            TrackerPayload payload;
            LinkedHashMap<String, Object> foo = new LinkedHashMap<String, Object>();
            foo.put("test", "testFlushBuffer");
            payload = new TrackerPayload();
            payload.addMap(foo);

            emitter.addToBuffer(payload);
        }

        verify(getRequestedFor(urlEqualTo("/i?test=testFlushBuffer")));
    }
}