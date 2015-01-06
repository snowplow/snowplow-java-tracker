/*
 * Copyright (c) 2014 Snowplow Analytics Ltd. All rights reserved.
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

package com.snowplowanalytics.snowplow.tracker.emitter;

import com.snowplowanalytics.snowplow.tracker.Constants;
import com.snowplowanalytics.snowplow.tracker.payload.Payload;
import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Emitter {

    private URIBuilder uri;
    private RequestMethod requestMethod = RequestMethod.Synchronous;
    private CloseableHttpClient httpClient;
    private CloseableHttpAsyncClient httpAsyncClient;
    private final ArrayList<Payload> buffer = new ArrayList<Payload>();

    private final Logger logger = LoggerFactory.getLogger(Emitter.class);

    protected BufferOption option = BufferOption.Default;
    protected RequestCallback requestCallback;
    protected HttpMethod httpMethod = HttpMethod.GET;

    /**
     * Default constructor does nothing.
     */
    public Emitter() {

    }

    /**
     * Create an Emitter instance with a collector URL.
     * @param URI The collector URL. Don't include "http://" - this is done automatically.
     */
    public Emitter(String URI) {
        this(URI, HttpMethod.GET, null);
    }

    /**
     * Create an Emitter instance with a collector URL, and callback function.
     * @param URI The collector URL. Don't include "http://" - this is done automatically.
     * @param callback The callback function to handle success/failure cases when sending events.
     */
    public Emitter(String URI, RequestCallback callback) {
        this(URI, HttpMethod.GET, callback);
    }

    /**
     * Create an Emitter instance with a collector URL,
     * @param URI The collector URL. Don't include "http://" - this is done automatically.
     * @param httpMethod The HTTP request method. If GET, <code>BufferOption</code> is set to <code>Instant</code>.
     */
    public Emitter(String URI, HttpMethod httpMethod) {
        this(URI, httpMethod, null);
    }

    /**
     * Create an Emitter instance with a collector URL and HttpMethod to send requests.
     * @param URI The collector URL. Don't include "http://" - this is done automatically.
     * @param httpMethod The HTTP request method. If GET, <code>BufferOption</code> is set to <code>Instant</code>.
     * @param callback The callback function to handle success/failure cases when sending events.
     */
    public Emitter(String URI, HttpMethod httpMethod, RequestCallback callback) {
        if (httpMethod == HttpMethod.GET) {
            uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(URI)
                    .setPath("/i");
        } else { // POST
            uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(URI)
                    .setPath("/" + Constants.PROTOCOL_VENDOR + "/" + Constants.PROTOCOL_VERSION);
        }
        this.requestCallback = callback;
        this.httpMethod = httpMethod;
        this.httpClient = HttpClients.createDefault();

        if (httpMethod == HttpMethod.GET) {
            this.setBufferOption(BufferOption.Instant);
        }

    }

    /**
     * Sets whether the buffer should send events instantly or after the buffer has reached
     * it's limit. By default, this is set to BufferOption Default.
     * @param option Set the BufferOption enum to Instant send events upon creation.
     */
    public void setBufferOption(BufferOption option) {
        this.option = option;
    }

    /**
     * Sets whether requests should be sent synchronously or asynchronously.
     * @param option The HTTP request method
     */
    public void setRequestMethod(RequestMethod option) {
        this.requestMethod = option;
        this.httpAsyncClient = HttpAsyncClients.createDefault();
        this.httpAsyncClient.start();
    }

    /**
     * Add event payloads to the emitter's buffer
     * @param payload Payload to be added
     * @return Returns the boolean value if the event was successfully added to the buffer
     */
    public boolean addToBuffer(Payload payload) {
        boolean ret = buffer.add(payload);
        if (buffer.size() == option.getCode())
            flushBuffer();
        return ret;
    }

    /**
     * Sends all events in the buffer to the collector.
     */
    public void flushBuffer() {
        if (buffer.isEmpty()) {
            logger.debug("Buffer is empty, exiting flush operation..");
            return;
        }

        if (httpMethod == HttpMethod.GET) {
            int success_count = 0;
            LinkedList<Payload> unsentPayloads = new LinkedList<Payload>();

            for (Payload payload : buffer) {
                int status_code = sendGetData(payload).getStatusLine().getStatusCode();
                if (status_code == 200)
                    success_count++;
                else
                    unsentPayloads.add(payload);
            }

            if (unsentPayloads.size() == 0) {
                if (requestCallback != null)
                    requestCallback.onSuccess(success_count);
            }
            else if (requestCallback != null)
                requestCallback.onFailure(success_count, unsentPayloads);

        } else if (httpMethod == HttpMethod.POST) {
            LinkedList<Payload> unsentPayload = new LinkedList<Payload>();

            SchemaPayload postPayload = new SchemaPayload();
            postPayload.setSchema(Constants.SCHEMA_PAYLOAD_DATA);

            ArrayList<Map> eventMaps = new ArrayList<Map>();
            for (Payload payload : buffer) {
                eventMaps.add(payload.getMap());
            }
            postPayload.setData(eventMaps);

            int status_code = sendPostData(postPayload).getStatusLine().getStatusCode();
            if (status_code == 200 && requestCallback != null)
                requestCallback.onSuccess(buffer.size());
            else if (requestCallback != null){
                unsentPayload.add(postPayload);
                requestCallback.onFailure(0, unsentPayload);
            }
        }

        // Empties current buffer
        buffer.clear();
    }

    protected HttpResponse sendPostData(Payload payload) {
        HttpPost httpPost = new HttpPost(uri.toString());
        httpPost.addHeader("Content-Type", "application/json; charset=utf-8");
        HttpResponse httpResponse = null;

        try {
            StringEntity params = new StringEntity(payload.toString());
            httpPost.setEntity(params);
            if (requestMethod == RequestMethod.Asynchronous) {
                Future<HttpResponse> future = httpAsyncClient.execute(httpPost, null);
                httpResponse = future.get();
            } else {
                httpResponse = httpClient.execute(httpPost);
            }
            logger.debug(httpResponse.getStatusLine().toString());
        } catch (UnsupportedEncodingException e) {
            logger.error("Encoding exception with the payload.");
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("Error when sending HTTP POST.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            logger.error("Interruption error when sending HTTP POST request.");
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return httpResponse;
    }

    @SuppressWarnings("unchecked")
    protected HttpResponse sendGetData(Payload payload) {
        HashMap hashMap = (HashMap) payload.getMap();
        Iterator<String> iterator = hashMap.keySet().iterator();
        HttpResponse httpResponse = null;

        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = (String) hashMap.get(key);
            uri.setParameter(key, value);
        }

        try {
            HttpGet httpGet = new HttpGet(uri.build());
            if (requestMethod == RequestMethod.Asynchronous) {
                Future<HttpResponse> future = httpAsyncClient.execute(httpGet, null);
                httpResponse = future.get();
            } else {
                httpResponse = httpClient.execute(httpGet);
            }
            logger.debug(httpResponse.getStatusLine().toString());
        } catch (IOException e) {
            logger.error("Error when sending HTTP GET error.");
            e.printStackTrace();
        } catch (URISyntaxException e) {
            logger.error("Error when creating HTTP GET request. Probably parsing error..");
            e.printStackTrace();
        } catch (InterruptedException e) {
            logger.error("Interruption error when sending HTTP GET request.");
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return httpResponse;
    }
}
