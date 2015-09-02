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

// Java
import java.util.Map;

// Google
import com.google.common.base.Preconditions;

// This library
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

/**
 * Abstract HttpClient class.
 */
public abstract class AbstractHttpClientAdapter implements HttpClientAdapter {

    /**
     * Builds a new HttpClientAdapter
     */
    protected AbstractHttpClientAdapter() {}

    /**
     * Sends a payload via a POST request.
     *
     * @param payload the SelfDescribingJson to send
     */
    @Override
    public int post(SelfDescribingJson payload) {
        Preconditions.checkNotNull(payload);
        String body = payload.toString();
        return doPost(body);
    }

    /**
     * Sends a payload via a GET request.
     *
     * @param payload the TrackerPayload to send
     */
    @Override
    @SuppressWarnings("unchecked")
    public int get(TrackerPayload payload) {
        Preconditions.checkNotNull(payload);
        return doGet(payload.getMap());
    }

    /**
     * Sends the SelfDescribingJson string containing
     * the events as a POST request to the endpoint.
     *
     * @param payload the event payload String
     * @return the result of the send
     */
    protected abstract int doPost(String payload);

    /**
     * Sends the Map of key-value pairs for the event
     * as a GET request to the endpoint.
     *
     * @param payload the event payload Map
     * @return the result of the send
     */
    protected abstract int doGet(Map<String, Object> payload);
}
