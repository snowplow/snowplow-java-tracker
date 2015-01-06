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

package com.snowplowanalytics.snowplow.tracker.core;

public class Constants {
    public static final String PROTOCOL_VENDOR = "com.snowplowanalytics.snowplow";
    public static final String PROTOCOL_VERSION = "tp2"; // Tracker Protocol v2

    public static final String SCHEMA_PAYLOAD_DATA = "iglu:com.snowplowanalytics.snowplow/payload_data/jsonschema/1-0-0";
    public static final String SCHEMA_CONTEXTS = "iglu:com.snowplowanalytics.snowplow/contexts/jsonschema/1-0-0";
    public static final String SCHEMA_UNSTRUCT_EVENT = "iglu:com.snowplowanalytics.snowplow/unstruct_event/jsonschema/1-0-0";
    public static final String SCHEMA_SCREEN_VIEW = "iglu:com.snowplowanalytics.snowplow/screen_view/jsonschema/1-0-0";

    public static final String EVENT_PAGE_VIEW = "pv";
    public static final String EVENT_STRUCTURED = "se";
    public static final String EVENT_UNSTRUCTURED = "ue";
    public static final String EVENT_ECOMM = "tr";
    public static final String EVENT_ECOMM_ITEM = "ti";
}