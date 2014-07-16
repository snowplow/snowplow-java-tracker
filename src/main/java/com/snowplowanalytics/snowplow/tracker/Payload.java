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

// Java
import java.util.Map;

// JSON
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Payload interface
 * The Payload is used to store all the parameters and configurations that are used
 * to send data via the HTTP GET request.
 * Payload have an immutable structure for secure and accurate transfers of information.
 * @version 0.4.0
 * @author Jonathan Almeida
 */
public interface Payload {

    /**
     * Add a basic parameter.
     * @param key The parameter key
     * @param value The parameter value
     */
    public void add(String key, String value);

    public void add(String key, Object value);

    public void addMap(Map map);

    public void addMap(Map map, Boolean base64_encoded, String type_encoded, String type_no_encoded);

    public JsonNode getNode();

    public void setSchema(String schema);

    public Map getMap();

    public String toString();
}
