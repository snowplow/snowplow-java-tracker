package com.snowplowanalytics.snowplow.tracker.http;

import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;

import java.util.Map;

public interface HttpClientAdapter {

    void post(SchemaPayload payload);
    void get(Map<String, Object> payload);
}
