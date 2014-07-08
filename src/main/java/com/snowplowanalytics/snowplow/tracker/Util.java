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

package com.snowplowanalytics.snowplow.tracker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

public class Util {
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

    public static Long getTimestamp() {
        return System.currentTimeMillis();
    }
}
