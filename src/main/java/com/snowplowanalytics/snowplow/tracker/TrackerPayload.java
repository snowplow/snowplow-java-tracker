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
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;
import java.util.Set;

public class TrackerPayload extends ObjectNode implements Payload {

    public TrackerPayload() {

    }

    public TrackerPayload(Map map) {

    }

    public TrackerPayload(JsonNode jsonNode) {

    }

    @Override
    public void add(String key, String value) {
        super.put(key, value);
    }

    @Override
    public void addMap(Map map) {
        super.putAll(map);
        Set<String> keys = map.keySet();
        for(String key : keys) {
            super.putPOJO(key, map.get(key));
        }
    }

    @Override
    public void addMap(Map map, Boolean base64_encoded, String type_encoded, String type_no_encoded) {
        if (base64_encoded) { // base64 encoded data

        } else { // add it as a child node
            
        }
    }

    @Override
    public JsonNode getNode() {
        return null;
    }

    @Override
    public Map getMap() {
        return null;
    }
}
