/*
 * Copyright (c) 2014-2022 Snowplow Analytics Ltd. All rights reserved.
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

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.snowplowanalytics.snowplow.tracker.constants.Constants;

import java.util.Objects;

/**
 * A HttpClient built using Apache to send events via
 * GET or POST requests.
 */
public class ApacheHttpClientAdapter extends AbstractHttpClientAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApacheHttpClientAdapter.class);
    private CloseableHttpClient httpClient;

    public ApacheHttpClientAdapter(String url, CloseableHttpClient httpClient) {
        super(url);

        // Precondition checks
        Objects.requireNonNull(httpClient);

        this.httpClient = httpClient;
    }

    /**
     * @deprecated Create HttpClientAdapter directly instead
     * @param <T> Builder
     */
    @Deprecated
    public static abstract class Builder<T extends Builder<T>> extends AbstractHttpClientAdapter.Builder<T> {

        private CloseableHttpClient httpClient; // Required

        /**
         * @param httpClient The Apache HTTP Client to use
         * @return itself
         */
        public T httpClient(CloseableHttpClient httpClient) {
            this.httpClient = httpClient;
            return self();
        }

        public ApacheHttpClientAdapter build() {
            return new ApacheHttpClientAdapter(this);
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
    protected ApacheHttpClientAdapter(Builder<?> builder) {
        super(builder);

        // Precondition checks
        Objects.requireNonNull(builder.httpClient);

        this.httpClient = builder.httpClient;
    }

    /**
     * Returns the HttpClient in use; it is up to the developer
     * to cast it back to its original class.
     *
     * @return the http client
     */
    public Object getHttpClient() {
        return this.httpClient;
    }

    /**
     * Attempts to send a group of payloads with a
     * GET request to the configured endpoint.
     *
     * @param url the URL send
     * @return the HttpResponse for the Request
     */
    public int doGet(String url) {
        try {
            HttpGet httpGet = new HttpGet(url);
            return httpClient.execute(httpGet, response -> {
                return response.getCode();
            });
        } catch (Exception e) {
            LOGGER.error("ApacheHttpClient GET Request failed: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * Attempts to send a group of payloads with a
     * POST request to the configured endpoint.
     *
     * @param url the URL to send to
     * @param payload the payload to send
     * @return the HttpResponse for the Request
     */
    public int doPost(String url, String payload) {
        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", Constants.POST_CONTENT_TYPE);
            StringEntity params = new StringEntity(payload, ContentType.APPLICATION_JSON);
            httpPost.setEntity(params);
            return httpClient.execute(httpPost, response -> {
                return response.getCode();
            });
        } catch (Exception e) {
            LOGGER.error("ApacheHttpClient POST Request failed: {}", e.getMessage());
            return -1;
        }
    }
}
