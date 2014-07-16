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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;
import java.util.Set;

public class TrackerPayload implements Payload {

    private ObjectMapper objectMapper = Util.defaultMapper();
    private ObjectNode objectNode;

    public TrackerPayload() {
        objectNode = objectMapper.createObjectNode();
    }

    @Override
    public void add(String key, String value) {
        objectNode.put(key, value);
    }

    public void add(String key, Object value) {
        objectNode.putPOJO(key, objectMapper.valueToTree(value));
    }

    @Override
    public void addMap(Map map) {
        Set<String> keys = map.keySet();
        for(String key : keys) {
            objectNode.putPOJO(key, objectMapper.valueToTree(map.get(key)));
        }
    }

    @Override
    public void addMap(Map map, Boolean base64_encoded, String type_encoded, String type_no_encoded) {
        if (base64_encoded) { // base64 encoded data
            objectNode.put(type_encoded, Util.base64Encode(map.toString()));
        } else { // add it as a child node
            add(type_no_encoded, map);
        }
    }

    public void setContext() {
        if (true) {
            // if object passed is an array,
        } else {
            // else it's just an ObjectNode with data to put in
        }
    }

    public void setSchema(String schema) {
        // Always sets schema with key "$schema"
    }

    @Override
    public JsonNode getNode() {
        return null;
    }

    @Override
    public Map getMap() {
        return null;
    }

    @Override
    public String toString() {
        return objectNode.toString();
    }
}
