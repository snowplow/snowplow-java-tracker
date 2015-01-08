package com.snowplowanalytics.snowplow.tracker.emitter;

import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;

import java.util.Map;

public class SimpleEmitter extends AbstractEmitter {

    public SimpleEmitter(HttpClientAdapter httpClientAdapter) {
        super(httpClientAdapter);
    }

    @Override
    public void emit(Map<String, Object> payload) {
        httpClientAdapter.get(payload);
    }
}
