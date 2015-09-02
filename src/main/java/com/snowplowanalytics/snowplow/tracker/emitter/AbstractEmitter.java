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
import com.google.common.base.Preconditions;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

/**
 * Base AbstractEmitter class.
 */
public class AbstractEmitter implements Emitter {

    protected HttpClientAdapter httpClientAdapter;
    protected RequestCallback requestCallback;

    public static abstract class Builder<T extends Builder<T>> {

        private HttpClientAdapter httpClientAdapter; // Required
        private RequestCallback requestCallback = null; // Optional

        protected abstract T self();

        /**
         * Adds the HttpClientAdapter to the AbstractEmitter
         *
         * @param httpClientAdapter the adapter to use
         * @return itself
         */
        public T httpClientAdapter(HttpClientAdapter httpClientAdapter) {
            this.httpClientAdapter = httpClientAdapter;
            return self();
        }

        /**
         * An optional Request Callback for adding the ability to
         * handle failure cases for sending.
         *
         * @param requestCallback the emitter request callback
         * @return itself
         */
        public T requestCallback(RequestCallback requestCallback) {
            this.requestCallback = requestCallback;
            return self();
        }

        public AbstractEmitter build() {
            return new AbstractEmitter(this);
        }
    }

    private static class Builder2 extends Builder<Builder2> {
        @Override
        protected Builder2 self() {
            return this;
        }
    }

    public static Builder<?> builder() {
        return new Builder2();
    }

    protected AbstractEmitter(Builder<?> builder) {

        // Precondition checks
        Preconditions.checkNotNull(builder.httpClientAdapter);

        this.httpClientAdapter = builder.httpClientAdapter;
        this.requestCallback = builder.requestCallback;
    }

    /**
     * Adds a payload to the buffer and checks whether
     * we have reached the buffer limit yet.
     *
     * @param payload an event payload
     */
    @Override
    public void emit(TrackerPayload payload) {}

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
