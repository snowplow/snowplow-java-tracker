package com.snowplowanalytics.snowplow.tracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.snowplowanalytics.snowplow.tracker.http.ApacheHttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.http.OkHttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;
import org.apache.http.impl.client.HttpClients;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.internal.matchers.Null;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

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
                        return new ApacheHttpClientAdapter(url, HttpClients.createDefault(), new ObjectMapper());
                    }
                }},
                {new HttpClientAdapterProvider() {
                    @Override
                    public HttpClientAdapter provide(String url) {
                        OkHttpClient httpClient = new OkHttpClient();
                        httpClient.setConnectTimeout(1, TimeUnit.SECONDS);
                        httpClient.setReadTimeout(1, TimeUnit.SECONDS);
                        httpClient.setWriteTimeout(1, TimeUnit.SECONDS);
                        
                        return new OkHttpClientAdapter(url, httpClient, new ObjectMapper());
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
    public void testGet() throws Exception {

        // Given
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
        );

        // When
        int code = adapter.get(ImmutableMap.<String, Object>of("foo", "bar"));

        // Then
        assertEquals(1, mockWebServer.getRequestCount());
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/i?foo=bar", recordedRequest.getPath());
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals(200, code);
    }

    @Test
    public void testPost() throws InterruptedException {
        // Given
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
        );

        // When
        int code = adapter.post(new SchemaPayload().setData(ImmutableMap.of("foo", "bar")).setSchema("schema"));

        // Then
        assertEquals(1, mockWebServer.getRequestCount());
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/com.snowplowanalytics.snowplow/tp2", recordedRequest.getPath());
        assertEquals("{\"schema\":\"schema\",\"data\":{\"foo\":\"bar\"}}", recordedRequest.getUtf8Body());
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("application/json; charset=utf-8", recordedRequest.getHeader("Content-Type"));
        assertEquals(200, code);
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

    @Test
    public void testPostWithEmptySchemaPayload() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        adapter.post(new SchemaPayload());
    }

    @Test
    public void testGetWithEmptySchemaPayload() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        adapter.get(new HashMap<String, Object>());
    }
}