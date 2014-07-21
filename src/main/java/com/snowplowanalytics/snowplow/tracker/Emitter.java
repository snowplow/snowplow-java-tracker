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

package com.snowplowanalytics.snowplow.tracker;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

public class Emitter {

    private URIBuilder uri;
    private EmitterHttpMethod httpMethod = EmitterHttpMethod.GET;
    private final ArrayList<Payload> buffer = new ArrayList<Payload>();
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    private final Logger logger = LoggerFactory.getLogger(Emitter.class);

    public Emitter(String URI, EmitterHttpMethod httpMethod) {
        uri = new URIBuilder()
                .setScheme("http")
                .setHost(URI)
                .setPath("/i");
        this.httpMethod = httpMethod;
    }

    public boolean addToBuffer(Payload payload) {
        boolean ret = buffer.add(payload);
        if (buffer.size() == 10)
            flushBuffer();
        return ret;
    }

    public void flushBuffer() {
        if (httpMethod == EmitterHttpMethod.GET) {
            for (Payload payload : buffer) {
                sendGetData(payload);
            }
        } else if (httpMethod == EmitterHttpMethod.POST) {
            Payload postPayload = new TrackerPayload();
            postPayload.setSchema(Constants.SCHEMA_PAYLOAD_DATA);
            postPayload.setData(buffer);

            sendPostData(postPayload);
        }
    }

    private void sendPostData(Payload payload) {
        HttpPost httpPost = new HttpPost(uri.toString());
        httpPost.addHeader("Content-Type", "application/json");

        try {
            StringEntity params = new StringEntity(payload.toString());
            httpPost.setEntity(params);
            httpClient.execute(httpPost);
        } catch (UnsupportedEncodingException e) {
            logger.error("Encoding exception with the payload.");
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("Error when sending HTTP POST.");
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
            httpClient.execute(httpGet);
        } catch (IOException e) {
            logger.error("Error when sending HTTP GET error.");
            e.printStackTrace();
        } catch (URISyntaxException e) {
            logger.error("Error when creating HTTP GET request. Probably parsing error..");
            e.printStackTrace();
        }
    }
}
