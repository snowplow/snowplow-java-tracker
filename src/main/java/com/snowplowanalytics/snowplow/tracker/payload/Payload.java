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

import java.util.Map;

/**
 * The Payload is used to store all the parameters and configurations that are used
 * to send data via the HTTP GET/POST request.
 */
public interface Payload {

    /**
     * Add a key-value pair to the payload:
     * - Checks that the key is not null or empty
     * - Checks that the value is not null or empty
     *
     * @param key The parameter key
     * @param value The parameter value as a String
     */
    void add(String key, String value);

    /**
     * Add all the mappings from the specified map. The effect is the equivalent to that of calling:
     *  - add(String key, String value) for each key value pair.
     *
     * @param map Key-Value pairs to be stored in this payload
     */
    void addMap(Map<String, String> map);

    /**
     * Add a map to the Payload with a key dependent on the base 64 encoding option you choose using the
     * two keys provided.
     *
     * @param map Map to be converted to a String and stored as a value
     * @param base64Encoded The option you choose to encode the data
     * @param typeEncoded The key that would be set if the encoding option was set to true
     * @param typeNotEncoded They key that would be set if the encoding option was set to false
     */
    void addMap(Map<String, ?> map, boolean base64Encoded, String typeEncoded, String typeNotEncoded);

    /**
     * Returns the Payload as a HashMap.
     *
     * @return A HashMap
     */
    Map<String, ?> getMap();

    /**
     * Returns the byte size of a payload.
     *
     * @return A long representing the byte size of the payload.
     */
    long getByteSize();

    /**
     * Returns the Payload as a string. This is essentially the toString from the ObjectNode used
     * to store the Payload.
     *
     * @return A string value of the Payload.
     */
    String toString();
}
