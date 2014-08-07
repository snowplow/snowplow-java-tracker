package com.snowplowanalytics.snowplow.tracker;

import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.emitter.HttpMethod;
import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class EmitterTest extends TestCase {

    private static String localURL = "segfault.ngrok.com";
    private static String testURL = "d3rkrsqld9gmqf.cloudfront.net";

    @Test
    public void testEmitterConstructor() throws Exception {
        Emitter emitter = new Emitter(localURL, HttpMethod.POST);
    }

    @Test
    public void testFlushGet() throws Exception {
        Emitter emitter = new Emitter(localURL, HttpMethod.GET);

        SchemaPayload payload;
        String res;
        LinkedHashMap<String, Object> foo = new LinkedHashMap<String, Object>();
        ArrayList<String> bar = new ArrayList<String>();
        bar.add("somebar");
        bar.add("somebar2");
        foo.put("myKey", "my Value");
        foo.put("mehh", bar);
        String myarray[] = {"arrayItem","arrayItem2"};
        payload = new SchemaPayload();
        payload.setData(myarray);
        payload.addMap(foo);

        emitter.addToBuffer(payload);

        emitter.flushBuffer();
    }
}