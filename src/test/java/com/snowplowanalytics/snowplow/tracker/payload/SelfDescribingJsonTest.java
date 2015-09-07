/*
 * Copyright (c) 2015 Snowplow Analytics Ltd. All rights reserved.
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

public class SelfDescribingJsonTest {

    @Test
    public void testMakeSdjWithoutData() {
        SelfDescribingJson sdj = new SelfDescribingJson("schema_string");
        String expected = "{\"schema\":\"schema_string\",\"data\":{}}";
        String sdjString = sdj.toString();
        assertNotNull(sdj);
        assertEquals(expected, sdjString);
    }

    @Test
    public void testMakeSdjWithObject() {
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        SelfDescribingJson sdj = new SelfDescribingJson("schema_string", data);
        String expected = "{\"schema\":\"schema_string\",\"data\":{\"key\":\"value\"}}";
        String sdjString = sdj.toString();
        assertNotNull(sdj);
        assertEquals(expected, sdjString);
    }

    @Test
    public void testMakeSdjWithTrackerPayload() {
        TrackerPayload data = new TrackerPayload();
        data.add("value", "key");
        SelfDescribingJson sdj = new SelfDescribingJson("schema_string", data);
        String expected = "{\"schema\":\"schema_string\",\"data\":{\"value\":\"key\"}}";
        String sdjString = sdj.toString();
        assertNotNull(sdj);
        assertEquals(expected, sdjString);
    }

    @Test
    public void testMakeSdjWithSdj() {
        SelfDescribingJson data = new SelfDescribingJson("nested_schema_string");
        SelfDescribingJson sdj = new SelfDescribingJson("schema_string", data);
        String expected = "{\"schema\":\"schema_string\",\"data\":{\"schema\":\"nested_schema_string\",\"data\":{}}}";
        String sdjString = sdj.toString();
        assertNotNull(sdj);
        assertEquals(expected, sdjString);
    }
}
