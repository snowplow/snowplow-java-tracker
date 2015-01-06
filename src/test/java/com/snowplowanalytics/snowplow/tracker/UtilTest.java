package com.snowplowanalytics.snowplow.tracker;

import com.fasterxml.jackson.databind.JsonNode;
import com.snowplowanalytics.snowplow.tracker.core.Util;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UtilTest {
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
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("foo", "bar");

        JsonNode node = Util.mapToJsonNode(map);

        assertEquals("bar", node.get("foo").asText());
    }

    @Test
    public void testMapToJsonNode2() {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("foo", "bar");

        ArrayList<String> list = new ArrayList<String>();
        list.add("some");
        list.add("stuff");

        map.put("list", list);

        JsonNode node = Util.mapToJsonNode(map);

        assertEquals("{\"list\":[\"some\",\"stuff\"],\"foo\":\"bar\"}", node.toString());
    }
}
