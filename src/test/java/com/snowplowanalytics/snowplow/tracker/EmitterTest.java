package com.snowplowanalytics.snowplow.tracker;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.ArrayList;

public class EmitterTest extends TestCase {

    @Test
    public void testEmitterConstructor() throws Exception {
        Emitter emitter = new Emitter("segfault.ngrok.com", HttpMethod.POST);
    }

    @Test
    public void testFlushGet() throws Exception {
        Emitter emitter = new Emitter("segfault.ngrok.com", HttpMethod.GET);

        Payload payload;
        String res;
        LinkedHashMap foo = new LinkedHashMap<String, String>();
        ArrayList<String> bar = new ArrayList<String>();
        bar.add("somebar");
        bar.add("somebar2");
        foo.put("myKey", "my Value");
        foo.put("mehh", bar);
        String myarray[] = {"arrayItem","arrayItem2"};
        payload = new TrackerPayload();
        payload.setData(myarray);
        payload.addMap(foo);

        emitter.addToBuffer(payload);

        emitter.flushBuffer();
    }
}