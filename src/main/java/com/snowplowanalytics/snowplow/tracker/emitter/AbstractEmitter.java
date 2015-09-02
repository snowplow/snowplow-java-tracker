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
package com.snowplowanalytics.snowplow.tracker.emitter;

// This library
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

/**
 * Abstract Emitter class.
 */
abstract class AbstractEmitter implements Emitter {

    protected final HttpClientAdapter httpClientAdapter;
    protected RequestCallback requestCallback;

    /**
     * Returns an Emitter with a selected HttpAdapter
     *
     * @param httpClientAdapter the chosen http client to use
     * @param requestCallback Request callback functions
     */
    public AbstractEmitter(HttpClientAdapter httpClientAdapter, RequestCallback requestCallback) {
        this.httpClientAdapter = httpClientAdapter;
        this.requestCallback = requestCallback;
    }

    /**
     * Adds a payload to the buffer and checks whether
     * we have reached the buffer limit yet.
     *
     * @param payload an event payload
     */
    @Override
    public abstract void emit(TrackerPayload payload);

    /**
     * Checks whether the response code was a success or not.
     *
     * @param code the response code
     * @return whether it is in the success range
     */
    protected boolean isSuccessfulSend(int code) {
        return code >= 200 && code < 300;
    }
}
