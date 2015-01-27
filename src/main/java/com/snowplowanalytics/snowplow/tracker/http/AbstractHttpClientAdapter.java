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

    protected abstract int doPost(byte[] payload);

    protected abstract int doGet(Map<String, Object> payload);

    @Override
    public int post(SchemaPayload payload) {
        Preconditions.checkNotNull(payload);
        Preconditions.checkNotNull(payload.getMap());
        Preconditions.checkArgument(payload.getMap().size() > 0, "empty parameters given");

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(payload.getMap());
            return doPost(bytes);
        } catch (JsonProcessingException e) {
           throw new RuntimeException(e);
        }
    }

    @Override
    public int get(Map<String, Object> payload) {
        Preconditions.checkNotNull(payload);
        Preconditions.checkArgument(payload.size() > 0, "empty parameters given");
        
        return doGet(payload);
    }
}
