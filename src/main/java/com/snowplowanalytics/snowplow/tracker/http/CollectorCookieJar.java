package com.snowplowanalytics.snowplow.tracker.http;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CollectorCookieJar implements okhttp3.CookieJar {
    private static final Set<CollectorCookie> cookies = Collections.newSetFromMap(new ConcurrentHashMap<CollectorCookie, Boolean>());

    public CollectorCookieJar() {
    }

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

