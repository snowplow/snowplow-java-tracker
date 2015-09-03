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
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

// Google
import com.google.common.collect.ImmutableMap;

// SquareUp
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

// Apache
import org.apache.http.impl.client.HttpClients;

// JUnit
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.assertEquals;

// This library
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

@RunWith(Parameterized.class)
public class HttpClientAdapterTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
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
                        return new ApacheHttpClientAdapter(url, HttpClients.createDefault());
                    }
                }},
                {new HttpClientAdapterProvider() {
                    @Override
                    public HttpClientAdapter provide(String url) {
                        OkHttpClient httpClient = new OkHttpClient();
                        httpClient.setConnectTimeout(1, TimeUnit.SECONDS);
                        httpClient.setReadTimeout(1, TimeUnit.SECONDS);
                        httpClient.setWriteTimeout(1, TimeUnit.SECONDS);
                        return new OkHttpClientAdapter(url, httpClient);
                    }
                }
            }
        });
    }

    public HttpClientAdapterTest(HttpClientAdapterProvider httpClientAdapterProvider) throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.play();
        adapter = httpClientAdapterProvider.provide(mockWebServer.getUrl("").toString());
    }

    @Test
    public void get_withSuccessfulStatusCode_isOk() throws Exception {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        // When
        TrackerPayload data = new TrackerPayload();
        data.add("foo", "bar");
        adapter.get(data);

        // Then
        assertEquals(1, mockWebServer.getRequestCount());
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/i?foo=bar", recordedRequest.getPath());
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
        assertEquals("{\"schema\":\"schema\",\"data\":{\"foo\":\"bar\"}}", recordedRequest.getUtf8Body());
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("application/json; charset=utf-8", recordedRequest.getHeader("Content-Type"));
    }

    @Test
    public void testPostWithNullArgument() throws Exception {
        expectedException.expect(NullPointerException.class);
        adapter.post(null);
    }

    @Test
    public void testGetWithNullArgument() throws Exception {
        expectedException.expect(NullPointerException.class);
        adapter.get(null);
    }
}
