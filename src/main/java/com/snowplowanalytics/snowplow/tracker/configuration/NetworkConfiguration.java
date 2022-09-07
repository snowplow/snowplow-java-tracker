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
package com.snowplowanalytics.snowplow.tracker.configuration;

import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import okhttp3.CookieJar;


public class NetworkConfiguration {

    private HttpClientAdapter httpClientAdapter = null; // Optional
    private String collectorUrl = null; // Required if not specifying a httpClientAdapter
    private CookieJar cookieJar = null; // Optional

    // Getters and Setters

    /**
     * Returns the HttpClientAdapter used.
     * @return HttpClientAdapter object
     */
    public HttpClientAdapter getHttpClientAdapter() {
        return httpClientAdapter;
    }

    /**
     * Returns the event collector URL endpoint.
     * @return collector URL
     */
    public String getCollectorUrl() {
        return collectorUrl;
    }

    /**
     * Returns the OkHttp CookieJar used for persisting cookies.
     * @return CookieJar object
     */
    public CookieJar getCookieJar() {
        return cookieJar;
    }

    // Constructors

    /**
     * Create a NetworkConfiguration instance and specify a custom HttpClientAdapter to use
     * (the default is OkHttpClientAdapter).
     *
     * @param httpClientAdapter the adapter to use
     */
    public NetworkConfiguration(HttpClientAdapter httpClientAdapter) {
        this.httpClientAdapter = httpClientAdapter;
    }

    /**
     * Create a NetworkConfiguration instance with a collector endpoint URL. The URL will be used
     * to create the default OkHttpClientAdapter.
     *
     * @param collectorUrl the url for the default httpClientAdapter
     */
    public NetworkConfiguration(String collectorUrl) {
        this.collectorUrl = collectorUrl;
    }

    // Builder methods

    /**
     * Sets a custom HttpClientAdapter (default is OkHttpClientAdapter).
     *
     * @param httpClientAdapter the adapter to use
     * @return itself
     */
    public NetworkConfiguration httpClientAdapter(HttpClientAdapter httpClientAdapter) {
        this.httpClientAdapter = httpClientAdapter;
        return this;
    }

    /**
     * Sets the endpoint url for when a httpClientAdapter is not specified.
     * It will be used to create the default OkHttpClientAdapter.
     *
     * @param collectorUrl the url for the default httpClientAdapter
     * @return itself
     */
    public NetworkConfiguration collectorUrl(String collectorUrl) {
        this.collectorUrl = collectorUrl;
        return this;
    }

    /**
     * Adds a custom CookieJar to be used with OkHttpClientAdapters.
     * Will be ignored if a custom httpClientAdapter is provided.
     *
     * @param cookieJar the CookieJar to use
     * @return itself
     */
    public NetworkConfiguration cookieJar(CookieJar cookieJar) {
        this.cookieJar = cookieJar;
        return this;
    }
}
