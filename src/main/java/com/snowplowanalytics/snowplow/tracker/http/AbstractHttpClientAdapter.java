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
import java.util.Objects;

// SquareUp
import com.squareup.okhttp.OkHttpClient;

// Apache
import org.apache.http.impl.client.CloseableHttpClient;

// Google
import com.google.common.base.Preconditions;

// This library
import com.snowplowanalytics.snowplow.tracker.Utils;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

/**
 * Abstract HttpClient class.
 */
public abstract class AbstractHttpClientAdapter implements HttpClientAdapter {

    protected final String url;

    public static abstract class Builder<T extends Builder<T>> {

        private String url; // Required
        protected abstract T self();

        /**
         * Adds a URI to the Client Adapter
         *
         * @param url the emitter url
         * @return itself
         */
        public T url(String url) {
            this.url = url;
            return self();
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

    protected AbstractHttpClientAdapter(Builder<?> builder) {
        // Precondition checks
        Preconditions.checkArgument(Utils.isValidUrl(builder.url));

        this.url = builder.url;
    }

    /**
     * Returns the HttpClient URI
     *
     * @return the uri String
     */
    @Override
    public String getUrl() {
        return this.url;
    }

    /**
     * Sends a payload via a POST request.
     *
     * @param payload the SelfDescribingJson to send
     */
    @Override
    public int post(SelfDescribingJson payload) {
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
        return doGet(payload.getMap());
    }

    /**
     * Returns the HttpClient in use; it is up to the developer
     * to cast it back to its original class.
     *
     * @return the http client
     */
    public abstract Object getHttpClient();

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
