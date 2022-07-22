package com.snowplowanalytics.snowplow.tracker.http;

import okhttp3.Cookie;

import java.util.*;

public class CollectorCookie {
    private final Cookie cookie;

    static List<CollectorCookie> decorateAll(Collection<Cookie> cookies) {
        List<CollectorCookie> collectorCookies = new ArrayList<>(cookies.size());
        for (Cookie cookie : cookies) {
            collectorCookies.add(new CollectorCookie(cookie));
        }
        return collectorCookies;
    }

    CollectorCookie(Cookie cookie) {
        this.cookie = cookie;
    }

    public boolean isExpired() {
        return cookie.expiresAt() < System.currentTimeMillis();
    }

    Cookie getCookie() {
        return cookie;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CollectorCookie)) return false;
        CollectorCookie that = (CollectorCookie) other;
        return that.cookie.name().equals(this.cookie.name())
                && that.cookie.domain().equals(this.cookie.domain())
                && that.cookie.path().equals(this.cookie.path());
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + cookie.name().hashCode();
        hash = 31 * hash + cookie.domain().hashCode();
        hash = 31 * hash + cookie.path().hashCode();
        return hash;
    }
}
