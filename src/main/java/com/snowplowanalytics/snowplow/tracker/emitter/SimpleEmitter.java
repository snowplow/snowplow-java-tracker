package com.snowplowanalytics.snowplow.tracker.emitter;

import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;

import java.util.Map;

public class SimpleEmitter extends AbstractEmitter {

    public SimpleEmitter(HttpClientAdapter httpClientAdapter) {
        super(httpClientAdapter);
    }

    @Override
    public void emit(Map<String, Object> payload) {
        int statusCode = httpClientAdapter.get(payload);
        if (statusCode != 200) {
            throw new RuntimeException(String.format("Failed to emit event to snowplow, got status %d", statusCode));
        }
    }
}
