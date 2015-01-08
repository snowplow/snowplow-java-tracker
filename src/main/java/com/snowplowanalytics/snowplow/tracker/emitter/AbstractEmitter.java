package com.snowplowanalytics.snowplow.tracker.emitter;

import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;

import java.util.Map;

abstract class AbstractEmitter implements Emitter {

    protected final HttpClientAdapter httpClientAdapter;

    public AbstractEmitter(HttpClientAdapter httpClientAdapter) {
        this.httpClientAdapter = httpClientAdapter;
    }

    @Override
    public abstract void emit(Map<String, Object> payload);
}
