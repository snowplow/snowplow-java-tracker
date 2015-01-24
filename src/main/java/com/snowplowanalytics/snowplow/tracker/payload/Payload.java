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

// Java

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

// JSON

/**
 * Payload interface
 * The Payload is used to store all the parameters and configurations that are used
 * to send data via the HTTP GET/POST request.
 *
 * It allows the code for buffering events for sending to be agnostic of the event
 * format (either a TrackerPayload or a SchemaPayload).
 * @version 0.5.0
 * @author Jonathan Almeida
 */
public interface Payload {

    /**
     * Returns the Payload as a HashMap.
     * @return A HashMap
     */
    public Map getMap();

    /**
     * Returns the Payload using Jackson JSON to return a JsonNode.
     * @return A JsonNode
     */
    public JsonNode getNode();

    /**
     * Returns the Payload as a string. This is essentially the toString from the ObjectNode used
     * to store the Payload.
     * @return A string value of the Payload.
     */
    public String toString();
}
