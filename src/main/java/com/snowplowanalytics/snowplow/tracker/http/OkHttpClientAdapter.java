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

// Java
import java.io.IOException;
import java.util.Objects;

// SquareUp
import okhttp3.*;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(OkHttpClientAdapter.class);
    private final MediaType JSON = MediaType.get(Constants.POST_CONTENT_TYPE);
    private OkHttpClient httpClient;

    public OkHttpClientAdapter(String url, OkHttpClient httpClient) {
        super(url);

        // Precondition checks
        Objects.requireNonNull(httpClient);

        this.httpClient = httpClient;
    }

    public OkHttpClientAdapter(String url) {
        this(url, new OkHttpClient.Builder().build());
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
     * @return the HttpResponse code for the Request or -1 if exception is caught
     */
    public int doGet(String url) {
        int returnValue = -1;

        Request request = new Request.Builder().url(url).build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                LOGGER.error("OkHttpClient GET Request failed: {}", response);
            }
            returnValue = response.code();
        } catch (IOException e) {
            LOGGER.error("OkHttpClient GET Request failed: {}", e.getMessage());
        }

        return returnValue;
    }


    /**
     * Attempts to send a group of payloads with a
     * POST request to the configured endpoint.
     *
     * @param url the URL to send to
     * @param payload the payload to send
     * @return the HttpResponse code for the Request or -1 if exception is caught
     */
    public int doPost(String url, String payload) {
        int returnValue = -1;

        RequestBody body = RequestBody.create(payload, JSON);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", Constants.POST_CONTENT_TYPE)
                .post(body)
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                LOGGER.error("OkHttpClient POST Request failed: {}", response);
            }
            returnValue = response.code();
        } catch (IOException e) {
            LOGGER.error("OkHttpClient POST Request failed: {}", e.getMessage());
        }

        return returnValue;
    }
}
