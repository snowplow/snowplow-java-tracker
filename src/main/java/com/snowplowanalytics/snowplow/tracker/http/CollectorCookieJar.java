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
package com.snowplowanalytics.snowplow.tracker.http;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CollectorCookieJar implements okhttp3.CookieJar {
    private static final Set<CollectorCookie> cookies = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<CollectorCookie> cookiesToRemove = new ArrayList<>();
        List<Cookie> validCookies = new ArrayList<>();

        for (CollectorCookie currentCookie : cookies) {
            if (currentCookie.isExpired()) {
                cookiesToRemove.add(currentCookie);
            } else if (currentCookie.getCookie().matches(url)) {
                validCookies.add(currentCookie.getCookie());
            }
        }

        if (!cookiesToRemove.isEmpty()) {
            removeAll(cookiesToRemove);
        }

        return validCookies;
    }

    @Override
    public void saveFromResponse(HttpUrl httpUrl, List<Cookie> cookies) {
        saveAll(cookies);
    }

    public void clear() {
        cookies.clear();
    }

    private void saveAll(Collection<Cookie> newCookies) {
        for (CollectorCookie cookie : CollectorCookie.decorateAll(newCookies)) {
            cookies.remove(cookie);
            cookies.add(cookie);
        }

    }

    private void removeAll(Collection<CollectorCookie> cookiesToRemove) {
        for (CollectorCookie cookie : cookiesToRemove) {
            cookies.remove(cookie);
        }
    }
}

