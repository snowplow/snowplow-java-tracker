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

import com.snowplowanalytics.snowplow.tracker.http.CollectorCookieJar;
import okhttp3.Cookie;
import okhttp3.HttpUrl;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class CollectorCookieJarTest {
    String domain1 = "http://snowplow.test.url.com";
    String domain2 = "http://other.test.url.com";
    Cookie cookie1;
    CollectorCookieJar cookieJar;
    List<Cookie> requestCookies;

    @Before
    public void setUp() {
        cookie1 = new Cookie.Builder()
                .name("sp")
                .value("xxx")
                .domain("snowplow.test.url.com")
                .build();
        cookieJar = new CollectorCookieJar();
    }

    @Test
    public void testNoCookiesAtStartup() {
        List<Cookie> cookies = cookieJar.loadForRequest(HttpUrl.parse(domain1));
        assertTrue(cookies.isEmpty());
    }

    @Test
    public void testReturnsCookiesAfterSetInResponse() {
        requestCookies = Collections.singletonList(cookie1);
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
        requestCookies = Collections.singletonList(cookie1);
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
        requestCookies = Collections.singletonList(cookie1);
        cookieJar.saveFromResponse(
                HttpUrl.parse(domain1),
                requestCookies
        );

        CollectorCookieJar cookieJar2 = new CollectorCookieJar();
        List<Cookie> cookies2 = cookieJar2.loadForRequest(HttpUrl.parse(domain1));
        assertFalse(cookies2.isEmpty());

        cookieJar.clear();
    }

    @Test
    public void testClearsCookies() {
        requestCookies = Collections.singletonList(cookie1);
        cookieJar.saveFromResponse(
                HttpUrl.parse(domain1),
                requestCookies
        );

        List<Cookie> cookies = cookieJar.loadForRequest(HttpUrl.parse(domain1));
        assertFalse(cookies.isEmpty());

        cookieJar.clear();
        List<Cookie> cookies2 = cookieJar.loadForRequest(HttpUrl.parse(domain1));
        assertTrue(cookies2.isEmpty());
    }

    @Test
    public void testRemovesExpiredCookies() {
        Cookie cookie2 = new Cookie.Builder()
                .name("sp")
                .value("xxx")
                .domain("snowplow.test.url.com")
                .expiresAt(1654869235L)
                .build();

        requestCookies = Collections.singletonList(cookie2);
        cookieJar.saveFromResponse(
                HttpUrl.parse(domain1),
                requestCookies
        );

        List<Cookie> cookies = cookieJar.loadForRequest(HttpUrl.parse(domain1));
        assertTrue(cookies.isEmpty());
    }

}
