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

package com.snowplowanalytics.snowplow.tracker.payload;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.snowplowanalytics.snowplow.tracker.Parameter;
import com.snowplowanalytics.snowplow.tracker.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SchemaPayload {

    private final ObjectMapper objectMapper = Util.defaultMapper();
    private final Logger logger = LoggerFactory.getLogger(SchemaPayload.class);
    private ObjectNode objectNode = objectMapper.createObjectNode();

    public SchemaPayload() { }

    public SchemaPayload(Payload payload) {
        ObjectNode data;

        if (payload.getClass() == TrackerPayload.class) {
            logger.debug("Payload class is a TrackerPayload instance.");
            logger.debug("Trying getNode()");
            data = (ObjectNode) ((TrackerPayload) payload).getNode();
        } else {
            logger.debug("Converting Payload map to ObjectNode.");
            data = objectMapper.valueToTree(payload.getMap());
        }
        objectNode.set(Parameter.DATA, data);
    }

    public SchemaPayload setSchema(String schema) {
        logger.debug("Setting schema: {}", schema);
        objectNode.put(Parameter.SCHEMA, schema);
        return this;
    }

    public SchemaPayload setData(Payload data) {
        objectNode.putPOJO(Parameter.DATA, objectMapper.valueToTree(data.getMap()));
        return this;
    }

    public SchemaPayload setData(Object data) {
        objectNode.putPOJO(Parameter.DATA, objectMapper.valueToTree(data));
        return this;
    }

    public Map getMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            map = objectMapper.readValue(objectNode.toString(), new TypeReference<Map>(){});
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public JsonNode getNode() { return objectNode; }

    public String toString() { return objectNode.toString(); }
}
