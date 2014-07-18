package com.snowplowanalytics.snowplow.tracker;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class TrackerPayloadTest extends TestCase {

    @Test
    public void testAddString() throws Exception {
        Payload payload = new TrackerPayload();
        payload.add("foo", "bar");

        String res = "{\"foo\":\"bar\"}";
        assertEquals(payload.toString(), res);
    }

    @Test
    public void testAddObject() throws Exception {

    }

    @Test
    public void testAddMap() throws Exception {
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
    public void testAddMapNotEncoding() throws Exception {
        Map foo = new LinkedHashMap<String, String>();
        ArrayList<String> bar = new ArrayList<String>();
        bar.add("somebar");
        bar.add("somebar2");
        foo.put("myKey", "my Value");
        foo.put("mehh", bar);
        Payload payload = new TrackerPayload();
        payload.addMap(foo, false, "cx", "co");

        String res = "{\"co\":{\"myKey\":\"my Value\",\"mehh\":[\"somebar\",\"somebar2\"]}}";
        assertEquals(payload.toString(), res);
    }

    @Test
    public void testAddMapEncoding() throws Exception {
        Map foo = new LinkedHashMap<String, String>();
        ArrayList<String> bar = new ArrayList<String>();
        bar.add("somebar");
        bar.add("somebar2");
        foo.put("myKey", "my Value");
        foo.put("mehh", bar);
        Payload payload = new TrackerPayload();
        payload.addMap(foo, true, "cx", "co");

        String res = "{\"cx\":\"e215S2V5PW15IFZhbHVlLCBtZWhoPVtzb21lYmFyLCBzb21lYmFyMl19\"}";
        assertEquals(payload.toString(), res);
    }

    @Test
    public void testSetData() {
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

        res = "{\"data\":[\"arrayItem\",\"arrayItem2\"]}";
        assertEquals(payload.toString(), res);

        payload = new TrackerPayload();
        payload.setData(foo);

        res = "{\"data\":{\"myKey\":\"my Value\",\"mehh\":[\"somebar\",\"somebar2\"]}}";
        assertEquals(payload.toString(), res);

        payload = new TrackerPayload();
        payload.setData(bar);

        res = "{\"data\":[\"somebar\",\"somebar2\"]}";
        assertEquals(payload.toString(), res);
    }

    @Test
    public void testSetSchema() throws Exception {
        Payload payload = new TrackerPayload();
        payload.setSchema("iglu:com.snowplowanalytics.snowplow/payload_data/jsonschema/1-0-0");
        String res = "{\"$schema\":\"iglu:com.snowplowanalytics.snowplow/payload_data/jsonschema/1-0-0\"}";
        assertEquals(payload.toString(), res);
    }

    @Test
    public void testGetNode() throws Exception {

    }

    @Test
    public void testGetMap() throws Exception {

    }
}