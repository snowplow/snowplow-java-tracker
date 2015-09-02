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

// Java
import java.lang.reflect.Array;
import java.util.*;

// Json
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Slf4j
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Apache
import static org.apache.commons.codec.binary.Base64.encodeBase64String;

/**
 * Provides basic Utilities for the Snowplow Tracker.
 */
public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    // Tracker Utils

    /**
     * Returns the current System time
     * as a String.
     *
     * @return the system time as a string
     */
    public static String getTimestamp() {
        return Long.toString(System.currentTimeMillis());
    }

    /**
     * Generates a random UUID for
     * each event.
     *
     * @return a UUID string
     */
    public static String getEventId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Returns a Transaction ID integer.
     *
     * @return a new random Transaction ID
     */
    public static int getTransactionId() {
        Random r = new Random();
        return r.nextInt(999999 - 100000 + 1) + 100000;
    }

    // Subject Utils

    /**
     * Gets the default timezone of the server running
     * the library.
     *
     * @return the timezone id string
     */
    public static String getTimezone() {
        TimeZone tz = Calendar.getInstance().getTimeZone();
        return tz.getID();
    }

    // Payload Utils

    /**
     * Encodes a string into Base64.
     *
     * @param string the string too encode
     * @return a Base64 encoded string
     */
    public static String base64Encode(String string) {
        return encodeBase64String(string.getBytes());
    }

    /**
     *  Converts a Map to a JSONObject
     *
     *  @param map The map to convert
     *  @return The JSONObject
     */
    @SuppressWarnings("unchecked")
    public static JSONObject mapToJSONObject(Map map) {
        LOGGER.debug("Converting a map to a JSONObject: %s", map);
        JSONObject retObject = new JSONObject();
        Set<Map.Entry> entries = map.entrySet();
        for (Map.Entry entry : entries) {
            String key = (String) entry.getKey();
            Object value = getJsonSafeObject(entry.getValue());
            try {
                retObject.put(key, value);
            } catch (JSONException e) {
                LOGGER.debug("Could not put key {} and value {} into new JSONObject: {}", key, value, e);
                e.printStackTrace();
            }
        }
        return retObject;
    }

    /**
     * Returns a Json Safe object for situations
     * where the Build Version is too old.
     *
     * @param o The object to check and convert
     * @return the json safe object
     */
    private static Object getJsonSafeObject(Object o) {
        if (o == null) {
            return new Object() {
                @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
                @Override
                public boolean equals(Object o) {
                    return o == this || o == null;
                }
                @Override
                public String toString() {
                    return "null";
                }
            };
        } else if (o instanceof JSONObject || o instanceof JSONArray) {
            return o;
        } else if (o instanceof Collection) {
            JSONArray retArray = new JSONArray();
            for (Object entry : (Collection) o) {
                retArray.put(getJsonSafeObject(entry));
            }
            return retArray;
        } else if (o.getClass().isArray()) {
            JSONArray retArray = new JSONArray();
            int length = Array.getLength(o);
            for (int i = 0; i < length; i++) {
                retArray.put(getJsonSafeObject(Array.get(o, i)));
            }
            return retArray;
        } else if (o instanceof Map) {
            return mapToJSONObject((Map)o);
        } else  if (o instanceof Boolean ||
                o instanceof Byte ||
                o instanceof Character ||
                o instanceof Double ||
                o instanceof Float ||
                o instanceof Integer ||
                o instanceof Long ||
                o instanceof Short ||
                o instanceof String) {
            return o;
        } else if (o.getClass().getPackage().getName().startsWith("java.")) {
            return o.toString();
        }
        return null;
    }

    /**
     * Count the number of bytes a string will occupy when UTF-8 encoded
     *
     * @param s the String to process
     * @return number Length of s in bytes when UTF-8 encoded
     */
    public static long getUTF8Length(String s) {
        long len = 0;
        for (int i = 0; i < s.length(); i++) {
            char code = s.charAt(i);
            if (code <= 0x7f) {
                len += 1;
            } else if (code <= 0x7ff) {
                len += 2;
            } else if (code >= 0xd800 && code <= 0xdfff) {
                len += 4; i++;
            } else if (code < 0xffff) {
                len += 3;
            } else {
                len += 4;
            }
        }
        return len;
    }
}
