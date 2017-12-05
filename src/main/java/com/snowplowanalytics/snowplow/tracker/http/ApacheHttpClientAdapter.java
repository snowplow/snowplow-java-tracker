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

// Apache
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

// Slf4j
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This library
import com.snowplowanalytics.snowplow.tracker.constants.Constants;

/**
 * A HttpClient built using Apache to send events via
 * GET or POST requests.
 */
public class ApacheHttpClientAdapter extends AbstractHttpClientAdapter {

    private final Logger LOGGER = LoggerFactory.getLogger(ApacheHttpClientAdapter.class);
    private CloseableHttpClient httpClient;

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

    public static Builder<?> builder() {
        return new Builder2();
    }

    protected ApacheHttpClientAdapter(Builder<?> builder) {
        super(builder);

        // Precondition checks
        Preconditions.checkNotNull(builder.httpClient);

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
            HttpResponse httpResponse = httpClient.execute(httpGet);
            httpGet.releaseConnection();
            return httpResponse.getStatusLine().getStatusCode();
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
            HttpResponse httpResponse = httpClient.execute(httpPost);
            httpPost.releaseConnection();
            return httpResponse.getStatusLine().getStatusCode();
        } catch (Exception e) {
            LOGGER.error("ApacheHttpClient POST Request failed: {}", e.getMessage());
            return -1;
        }
    }
}
