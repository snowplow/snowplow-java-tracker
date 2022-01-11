package com.snowplowanalytics.snowplow.tracker.emitter;

import java.util.Collection;
import java.util.List;

import com.snowplowanalytics.snowplow.tracker.payload.TrackerEvent;

public interface EventStore {

    boolean add(TrackerEvent trackerEvent);

    void removeEvents(List<TrackerEvent> eventsList, int numberToRemove);

    int getSize();

    List<TrackerEvent> getAllEvents();
}
