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

    private HttpClientAdapter httpClientAdapter; // Optional
    private String collectorUrl; // Required if not specifying a httpClientAdapter
    private CookieJar cookieJar; // Optional

    // Getters and Setters

    public HttpClientAdapter getHttpClientAdapter() {
        return httpClientAdapter;
    }

    public String getCollectorUrl() {
        return collectorUrl;
    }

    public CookieJar getCookieJar() {
        return cookieJar;
    }

    // Constructor

    /**
     * Create a NetworkConfiguration instance. No arguments are required for this constructor, but you will need to
     * provide either a collector URL or an HttpClientAdapter object before using this NetworkConfiguration.
     */
    public NetworkConfiguration() {
        httpClientAdapter = null;
        collectorUrl = null;
        cookieJar = null;
    }

    /**
     * Create a NetworkConfiguration instance and specify a custom HttpClientAdapter to use
     * (the default is OkHttpClientAdapter).
     *
     * @param httpClientAdapter the adapter to use
     */
    public NetworkConfiguration(HttpClientAdapter httpClientAdapter) {
        this.httpClientAdapter = httpClientAdapter;
        collectorUrl = null;
        cookieJar = null;
    }

    /**
     * Create a NetworkConfiguration instance with a collector endpoint URL. The URL will be used
     * to create the default OkHttpClientAdapter.
     *
     * @param collectorUrl the url for the default httpClientAdapter
     */
    public NetworkConfiguration(String collectorUrl) {
        httpClientAdapter = null;
        this.collectorUrl = collectorUrl;
        cookieJar = null;
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
