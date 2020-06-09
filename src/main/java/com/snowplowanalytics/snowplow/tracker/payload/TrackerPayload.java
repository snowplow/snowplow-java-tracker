/*
 * Copyright (c) 2014-2020 Snowplow Analytics Ltd. All rights reserved.
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

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.snowplowanalytics.snowplow.tracker.Utils;

/**
 * Returns a standard Tracker Payload consisting of
 * many key - pair values.
 */
public class TrackerPayload implements Payload {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackerPayload.class);
    protected final Map<String, String> payload = new LinkedHashMap<>();

    /**
     * Add a key-value pair to the payload: - Checks that the key is not null or
     * empty - Checks that the value is not null or empty
     *
     * @param key   The parameter key
     * @param value The parameter value as a String
     */
    @Override
    public void add(final String key, final String value) {
        if (key == null || key.isEmpty()) {
            LOGGER.error("Invalid key detected: {}", key);
            return;
        }
        if (value == null || value.isEmpty()) {
            LOGGER.info("null or empty value detected: {}", value);
            return;
        }
        LOGGER.debug("Adding new kv pair: {}->{}", key, value);
        payload.put(key, value);
    }

    /**
     * Add all the mappings from the specified map. The effect is the equivalent to
     * that of calling: - add(String key, String value) for each key value pair.
     *
     * @param map Key-Value pairs to be stored in this payload
     */
    @Override
    public void addMap(final Map<String, String> map) {
        if (map == null) {
            LOGGER.debug("Map passed in is null, returning without adding map.");
            return;
        }
        LOGGER.debug("Adding new map: {}", map);
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Add a map to the Payload with a key dependent on the base 64 encoding option
     * you choose using the two keys provided.
     *
     * @param map            Map to be converted to a String and stored as a value
     * @param base64Encoded  The option you choose to encode the data
     * @param typeEncoded    The key that would be set if the encoding option was set to true
     * @param typeNotEncoded They key that would be set if the encoding option was set to false
     */
    @Override
    public void addMap(final Map<String, ?> map, final boolean base64Encoded, final String typeEncoded, final String typeNotEncoded) {
        if (map == null) {
            LOGGER.debug("Map passed in is null, returning nothing.");
            return;
        }

        final String mapString = Utils.mapToJSONString(map);
        LOGGER.debug("Adding new map: {}", map);

        if (base64Encoded) {
            add(typeEncoded, Utils.base64Encode(mapString, StandardCharsets.UTF_8));
        } else {
            add(typeNotEncoded, mapString);
        }
    }

    /**
     * Returns the Payload as a Map.
     *
     * @return A Map of all the key-value entries
     */
    @Override
    public Map<String, String> getMap() {
        return payload;
    }

    /**
     * Returns the byte size of a payload.
     *
     * @return A long representing the byte size of the payload.
     */
    @Override
    public long getByteSize() {
        return Utils.getUTF8Length(toString());
    }

    /**
     * Returns the Payload as a string. This is essentially the toString from the
     * ObjectNode used to store the Payload.
     *
     * @return A string value of the Payload.
     */
    @Override
    public String toString() {
        return Utils.mapToJSONString(payload);
    }
}
