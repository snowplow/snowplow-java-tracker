package com.snowplowanalytics.snowplow.tracker.emitter;

import java.util.Map;

public interface Emitter {

    void emit(Map<String, Object> payload);
}
