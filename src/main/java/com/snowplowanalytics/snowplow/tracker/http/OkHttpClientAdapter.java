package com.snowplowanalytics.snowplow.tracker.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snowplowanalytics.snowplow.tracker.Constants;
import com.squareup.okhttp.*;

import java.util.Iterator;
import java.util.Map;


public class OkHttpClientAdapter extends AbstractHttpClientAdapter {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final String uri;

    private OkHttpClient httpClient;

    public OkHttpClientAdapter(String uri, OkHttpClient httpClient, ObjectMapper objectMapper) {
        super(objectMapper);
        this.uri = uri;
        this.httpClient = httpClient;
    }


    public int doGet(Map<String, Object> payload) {
        StringBuilder urlBuilder = new StringBuilder(uri)
                .append("/i?");

        Iterator<String> iterator = payload.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            urlBuilder.append(key)
                    .append("=")
                    .append(payload.get(key));
            
            if (iterator.hasNext()) {
                urlBuilder.append("&");
            }
        }
        Request request = new Request.Builder().url(urlBuilder.toString()).build();
        try {
            Response response = httpClient.newCall(request).execute();
            return response.code();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int doPost(String payload) {
        try {
            RequestBody body = RequestBody.create(JSON, payload);
            Request request = new Request.Builder()
                    .url(uri + "/" + Constants.PROTOCOL_VENDOR + "/" + Constants.PROTOCOL_VERSION)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .post(body)
                    .build();
            Response response = httpClient.newCall(request).execute();
            return response.code();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
