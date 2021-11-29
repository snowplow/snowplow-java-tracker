/*
 * Copyright (c) 2014-2020 Snowplow Analytics Ltd. All rights reserved.
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableMap;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.apache.http.impl.client.HttpClients;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.assertEquals;

import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

@RunWith(Parameterized.class)
public class HttpClientAdapterTest {
    
    private final MockWebServer mockWebServer;
    private HttpClientAdapter adapter;

    interface HttpClientAdapterProvider {
        HttpClientAdapter provide(String uri);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new HttpClientAdapterProvider() {
                    @Override
                    public HttpClientAdapter provide(String url) {
                        return ApacheHttpClientAdapter.builder()
                                .url(url)
                                .httpClient(HttpClients.createDefault())
                                .build();
                    }
                }},
                {new HttpClientAdapterProvider() {
                    @Override
                    public HttpClientAdapter provide(String url) {
                        OkHttpClient httpClient = new OkHttpClient.Builder()
                            .connectTimeout(1, TimeUnit.SECONDS)
                            .readTimeout(1, TimeUnit.SECONDS)
                            .writeTimeout(1, TimeUnit.SECONDS)
                            .build();
                        return OkHttpClientAdapter.builder()
                                .url(url)
                                .httpClient(httpClient)
                                .build();
                    }
                }
            }
        });
    }

    public HttpClientAdapterTest(HttpClientAdapterProvider httpClientAdapterProvider) throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        adapter = httpClientAdapterProvider.provide(mockWebServer.url("/").toString());
    }

    @Test
    public void get_withSuccessfulStatusCode_isOk() throws Exception {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        // When
        TrackerPayload data = new TrackerPayload();
        data.add("foo", "bar");
        data.add("space", "b a r");
        adapter.get(data);

        // Then
        assertEquals(1, mockWebServer.getRequestCount());
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/i?foo=bar&space=b%20a%20r", recordedRequest.getPath());
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    public void post_withSuccessfulStatusCode_isOk() throws InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        // When
        adapter.post(new SelfDescribingJson("schema", ImmutableMap.of("foo", "bar")));

        // Then
        assertEquals(1, mockWebServer.getRequestCount());
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/com.snowplowanalytics.snowplow/tp2", recordedRequest.getPath());
        assertEquals("{\"schema\":\"schema\",\"data\":{\"foo\":\"bar\"}}", recordedRequest.getBody().readUtf8());
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("application/json; charset=utf-8", recordedRequest.getHeader("Content-Type"));
    }

    @Test
    public void testPostWithNullArgument() {
        Assert.assertThrows(NullPointerException.class, () -> adapter.post(null));
    }

    @Test
    public void testGetWithNullArgument() {
        Assert.assertThrows(NullPointerException.class, () -> adapter.get(null));
    }
}
