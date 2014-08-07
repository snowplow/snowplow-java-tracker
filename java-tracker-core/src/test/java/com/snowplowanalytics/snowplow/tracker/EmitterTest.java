package com.snowplowanalytics.snowplow.tracker;

import com.snowplowanalytics.snowplow.tracker.emitter.BufferOption;
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.emitter.HttpMethod;
import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

import junit.framework.TestCase;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class EmitterTest extends TestCase {

    private static String testURL = "segfault.ngrok.com";
//    private static String testURL = "d3rkrsqld9gmqf.cloudfront.net";

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
        Emitter emitter = new Emitter(testURL, HttpMethod.GET);

        TrackerPayload payload;
        LinkedHashMap<String, Object> foo = new LinkedHashMap<String, Object>();
        ArrayList<String> bar = new ArrayList<String>();
        bar.add("somebar");
        bar.add("somebar2");
        foo.put("myKey", "my Value");
        foo.put("mehh", bar);
        String my_array[] = {"arrayItem","arrayItem2"};
        payload = new TrackerPayload();
        payload.add("my_array", my_array);
        payload.addMap(foo);

        emitter.addToBuffer(payload);

        emitter.flushBuffer();
    }

    @Test
    public void testFlushPost() throws Exception {
        Emitter emitter = new Emitter(testURL, HttpMethod.POST);

        SchemaPayload payload;
        TrackerPayload trackerPayload = new TrackerPayload();
        LinkedHashMap<String, Object> foo = new LinkedHashMap<String, Object>();
        ArrayList<String> bar = new ArrayList<String>();
        bar.add("somebar");
        bar.add("somebar2");
        foo.put("myKey", "my Value");
        foo.put("mehh", bar);
        String my_array[] = {"arrayItem","arrayItem2"};
        trackerPayload.addMap(foo);
        trackerPayload.add("my_array", my_array);
        payload = new SchemaPayload();
        payload.setData(trackerPayload.getMap());
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
        Emitter emitter = new Emitter(testURL);
        emitter.flushBuffer();
    }

    @Test
    public void testMaxBuffer() throws Exception {
        Emitter emitter = new Emitter(testURL);
        TrackerPayload payload = new TrackerPayload();
        for (int i=0; i < 10; i++) {
            payload.add("key", "value" + i);
            emitter.addToBuffer(payload);
        }
    }
}