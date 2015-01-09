package com.snowplowanalytics.snowplow.tracker.http;

import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;

import java.util.Map;

public interface HttpClientAdapter {

    int post(SchemaPayload payload);
    int get(Map<String, Object> payload);
}
