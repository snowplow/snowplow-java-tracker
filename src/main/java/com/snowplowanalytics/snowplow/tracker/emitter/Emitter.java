package com.snowplowanalytics.snowplow.tracker.emitter;

import java.util.Map;

public interface Emitter {

    public void emit(Map<String, Object> payload);
}
