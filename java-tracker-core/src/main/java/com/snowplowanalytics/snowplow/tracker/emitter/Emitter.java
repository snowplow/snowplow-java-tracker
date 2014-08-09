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

import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Emitter {

    private URIBuilder uri;
    private BufferOption option = BufferOption.Default;
    private HttpMethod httpMethod = HttpMethod.GET;
    private RequestMethod requestMethod = RequestMethod.Synchronous;
    private CloseableHttpClient httpClient;
    private CloseableHttpAsyncClient httpAsyncClient;
    private final ArrayList<Payload> buffer = new ArrayList<Payload>();

    private final Logger logger = LoggerFactory.getLogger(Emitter.class);

    /**
     * @param URI The collector URL. Don't include "http://" - this is done automatically.
     */
    public Emitter(String URI) {
        new Emitter(URI, HttpMethod.GET);
    }

    /**
     * @param URI The collector URL. Don't include "http://" - this is done automatically.
     * @param httpMethod The HTTP request method
     */
    public Emitter(String URI, HttpMethod httpMethod) {
        if (httpMethod == HttpMethod.GET) {
            uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(URI)
                    .setPath("/i");
        } else { // POST
            uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(URI)
                    .setPath("/" + Constants.DEFAULT_VENDOR + "/tp2");
        }
        this.httpMethod = httpMethod;
        this.httpClient = HttpClients.createDefault();
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
            for (Payload payload : buffer) {
                sendGetData(payload);
            }
        } else if (httpMethod == HttpMethod.POST) {
            SchemaPayload postPayload = new SchemaPayload();
            postPayload.setSchema(Constants.SCHEMA_PAYLOAD_DATA);

            ArrayList<Map> eventMaps = new ArrayList<Map>();
            for (Payload payload : buffer) {
                eventMaps.add(payload.getMap());
            }
            postPayload.setData(eventMaps);

            sendPostData(postPayload);
        }
    }

    private void sendPostData(Payload payload) {
        HttpPost httpPost = new HttpPost(uri.toString());
        httpPost.addHeader("Content-Type", "application/json; charset=utf-8");

        try {
            StringEntity params = new StringEntity(payload.toString());
            HttpResponse httpResponse;
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

    }

    private void sendGetData(Payload payload) {
        JsonNode eventMap = payload.getNode();
        Iterator<String> iterator = eventMap.fieldNames();

        URIBuilder requestUri = uri;
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = eventMap.get(key).toString();
            // Removing the end quotes in 'value' is an ugly hack
            if (value.charAt(0) ==  '\"')
                value = value.substring(1,eventMap.get(key).toString().length()-1);

            requestUri.setParameter(key, value);
        }

        try {
            HttpGet httpGet = new HttpGet(requestUri.build());
            HttpResponse httpResponse;
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
    }
}
