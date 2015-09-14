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
package com.snowplowanalytics.snowplow.tracker;

// JUnit
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UtilsTest {

    @Test
    public void testGetTimestamp() {
        String ts = Utils.getTimestamp();
        assertNotNull(ts);
        assertTrue(ts.length() == 13);
    }

    @Test
    public void testGetEventId() {
        String eid = Utils.getEventId();
        assertNotNull(eid);
        assertTrue(eid.matches("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$"));
    }

    @Test
    public void testGetTransactionId() {
        int tid = Utils.getTransactionId();
        assertTrue(tid > 100000 && tid < 999999);
    }

    @Test
    public void testIsUriValid() {
        String goodUri1 = "http://www.acme.com";
        assertTrue(Utils.isValidUrl(goodUri1));
        String goodUri2 = "https://www.acme.com";
        assertTrue(Utils.isValidUrl(goodUri2));
        String goodUri3 = "ftp://www.acme.com";
        assertTrue(Utils.isValidUrl(goodUri3));

        String badUri1 = "www.acme.com";
        assertTrue(!Utils.isValidUrl(badUri1));
        String badUri2 = "http://";
        assertTrue(!Utils.isValidUrl(badUri2));
    }

    @Test
    public void testGetTimezone() {
        String tz = Utils.getTimezone();
        assertNotNull(tz);
    }

    @Test
    public void testBase64Encode() {
        String expected = "aGVsbG93b3JsZHRlc3RiNjQ=";
        String b64encoded = Utils.base64Encode("helloworldtestb64");
        assertEquals(expected, b64encoded);
    }

    @Test
    public void testGetUtf8Length() {
        long expected = 20;
        long utf8Length = Utils.getUTF8Length("helloworldTest123456");
        assertEquals(expected, utf8Length);
    }
}
