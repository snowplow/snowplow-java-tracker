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
import java.util.Iterator;
import java.util.Map;

// Google
import com.google.common.base.Preconditions;

// SquareUp
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.RequestBody;

// Slf4j
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This library
import com.snowplowanalytics.snowplow.tracker.constants.Constants;

/**
 * A HttpClient built using OkHttp to send events via
 * GET or POST requests.
 */
public class OkHttpClientAdapter extends AbstractHttpClientAdapter {

    private final Logger LOGGER = LoggerFactory.getLogger(OkHttpClientAdapter.class);
    private final MediaType JSON = MediaType.parse(Constants.POST_CONTENT_TYPE);
    private OkHttpClient httpClient;

    public static abstract class Builder<T extends Builder<T>> extends AbstractHttpClientAdapter.Builder<T> {

        private OkHttpClient httpClient; // Required

        /**
         * @param httpClient The Apache HTTP Client to use
         * @return itself
         */
        public T httpClient(OkHttpClient httpClient) {
            this.httpClient = httpClient;
            return self();
        }

        public OkHttpClientAdapter build() {
            return new OkHttpClientAdapter(this);
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

    protected OkHttpClientAdapter(Builder<?> builder) {
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
     * @param payload the payload map to send
     * @return the HttpResponse for the Request
     */
    public int doGet(Map<String, Object> payload) {
        StringBuilder urlBuilder = new StringBuilder(this.url).append("/i?");

        Iterator<String> iterator = payload.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            urlBuilder.append(key).append("=").append(payload.get(key));
            if (iterator.hasNext()) {
                urlBuilder.append("&");
            }
        }
        Request request = new Request.Builder().url(urlBuilder.toString()).build();

        try {
            Response response = httpClient.newCall(request).execute();
            return response.code();
        } catch (Exception e) {
            LOGGER.error("OkHttpClient GET Request failed: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * Attempts to send a group of payloads with a
     * POST request to the configured endpoint.
     *
     * @param payload the payload to send
     * @return the HttpResponse for the Request
     */
    public int doPost(String payload) {
        try {
            RequestBody body = RequestBody.create(JSON, payload);
            Request request = new Request.Builder()
                    .url(this.url + "/" + Constants.PROTOCOL_VENDOR + "/" + Constants.PROTOCOL_VERSION)
                    .addHeader("Content-Type", Constants.POST_CONTENT_TYPE)
                    .post(body)
                    .build();
            Response response = httpClient.newCall(request).execute();
            return response.code();
        } catch (Exception e) {
            LOGGER.error("OkHttpClient POST Request failed: {}", e.getMessage());
            return -1;
        }
    }
}
