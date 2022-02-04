package com.snowplowanalytics.snowplow.tracker.emitter;

import java.util.List;

import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

public interface EventStore {

    boolean addEvent(TrackerPayload trackerPayload);

    List<EmitterPayload> getEvents(int numberToRemove);

    void removeEvents(List<String> eventIds);

    int getSize();
}
