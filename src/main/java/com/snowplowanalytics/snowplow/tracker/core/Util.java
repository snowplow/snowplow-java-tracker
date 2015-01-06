/*
 * Copyright (c) 2014 Snowplow Analytics Ltd. All rights reserved.
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

package com.snowplowanalytics.snowplow.tracker.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Util {
    private static final Logger logger = LoggerFactory.getLogger(Util.class);
    private static ObjectMapper sObjectMapper = new ObjectMapper();
    public static ObjectMapper defaultMapper() {
        return sObjectMapper;
    }

    @Deprecated
    public static JsonNode stringToJsonNode(String str) {
        try {
            return defaultMapper().readTree(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JsonNode mapToJsonNode(Map map) {
        return defaultMapper().valueToTree(map);
    }

    public static int getTransactionId() {
        Random r = new Random(); //NEED ID RANGE
        return r.nextInt(999999-100000+1) + 100000;
    }

    public static String getTimestamp() {
        return Long.toString(System.currentTimeMillis());
    }


    /* Addition functions
     *  Used to add different sources of key=>value pairs to a map.
     *  Map is then used to build "Associative array for getter function.
     *  Some use Base64 encoding
     */
    public static String base64Encode(String string) {
        try {
            return Base64.encode(string.getBytes());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getEventId() {
        return UUID.randomUUID().toString();
    }
}
