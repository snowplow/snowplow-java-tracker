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
package com.snowplowanalytics.snowplow.tracker.http;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

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
                        return new ApacheHttpClientAdapter(url, HttpClients.createDefault());
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
                        return new OkHttpClientAdapter(url, httpClient);
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

        String eventId = data.getEventId();
        String dtm = Long.toString(data.getDeviceCreatedTimestamp());

        // Then
        assertEquals(1, mockWebServer.getRequestCount());
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        String expectedString = "/i?eid=" + eventId + "&dtm=" + dtm + "&foo=bar&space=b%20a%20r";
        assertEquals(expectedString, recordedRequest.getPath());
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    public void post_withSuccessfulStatusCode_isOk() throws InterruptedException {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        // When
        adapter.post(new SelfDescribingJson("schema", Collections.singletonMap("foo", "bar")));

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

    @Test
    public void testRequestWithCookies() throws IOException, InterruptedException {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.SECONDS)
                .cookieJar(new CollectorCookieJar())
                .build();
        adapter = new OkHttpClientAdapter(mockWebServer.url("/").toString(), httpClient);

        mockWebServer.enqueue(new MockResponse().addHeader("Set-Cookie", "sp=test"));

        SelfDescribingJson payload = new SelfDescribingJson("schema", Collections.singletonMap("foo", "bar"));

        adapter.post(payload);
        adapter.post(payload);

        assertEquals(2, mockWebServer.getRequestCount());
        mockWebServer.takeRequest();
        RecordedRequest recordedRequest2 = mockWebServer.takeRequest();

        assertEquals("sp=test", recordedRequest2.getHeader("Cookie"));

        mockWebServer.shutdown();
    }
}
