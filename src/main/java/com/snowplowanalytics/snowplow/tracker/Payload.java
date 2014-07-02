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

import java.util.Map;

public interface Payload {
    public void add(String key, Object value);
    public void addMap(Map map);
    public void addJson(JsonNode node);
    public void addMap(Map map, Boolean base64_encoded, String type_encoded, String type_no_encoded);
    public void addJson(JsonNode node, Boolean base64_encoded, String type_encoded, String type_no_encoded);
    public JsonNode get();
}
