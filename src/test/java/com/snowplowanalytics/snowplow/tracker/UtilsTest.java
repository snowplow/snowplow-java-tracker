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

// Java
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

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
    public void testMapToQueryString() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("k1", "v1");
        payload.put("k2", "s p a c e");
        payload.put("k3", "s+p+a+c+e");

        assertEquals("k1=v1&k2=s%20p%20a%20c%20e&k3=s%2Bp%2Ba%2Bc%2Be", Utils.mapToQueryString(payload));
    }

    @Test
    public void testObjectToUTF8() {
        assertEquals("", Utils.urlEncodeUTF8(null));
        assertEquals(
            "%3C%20%3E%20%23%20%25%20%7B%20%7D%20%7C%20%5C%20%5E%20%7E%20%5B%20%5D%20%60%20%3B%20%2F%20%3F%20%3A%20%40%20%3D%20%26%20%24%20%2B%20%22", 
            Utils.urlEncodeUTF8("< > # % { } | \\ ^ ~ [ ] ` ; / ? : @ = & $ + \""));
    }

    @Test
    public void testGetTimezone() {
        String tz = Utils.getTimezone();
        assertNotNull(tz);
    }

    @Test
    public void testBase64Encode() {
        String expected = "aGVsbG93b3JsZHRlc3RiNjR3aXRodXRmOGNoYXJzw7TDqcOgw6c=";
        String b64encoded = Utils.base64Encode("helloworldtestb64withutf8charsôéàç", StandardCharsets.UTF_8);
        assertEquals(expected, b64encoded);

    }

    @Test
    public void testGetUtf8Length() {
        long expected = 20;
        long utf8Length = Utils.getUTF8Length("helloworldTest123456");
        assertEquals(expected, utf8Length);
    }
}
