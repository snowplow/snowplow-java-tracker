package com.snowplowanalytics.snowplow.tracker;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// JSONassert
import org.skyscreamer.jsonassert.JSONAssert;
import org.json.JSONException;

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
    public void testMapToJsonNode2() throws JSONException {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("foo", "bar");

        ArrayList<String> list = new ArrayList<String>();
        list.add("some");
        list.add("stuff");

        map.put("list", list);

        JsonNode node = Util.mapToJsonNode(map);

        // Have to stringify because JSONAssert works with json.org, not Jackson
        JSONAssert.assertEquals("{\"list\":[\"some\",\"stuff\"],\"foo\":\"bar\"}", node.toString(), false);
    }
}
