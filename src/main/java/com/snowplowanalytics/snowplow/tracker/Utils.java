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
import java.net.URL;
import java.util.*;

// Jackson
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private static final ObjectMapper objectMapper = new ObjectMapper();

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

    // Emitter Utils

    /**
     * Validates a uri and checks that it is valid
     * before being used by the emitter.
     *
     * @param url the uri to validate
     * @return whether the uri is valid or not
     */
    public static boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            LOGGER.error("URI {} is not valid: {}", url, e.getMessage());
            return false;
        }
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
     * Processes a Map into a JSON String or returns an empty
     * String if it fails
     *
     * @param map the map to process into a JSON String
     * @return the final JSON String
     */
    public static String mapToJSONString(Map map) {
        String jString = "";
        try {
            jString = objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            LOGGER.error("Could not process Map {} into JSON String: {}", map, e.getMessage());
        }
        return jString;
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
