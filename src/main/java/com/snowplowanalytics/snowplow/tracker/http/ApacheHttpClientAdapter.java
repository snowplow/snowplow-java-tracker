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

// Apache
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
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
    private final String uri;
    private CloseableHttpClient httpClient;

    /**
     * Builds and returns a new ApacheHttpClient.
     *
     * @param uri the collector uri to use for sending
     * @param httpClient the closeable httpclient
     */
    public ApacheHttpClientAdapter(String uri, CloseableHttpClient httpClient) {
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
        try {
            URIBuilder uriBuilder = new URIBuilder(uri);
            for (String key : payload.keySet()) {
                String value = (String) payload.get(key);
                uriBuilder.setParameter(key, value);
            }
            HttpGet httpGet = new HttpGet(uriBuilder.setPath("/i").build());
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
     * @param payload the payload to send
     * @return the HttpResponse for the Request
     */
    public int doPost(String payload) {
        try {
            URIBuilder uriBuilder = new URIBuilder(uri);
            HttpPost httpPost = new HttpPost(uriBuilder.setPath("/" + Constants.PROTOCOL_VENDOR + "/" + Constants.PROTOCOL_VERSION).build());
            httpPost.addHeader("Content-Type", Constants.POST_CONTENT_TYPE);
            StringEntity params = new StringEntity(payload);
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
