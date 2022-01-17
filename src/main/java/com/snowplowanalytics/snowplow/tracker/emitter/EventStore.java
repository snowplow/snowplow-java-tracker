package com.snowplowanalytics.snowplow.tracker.emitter;

import java.util.List;

import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

public interface EventStore {

    boolean add(TrackerPayload trackerPayload);

    List<TrackerPayload> removeEvents(int numberToRemove);

    int getSize();

    List<TrackerPayload> getAllEvents();
}
