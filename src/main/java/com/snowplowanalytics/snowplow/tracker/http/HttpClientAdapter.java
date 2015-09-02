/*
 * Copyright (c) 2015 Snowplow Analytics Ltd. All rights reserved.
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
package com.snowplowanalytics.snowplow.tracker.http;

// This library
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

/**
 * Interface for all HttpClients
 */
public interface HttpClientAdapter {

    /**
     * Sends a group of events compressed into a
     * single SelfDescribingJson payload
     *
     * @param payload the final event payload
     */
    int post(SelfDescribingJson payload);

    /**
     * Sends a single TrackerPayload via a
     * GET request
     *
     * @param payload the event payload
     */
    int get(TrackerPayload payload);
}
