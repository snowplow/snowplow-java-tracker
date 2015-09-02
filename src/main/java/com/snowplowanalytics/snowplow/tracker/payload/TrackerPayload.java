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
package com.snowplowanalytics.snowplow.tracker.payload;

// Java
import java.util.HashMap;
import java.util.Map;

// Slf4j
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This library
import com.snowplowanalytics.snowplow.tracker.Utils;

/**
 * Returns a standard Tracker Payload consisting of
 * many key - pair values.
 */
public class TrackerPayload implements Payload {

    private final Logger LOGGER = LoggerFactory.getLogger(TrackerPayload.class);
    private final HashMap<String, Object> payload = new HashMap<>();

    /**
     * Add a basic parameter.
     *
     * @param key The parameter key
     * @param value The parameter value as a String
     */
    public void add(String key, String value) {
        if (value == null || value.isEmpty()) {
            LOGGER.debug("The keys value is empty, returning without adding key: {}", key);
            return;
        }
        LOGGER.info("Adding new kv pair: {}->{}", key, value);
        payload.put(key, value);
    }

    /**
     * Add a basic parameter.
     *
     * @param key The parameter key
     * @param value The parameter value
     */
    public void add(String key, Object value) {
        if (value == null) {
            LOGGER.debug("The keys value is empty, returning without adding key: {}", key);
            return;
        }
        LOGGER.info("Adding new kv pair: {}->{}", key, value);
        payload.put(key, value);
    }

    /**
     * Add all the mappings from the specified map. The effect is the equivalent to that of calling
     * add(String key, Object value) for each mapping for each key.
     *
     * @param map Mappings to be stored in this map
     */
    public void addMap(Map<String, Object> map) {
        if (map == null) {
            LOGGER.debug("Map passed in is null, returning without adding map.");
            return;
        }
        LOGGER.info("Adding new map: {}", map);
        payload.putAll(map);
    }

    /**
     * Add a map to the Payload with a key dependent on the base 64 encoding option you choose using the
     * two keys provided.
     *
     * @param map Mapping to be stored
     * @param base64_encoded The option you choose to encode the data
     * @param type_encoded The key that would be set if the encoding option was set to true
     * @param type_no_encoded They key that would be set if the encoding option was set to false
     */
    public void addMap(Map map, Boolean base64_encoded, String type_encoded, String type_no_encoded) {
        if (map == null) {
            LOGGER.debug("Map passed in is null, returning nothing.");
            return;
        }

        String mapString = Utils.mapToJSONObject(map).toString();
        LOGGER.info("Adding new map: {}", map);

        if (base64_encoded) {
            add(type_encoded, Utils.base64Encode(mapString));
        } else {
            add(type_no_encoded, mapString);
        }
    }

    /**
     * Returns the Payload as a HashMap.
     *
     * @return A HashMap
     */
    public Map getMap() {
        return payload;
    }

    /**
     * Returns the byte size of a payload.
     *
     * @return A long representing the byte size of the payload.
     */
    public long getByteSize() {
        return Utils.getUTF8Length(toString());
    }

    /**
     * Returns the Payload as a string. This is essentially the toString from the ObjectNode used
     * to store the Payload.
     *
     * @return A string value of the Payload.
     */
    public String toString() {
        return Utils.mapToJSONObject(payload).toString();
    }
}
