package com.snowplowanalytics.snowplow.tracker.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Cookie;

import java.util.*;

public class CollectorCookie {
    private final Cookie cookie;
    private static final ObjectMapper objectMapper = new ObjectMapper();

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

    CollectorCookie(String serialized) throws JsonProcessingException {
        // this probably won't work
        cookie = objectMapper.readValue(serialized, Cookie.class);

//        JSONObject object = new JSONObject(serialized);
//        cookie = new Cookie.Builder()
//                .name(object.getString("name"))
//                .value(object.getString("value"))
//                .expiresAt(object.getLong("expiresAt"))
//                .domain(object.getString("domain"))
//                .path(object.getString("path"))
//                .build();
    }

    public boolean isExpired() {
        return cookie.expiresAt() < System.currentTimeMillis();
    }

    Cookie getCookie() {
        return cookie;
    }

    String getCookieKey() {
        return (cookie.secure() ? "https" : "http") + "://" + cookie.domain() + cookie.path() + "|" + cookie.name();
    }

    public String serialize() throws JsonProcessingException {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("name", cookie.name());
        values.put("value", cookie.value());
        values.put("expiresAt", cookie.expiresAt());
        values.put("domain", cookie.domain());
        values.put("path", cookie.path());
        return objectMapper.writeValueAsString(values);

//        return new JSONObject(values).toString();
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
