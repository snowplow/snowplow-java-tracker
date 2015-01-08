package com.snowplowanalytics.snowplow.tracker.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;

import java.util.Map;


public abstract class AbstractHttpClientAdapter implements HttpClientAdapter {

    private final ObjectMapper objectMapper;

    protected AbstractHttpClientAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected abstract int doPost(String payload);

    protected abstract int doGet(Map<String, Object> payload);

    @Override
    public void post(SchemaPayload payload) {
        Preconditions.checkNotNull(payload);
        Preconditions.checkNotNull(payload.getMap());
        Preconditions.checkArgument(payload.getMap().size() > 0, "empty parameters given");

        try {
            String body = objectMapper.writeValueAsString(payload.getMap());
            int status = doPost(body);
            if (200 != status) {
                throw new RuntimeException(String.format("Failed to send event using POST. Got http response %d", status));
            }
        } catch (JsonProcessingException e) {
           throw new RuntimeException(e);
        }
    }

    @Override
    public void get(Map<String, Object> payload) {
        Preconditions.checkNotNull(payload);
        Preconditions.checkArgument(payload.size() > 0, "empty parameters given");

        int status = doGet(payload);
        if (200 != status) {
            throw new RuntimeException(String.format("Failed to send event using GET. Got http response %d", status));
        }
    }

}
