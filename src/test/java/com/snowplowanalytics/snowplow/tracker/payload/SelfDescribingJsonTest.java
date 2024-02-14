/*
 * Copyright (c) 2014-present Snowplow Analytics Ltd. All rights reserved.
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

import static org.junit.Assert.*;

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
        String eventId = data.getEventId();
        String dtm = Long.toString(data.getDeviceCreatedTimestamp());

        SelfDescribingJson sdj = new SelfDescribingJson("schema_string", data);

        String expected = "{\"schema\":\"schema_string\",\"data\":{\"eid\":\"" + eventId + "\",\"dtm\":\"" + dtm + "\",\"value\":\"key\"}}";
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

    @Test
    public void testEqualityOfTwoInstances_withSchemaNameOnly() {
        SelfDescribingJson a = new SelfDescribingJson("schema");
        SelfDescribingJson b = new SelfDescribingJson("schema");
        assertEquals(a, b);
    }

    @Test
    public void testEqualityOfTwoInstances_withTrackerPayload() {
        TrackerPayload nestedData = new TrackerPayload();
        nestedData.add("key", "value");
        SelfDescribingJson a = new SelfDescribingJson("schema", nestedData);
        SelfDescribingJson b = new SelfDescribingJson("schema", nestedData);
        assertEquals(a, b);
    }

    @Test
    public void testEqualityOfTwoInstances_withNestedEvent() {
        TrackerPayload nestedData = new TrackerPayload();
        nestedData.add("key", "value");
        SelfDescribingJson nestedEvent = new SelfDescribingJson("nested_event", nestedData);
        SelfDescribingJson a = new SelfDescribingJson("schema", nestedEvent);
        SelfDescribingJson b = new SelfDescribingJson("schema", nestedEvent);
        assertEquals(a, b);
    }

    @Test
    public void testNegativeEqualityOfTwoInstances_withSchemaNameOnly() {
        SelfDescribingJson a = new SelfDescribingJson("schema-one");
        SelfDescribingJson b = new SelfDescribingJson("schema-two");
        assertNotEquals(a, b);
    }

    @Test
    public void testNegativeEqualityOfTwoInstances_withTrackerPayload() {
        TrackerPayload nestedDataOne = new TrackerPayload();
        nestedDataOne.add("key", "value-one");
        TrackerPayload nestedDataTwo = new TrackerPayload();
        nestedDataTwo.add("key", "value-two");
        SelfDescribingJson a = new SelfDescribingJson("schema", nestedDataOne);
        SelfDescribingJson b = new SelfDescribingJson("schema", nestedDataTwo);
        assertNotEquals(a, b);
    }

    @Test
    public void testNegativeEqualityOfTwoInstances_withNestedEvent() {
        TrackerPayload nestedDataOne = new TrackerPayload();
        nestedDataOne.add("key", "value-one");
        SelfDescribingJson nestedEventOne = new SelfDescribingJson("nested_event", nestedDataOne);

        TrackerPayload nestedDataTwo = new TrackerPayload();
        nestedDataTwo.add("key", "value-two");
        SelfDescribingJson nestedEventTwo = new SelfDescribingJson("nested_event", nestedDataTwo);


        SelfDescribingJson a = new SelfDescribingJson("schema", nestedEventOne);
        SelfDescribingJson b = new SelfDescribingJson("schema", nestedEventTwo);
        assertNotEquals(a, b);
    }
}
