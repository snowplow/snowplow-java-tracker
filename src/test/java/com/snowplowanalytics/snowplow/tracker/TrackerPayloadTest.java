package com.snowplowanalytics.snowplow.tracker;

import com.snowplowanalytics.snowplow.tracker.payload.Payload;
import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TrackerPayloadTest {

    @Test
    public void testAddString() throws Exception {
        TrackerPayload payload = new TrackerPayload();
        payload.add("foo", "bar");

        String res = "{\"foo\":\"bar\"}";
        assertEquals(res, payload.toString());
    }

    @Test
    public void testAddObject() throws Exception {
        TrackerPayload payload = new TrackerPayload();
        Map<String, String> map = new HashMap<String, String>();
        map.put("foo", "bar");
        map.put("more foo", "more bar");
        payload.add("map", map);

        String res = "{\"map\":{\"more foo\":\"more bar\",\"foo\":\"bar\"}}";
        assertEquals(res, payload.toString());
    }

    @Test
    public void testAddMap() throws Exception {
        Map<String, Object> foo = new LinkedHashMap<String, Object>();
        ArrayList<String> bar = new ArrayList<String>();
        bar.add("somebar");
        bar.add("somebar2");
        foo.put("myKey", "my Value");
        foo.put("mehh", bar);
        TrackerPayload payload = new TrackerPayload();
        payload.addMap(foo);

        String res = "{\"myKey\":\"my Value\",\"mehh\":[\"somebar\",\"somebar2\"]}";
        assertEquals(res, payload.toString());
    }

    @Test
    public void testAddMapNotEncoding() throws Exception {
        Map<String, Object> foo = new LinkedHashMap<String, Object>();
        ArrayList<String> bar = new ArrayList<String>();
        bar.add("somebar");
        bar.add("somebar2");
        foo.put("myKey", "my Value");
        foo.put("mehh", bar);
        TrackerPayload payload = new TrackerPayload();
        payload.addMap(foo, false, "cx", "co");

        String res = "{\"co\":\"{\\\"myKey\\\":\\\"my Value\\\",\\\"mehh\\\":[\\\"somebar\\\",\\\"somebar2\\\"]}\"}";
        assertEquals(res, payload.toString());
    }

    @Test
    public void testAddMapEncoding() throws Exception {
        Map<String, Object> foo = new LinkedHashMap<String, Object>();
        ArrayList<String> bar = new ArrayList<String>();
        bar.add("somebar");
        bar.add("somebar2");
        foo.put("myKey", "my Value");
        foo.put("mehh", bar);
        TrackerPayload payload = new TrackerPayload();
        payload.addMap(foo, true, "cx", "co");

        String res = "{\"cx\":\"eyJteUtleSI6Im15IFZhbHVlIiwibWVoaCI6WyJzb21lYmFyIiwic29tZWJhcjIiXX0=\"}";
        assertEquals(res, payload.toString());
    }

    @Test
    public void testSetData() {
        TrackerPayload payload;
        String res;
        LinkedHashMap<String, Object> foo = new LinkedHashMap<String, Object>();
        ArrayList<String> bar = new ArrayList<String>();
        bar.add("somebar");
        bar.add("somebar2");
        foo.put("myKey", "my Value");
        foo.put("mehh", bar);
        String myarray[] = {"arrayItem","arrayItem2"};
        payload = new TrackerPayload();
        payload.add("myarray", myarray);

        res = "{\"myarray\":[\"arrayItem\",\"arrayItem2\"]}";
        assertEquals(res, payload.toString());

        payload = new TrackerPayload();
        payload.add("foo", foo);

        res = "{\"foo\":{\"myKey\":\"my Value\",\"mehh\":[\"somebar\",\"somebar2\"]}}";
        assertEquals(res, payload.toString());

        payload = new TrackerPayload();
        payload.add("bar", bar);

        res = "{\"bar\":[\"somebar\",\"somebar2\"]}";
        assertEquals(res, payload.toString());
    }

    @Test
    public void testSetSchema() throws Exception {
        SchemaPayload payload = new SchemaPayload();
        payload.setSchema("iglu:com.snowplowanalytics.snowplow/payload_data/jsonschema/1-0-0");
        String res = "{\"schema\":\"iglu:com.snowplowanalytics.snowplow/payload_data/jsonschema/1-0-0\"}";
        assertEquals(res, payload.toString());
    }

    @Test
    public void testGetMap() throws Exception {
        SchemaPayload payload;
        String res;
        LinkedHashMap<String, Object> foo = new LinkedHashMap<String, Object>();
        ArrayList<String> bar = new ArrayList<String>();
        bar.add("somebar");
        bar.add("somebar2");
        foo.put("myKey", "my Value");
        foo.put("mehh", bar);
        LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("data", foo);
        payload = new SchemaPayload();
        payload.setData(foo);

        assertEquals(data, payload.getMap());
    }
}