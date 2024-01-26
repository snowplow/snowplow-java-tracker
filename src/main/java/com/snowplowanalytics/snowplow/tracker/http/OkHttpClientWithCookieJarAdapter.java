/*
 * Copyright (c) 2024-present Snowplow Analytics Ltd. All rights reserved.
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

// SquareUp
import okhttp3.*;

/**
 * A HttpClient built using OkHttp to send events via GET or POST requests.
 * The adapter is configured to use a CollectorCookieJar to store and send cookies set by the collector.
 * The cookies are stored in memory.
 */
public class OkHttpClientWithCookieJarAdapter extends OkHttpClientAdapter {

    public OkHttpClientWithCookieJarAdapter(String url) {
        super(url, new OkHttpClient.Builder().cookieJar(new CollectorCookieJar()).build());
    }

}
