package com.snowplowanalytics.snowplow.tracker;

import com.snowplowanalytics.snowplow.tracker.emitter.*;
import com.snowplowanalytics.snowplow.tracker.payload.Payload;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class EmitterTest {

    private static String testURL = "d3rkrsqld9gmqf.cloudfront.net";

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
    }

    @Test
    public void testFlushPost() throws Exception {
        Emitter emitter = new Emitter(testURL, HttpMethod.POST, null);

        TrackerPayload payload;
        LinkedHashMap<String, Object> foo = new LinkedHashMap<String, Object>();
        ArrayList<String> bar = new ArrayList<String>();
        bar.add("somebar");
        bar.add("somebar");
        foo.put("test", "testMaxBuffer");
        foo.put("mehh", bar);
        payload = new TrackerPayload();
        payload.addMap(foo);

        emitter.addToBuffer(payload);


        emitter.flushBuffer();
    }

    @Test
    public void testBufferOption() throws Exception {
        Emitter emitter = new Emitter(testURL);
        emitter.setBufferOption(BufferOption.Instant);
    }

    @Test
    public void testFlushBuffer() throws Exception {

        Emitter emitter = new Emitter(testURL, HttpMethod.GET, new RequestCallback() {
            @Override
            public void onSuccess(int successCount) {
                System.out.println("Buffer length for POST/GET:" + successCount);
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
    }
}