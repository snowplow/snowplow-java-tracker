/*
 * Copyright (c) 2014-2020 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.snowplowanalytics.snowplow.tracker.payload;

// Java
import java.util.HashMap;
import java.util.Map;

// JUnit
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TrackerPayloadTest {

    @Test
    public void testAddKeyValue() {
        TrackerPayload payload = new TrackerPayload();
        payload.add("key", "value");
        assertNotNull(payload);
        assertTrue(payload.getMap().containsKey("key"));
        assertEquals("value", payload.getMap().get("key"));
    }

    @Test
    public void testAddKeyWithNullValue() {
        TrackerPayload payload = new TrackerPayload();
        payload.add("key", null);
        assertNotNull(payload);
        assertTrue(!payload.getMap().containsKey("key"));
    }

    @Test
    public void testAddKeyWithEmptyValue() {
        TrackerPayload payload = new TrackerPayload();
        payload.add("key", "");
        assertNotNull(payload);
        assertTrue(!payload.getMap().containsKey("key"));
    }

    @Test
    public void testAddMap() {
        Map<String, String> data = new HashMap<>();
        data.put("key", "value");
        TrackerPayload payload = new TrackerPayload();
        payload.addMap(data);
        assertNotNull(payload);
        assertTrue(payload.getMap().containsKey("key"));
        assertEquals("value", payload.getMap().get("key"));
    }

    @Test
    public void testAddMapWithNullValue() {
        Map<String, String> data = new HashMap<>();
        data.put("key", null);
        TrackerPayload payload = new TrackerPayload();
        payload.addMap(data);
        assertNotNull(payload);
        assertTrue(!payload.getMap().containsKey("key"));
    }

    @Test
    public void testAddMapWithEmptyValue() {
        Map<String, String> data = new HashMap<>();
        data.put("key", "");
        TrackerPayload payload = new TrackerPayload();
        payload.addMap(data);
        assertNotNull(payload);
        assertTrue(!payload.getMap().containsKey("key"));
    }

    @Test
    public void testAddMapEncoded() {
        Map<String, String> data = new HashMap<>();
        data.put("key", "value");
        TrackerPayload payload = new TrackerPayload();
        payload.addMap(data, true, "encoded", "non_encoded");
        assertNotNull(payload);
        assertTrue(payload.getMap().containsKey("encoded"));
        assertEquals("eyJrZXkiOiJ2YWx1ZSJ9", payload.getMap().get("encoded"));
    }

    @Test
    public void testAddMapNonEncoded() {
        Map<String, String> data = new HashMap<>();
        data.put("key", "value");
        TrackerPayload payload = new TrackerPayload();
        payload.addMap(data, false, "encoded", "non_encoded");
        assertNotNull(payload);
        assertTrue(payload.getMap().containsKey("non_encoded"));
        assertEquals("{\"key\":\"value\"}", payload.getMap().get("non_encoded"));
    }
}
