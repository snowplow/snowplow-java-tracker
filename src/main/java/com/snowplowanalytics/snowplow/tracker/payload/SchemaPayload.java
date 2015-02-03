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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.snowplowanalytics.snowplow.tracker.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SchemaPayload {

    private final Logger LOGGER = LoggerFactory.getLogger(SchemaPayload.class);
    private Map<String, Object> map;

    public SchemaPayload() { 
        map = new HashMap<String, Object>();
    }

    public SchemaPayload setSchema(String schema) {
        Preconditions.checkNotNull(schema, "schema cannot be null");
        Preconditions.checkArgument(!schema.isEmpty(), "schema cannot be empty.");

        LOGGER.debug("Setting schema: {}", schema);
        map.put(Parameter.SCHEMA, schema);
        return this;
    }

    public SchemaPayload setData(Object data) {
        map.put(Parameter.DATA, data);

        return this;
    }

    @VisibleForTesting
    public Object getData() {
        return map.get(Parameter.DATA);
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public String toString() { return map.toString(); }
}
