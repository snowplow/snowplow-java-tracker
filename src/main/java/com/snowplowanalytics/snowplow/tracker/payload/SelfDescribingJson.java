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
package com.snowplowanalytics.snowplow.tracker.payload;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.snowplowanalytics.snowplow.tracker.Utils;
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;

/**
 * Builds a SelfDescribingJson object which can contain two fields:
 * - Schema: the JsonSchema path for this Json
 * - Data: the data for this Json
 */
public class SelfDescribingJson implements Payload {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelfDescribingJson.class);
    private final LinkedHashMap<String, Object> payload = new LinkedHashMap<>();

    /**
     * Creates a SelfDescribingJson with only a Schema
     * String and an empty data map.
     *
     * @param schema the schema string
     */
    public SelfDescribingJson(String schema) {
        this(schema, new LinkedHashMap<String, Object>());
    }

    /**
     * Creates a SelfDescribingJson with a Schema and a
     * TrackerPayload object.
     *
     * @param schema the schema string
     * @param data a TrackerPayload object to be embedded as
     *             the data
     */
    public SelfDescribingJson(String schema, TrackerPayload data) {
        setSchema(schema);
        setData(data);
    }

    /**
     * Creates a SelfDescribingJson with a Schema and a
     * SelfDescribingJson object.  This can be used to
     * nest SDJs inside of each other.
     *
     * @param schema the schema string
     * @param data a SelfDescribingJson object to be embedded as
     *             the data
     */
    public SelfDescribingJson(String schema, SelfDescribingJson data) {
        setSchema(schema);
        setData(data);
    }

    /**
     * Creates a SelfDescribingJson with a Schema and a
     * data object.
     *
     * @param schema the schema string
     * @param data an object to attempt to embed as data
     */
    public SelfDescribingJson(String schema, Object data) {
        setSchema(schema);
        setData(data);
    }

    /**
     * Sets the Schema for the SelfDescribingJson
     *
     * @param schema a valid schema string
     * @return this SelfDescribingJson
     */
    public SelfDescribingJson setSchema(String schema) {
        Preconditions.checkNotNull(schema, "schema cannot be null");
        Preconditions.checkArgument(!schema.isEmpty(), "schema cannot be empty.");
        payload.put(Parameter.SCHEMA, schema);
        return this;
    }

    /**
     * Adds data to the SelfDescribingJson
     * - Accepts a TrackerPayload object
     *
     * @param data the data to be added to the SelfDescribingJson
     * @return this SelfDescribingJson
     */
    public SelfDescribingJson setData(TrackerPayload data) {
        if (data == null) {
            return this;
        }
        payload.put(Parameter.DATA, data.getMap());
        return this;
    }

    /**
     * Adds data to the SelfDescribingJson
     *
     * @param data the data to be added to the SelfDescribingJson
     * @return this SelfDescribingJson
     */
    public SelfDescribingJson setData(Object data) {
        if (data == null) {
            return this;
        }
        payload.put(Parameter.DATA, data);
        return this;
    }

    /**
     * Allows us to add data from one SelfDescribingJson into another
     * without copying over the Schema.
     *
     * @param data the payload to add to the SelfDescribingJson
     * @return this SelfDescribingJson
     */
    public SelfDescribingJson setData(SelfDescribingJson data) {
        if (payload == null) {
            return this;
        }
        payload.put(Parameter.DATA, data.getMap());
        return this;
    }

    @Deprecated
    @Override
    public void add(String key, String value) {
        LOGGER.info("Payload: add(String, String) method called - Doing nothing.");
    }

    @Deprecated
    @Override
    public void addMap(Map<String, String> map) {
        LOGGER.info("Payload: addMap(Map<String, Object>) method called - Doing nothing.");
    }

    @Deprecated
    @Override
    public void addMap(Map<String, ?> map, boolean base64Encoded, String typeEncoded, String typeNotEncoded) {
        LOGGER.info("Payload: addMap(Map, boolean, String, String) method called - Doing nothing.");
    }

    /**
     * Returns the Payload as a Map.
     *
     * @return A Map of all the key-value entries
     */
    @Override
    public Map<String, Object> getMap() {
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
     * Returns the Payload as a string. This is essentially the toString from the ObjectNode used
     * to store the Payload.
     *
     * @return A string value of the Payload.
     */
    @Override
    public String toString() {
        return Utils.mapToJSONString(payload);
    }
}
