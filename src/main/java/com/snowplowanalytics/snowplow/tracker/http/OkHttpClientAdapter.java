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

// SquareUp
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.RequestBody;

// This library
import com.snowplowanalytics.snowplow.tracker.constants.Constants;

/**
 * A HttpClient built using OkHttp to send events via
 * GET or POST requests.
 */
public class OkHttpClientAdapter extends AbstractHttpClientAdapter {

    private final MediaType JSON = MediaType.parse(Constants.POST_CONTENT_TYPE);
    private final String uri;
    private OkHttpClient httpClient;

    /**
     * Builds and returns a new ApacheHttpClient.
     *
     * @param uri the collector uri to use for sending
     * @param httpClient the closeable httpclient
     */
    public OkHttpClientAdapter(String uri, OkHttpClient httpClient) {
        this.uri = uri;
        this.httpClient = httpClient;
    }

    /**
     * Attempts to send a group of payloads with a
     * GET request to the configured endpoint.
     *
     * @param payload the payload map to send
     * @return the HttpResponse for the Request
     */
    public int doGet(Map<String, Object> payload) {
        StringBuilder urlBuilder = new StringBuilder(uri).append("/i?");

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
            throw new RuntimeException(e);
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
                    .url(uri + "/" + Constants.PROTOCOL_VENDOR + "/" + Constants.PROTOCOL_VERSION)
                    .addHeader("Content-Type", Constants.POST_CONTENT_TYPE)
                    .post(body)
                    .build();
            Response response = httpClient.newCall(request).execute();
            return response.code();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
