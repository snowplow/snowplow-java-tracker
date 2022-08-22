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

    public void setHttpClientAdapter(HttpClientAdapter httpClientAdapter) {
        this.httpClientAdapter = httpClientAdapter;
    }

    public String getCollectorUrl() {
        return collectorUrl;
    }

    public void setCollectorUrl(String collectorUrl) {
        this.collectorUrl = collectorUrl;
    }

    public CookieJar getCookieJar() {
        return cookieJar;
    }

    public void setCookieJar(CookieJar cookieJar) {
        this.cookieJar = cookieJar;
    }

    // Constructor

    public NetworkConfiguration() {
        httpClientAdapter = null;
        collectorUrl = null;
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
