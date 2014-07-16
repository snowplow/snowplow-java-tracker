package com.snowplowanalytics.snowplow.tracker;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

public class TrackerPayloadTest extends TestCase {

    @Test
    public void testAddString() throws Exception {
        Payload payload = new TrackerPayload();
        payload.add("foo", "bar");
        System.out.println(payload.toString());

        String res = "{\"foo\":\"bar\"}";
        assertEquals(payload.toString(), res);
    }

    @Test
    public void testAddObject() throws Exception {
        Map foo = new LinkedHashMap<String, String>();
        ArrayList<String> bar = new ArrayList<String>();
        bar.add("somebar");
        bar.add("somebar2");
        foo.put("myKey", "my Value");
        foo.put("mehh", bar);
        Payload payload = new TrackerPayload();
        payload.addMap(foo);

        String res = "{\"myKey\":\"my Value\",\"mehh\":[\"somebar\",\"somebar2\"]}";
        assertEquals(payload.toString(),res);
    }

    @Test
    public void testAddMap() throws Exception {

    }

    @Test
    public void testAddMap1() throws Exception {

    }

    @Test
    public void testGetNode() throws Exception {

    }

    @Test
    public void testGetMap() throws Exception {

    }
}