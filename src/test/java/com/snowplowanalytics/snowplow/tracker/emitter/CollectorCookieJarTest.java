/*
 * Copyright (c) 2014-2022 Snowplow Analytics Ltd. All rights reserved.
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
package com.snowplowanalytics.snowplow.tracker.emitter;

import com.snowplowanalytics.snowplow.tracker.events.PageView;
import com.snowplowanalytics.snowplow.tracker.http.CollectorCookieJar;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import okhttp3.Cookie;
import okhttp3.HttpUrl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CollectorCookieJarTest {
    String domain1 = "http://snowplow.test.url.com";
    String domain2 = "http://other.test.url.com";
    Cookie cookie1;
    CollectorCookieJar cookieJar;
    ArrayList<Cookie> requestCookies;

    @Before
    public void setUp() {
        cookie1 = new Cookie.Builder()
                .name("sp")
                .value("xxx")
                .domain("snowplow.test.url.com")
                .build();
        cookieJar = new CollectorCookieJar();
        requestCookies = new ArrayList<>();
    }

    @Test
    public void testNoCookiesAtStartup() {
        List<Cookie> cookies1 = cookieJar.loadForRequest(HttpUrl.parse(domain1));
        assertTrue(cookies1.isEmpty());
    }

    @Test
    public void testReturnsCookiesAfterSetInResponse() {
        requestCookies.add(cookie1);
        cookieJar.saveFromResponse(
                HttpUrl.parse(domain1),
                requestCookies
        );

        List<Cookie> cookies2 = cookieJar.loadForRequest(HttpUrl.parse(domain1));
        assertFalse(cookies2.isEmpty());
        assertEquals(cookies2.get(0).name(), "sp");

        cookieJar.clear();
    }

    @Test
    public void testDoesntReturnCookiesForDifferentDomain() {
        requestCookies.add(cookie1);
        cookieJar.saveFromResponse(
                HttpUrl.parse(domain1),
                requestCookies
        );

        List<Cookie> cookies2 = cookieJar.loadForRequest(HttpUrl.parse(domain2));
        assertTrue(cookies2.isEmpty());

        cookieJar.clear();
    }

    @Test
    public void testMaintainsCookiesAcrossJarInstances() {
        requestCookies.add(cookie1);
        cookieJar.saveFromResponse(
                HttpUrl.parse("http://acme.test.url.com"),
                requestCookies
        );

        CollectorCookieJar cookieJar2 = new CollectorCookieJar();

        List<Cookie> cookies2 = cookieJar2.loadForRequest(HttpUrl.parse("http://acme.test.url.com"));
        assertFalse(cookies2.isEmpty());

        cookieJar.clear();
    }

//    @Test
//    public void testRemovesInvalidCookies() {
//        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
//        SharedPreferences sharedPreferences = context.getSharedPreferences(COOKIE_PERSISTANCE, Context.MODE_PRIVATE);
//        sharedPreferences.edit().putString("x", "y").apply();
//        assertEquals(1, sharedPreferences.getAll().size());
//
//        new CollectorCookieJar(context);
//        assertEquals(0, sharedPreferences.getAll().size());
//    }
}
