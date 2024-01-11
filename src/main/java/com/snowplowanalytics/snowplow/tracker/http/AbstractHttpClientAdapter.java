/*
 * Copyright (c) 2014-present Snowplow Analytics Ltd. All rights reserved.
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

import com.snowplowanalytics.snowplow.tracker.constants.Constants;
import com.snowplowanalytics.snowplow.tracker.Utils;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

/**
 * Abstract HttpClient class.
 */
public abstract class AbstractHttpClientAdapter implements HttpClientAdapter {

    protected final String url;

    public AbstractHttpClientAdapter(String url) {
        this.url = url.replaceFirst("/*$", "");
    }

    /**
     * @deprecated Create HttpClientAdapter directly instead
     * @param <T> Builder
     */
    @Deprecated
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
            this.url = url.replaceFirst("/*$", "");
            return self();
        }
    }

    private static class Builder2 extends Builder<Builder2> {
        @Override
        protected Builder2 self() {
            return this;
        }
    }

    /**
     * @deprecated Create HttpClientAdapter directly instead
     * @return Builder object
     */
    @Deprecated
    public static Builder<?> builder() {
        return new Builder2();
    }

    /**
     * @deprecated Create HttpClientAdapter directly instead
     * @param builder Builder object
     */
    @Deprecated
    protected AbstractHttpClientAdapter(Builder<?> builder) {
        // Precondition checks
        if (!Utils.isValidUrl(builder.url)) {
            throw new IllegalArgumentException();
        }

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
        String url = this.url + "/" + Constants.PROTOCOL_VENDOR + "/" + Constants.PROTOCOL_VERSION;
        String body = payload.toString();
        return doPost(url, body);
    }

    /**
     * Sends a payload via a GET request.
     *
     * @param payload the TrackerPayload to send
     */
    @Override
    public int get(TrackerPayload payload) {
        String url = this.url + "/i?" + Utils.mapToQueryString(payload.getMap());
        return doGet(url);
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
     * @param url the URL to send to
     * @param payload the event payload String
     * @return the result of the send
     */
    protected abstract int doPost(String url, String payload);

    /**
     * Sends the Map of key-value pairs for the event
     * as a GET request to the endpoint.
     *
     * @param url the URL to send
     * @return the result of the send
     */
    protected abstract int doGet(String url);
}
