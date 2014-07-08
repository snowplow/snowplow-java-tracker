package com.snowplowanalytics.snowplow.tracker;

import com.fasterxml.jackson.databind.JsonNode;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UtilTest extends TestCase {
    @Test
    public void testGetTimestamp() {
        assertNotNull(Util.getTimestamp());
    }

    @Test
    public void testGetTransactionId() {
        assertNotNull(Util.getTransactionId());
    }

    @Test
    public void testMapToJsonNode() {
        Map map = new HashMap();
        map.put("foo", "bar");

        JsonNode node = Util.mapToJsonNode(map);

        assertEquals("bar", node.get("foo").asText());
    }

    @Test
    public void testMapToJsonNode2() {
        Map map = new HashMap();
        map.put("foo", "bar");

        ArrayList<String> list = new ArrayList<String>();
        list.add("some");
        list.add("stuff");

        map.put("list", list);

        JsonNode node = Util.mapToJsonNode(map);

        assertEquals("{\"list\":[\"some\",\"stuff\"],\"foo\":\"bar\"}", node.toString());
    }

    @Test
    public void testStringToJsonNode() throws Exception {
        Map map = new HashMap();
        map.put("foo", "bar");

        ArrayList<String> list = new ArrayList<String>();
        list.add("some");
        list.add("stuff");

        map.put("list", list);
        String res = "{\"list\":[\"some\",\"stuff\"],\"foo\":\"bar\"}";

        Util.stringToJsonNode(res);

        JsonNode node = Util.mapToJsonNode(map);

        assertEquals(node.toString(), res);
    }
}
