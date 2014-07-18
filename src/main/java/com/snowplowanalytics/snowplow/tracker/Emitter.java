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

enum HttpMethod {
    GET,
    POST
}

public class Emitter {

    private URIBuilder uri;
    private HttpMethod httpMethod = HttpMethod.GET;
    private final ArrayList<Payload> buffer = new ArrayList<Payload>();
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    private final Logger logger = LoggerFactory.getLogger(Emitter.class);

    public Emitter(String URI, HttpMethod httpMethod) {
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

//    public void sendStuff() {
//        System.out.println(uri.toString());
////        HttpPost httpPost = new HttpPost(uri.toString());
////        HttpGet httpGet = new HttpGet(uri.toString());
//        CloseableHttpResponse response;
//
////        httpPost.addHeader("Content-Type", "application/json");
//
//        Map foo = new LinkedHashMap<String, String>();
//        ArrayList<String> bar = new ArrayList<String>();
//        ArrayList<Payload> arrayList = new ArrayList<Payload>();
//        bar.add("somebar");
//        bar.add("somebar2");
//        foo.put("myKey", "my Value");
//        foo.put("mehh", bar);
//        Payload payload = new TrackerPayload();
//        payload.addMap(foo, false, "cx", "co");
//        Payload payload1 = new TrackerPayload();
//        payload1.add("second", "payload");
//        arrayList.add(payload);
//        arrayList.add(payload1);
//
//
//        try {
//            StringEntity params = new StringEntity(arrayList.toString());
////            httpPost.setEntity(params);
//            String encodedParameters = URLEncoder.encode(payload.toString(), "UTF-8");
//
//            HttpGet httpGet = new HttpGet(uri.toString() + "?" +  encodedParameters);
//
//            System.out.println("Encoded:");
//            System.out.println(uri.toString());
//            httpClient.execute(httpGet);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public void flushBuffer() {
        if (httpMethod == HttpMethod.GET) {
            for (Payload payload : buffer) {
                sendGetData(payload);
            }
        } else if (httpMethod == HttpMethod.POST) {
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
