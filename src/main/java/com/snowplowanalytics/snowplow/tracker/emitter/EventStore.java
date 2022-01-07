package com.snowplowanalytics.snowplow.tracker.emitter;

import java.util.Collection;
import java.util.List;

import com.snowplowanalytics.snowplow.tracker.payload.TrackerEvent;

public interface EventStore {

    boolean add(TrackerEvent trackerEvent);

    void removeAllEvents(List<TrackerEvent> eventsList);

    // this can be removed if Emitter.getBuffer is changed to return just the size
    List<TrackerEvent> retrieveAllEvents();

    void prepareAllEventsForRemoval();

    long getSize();
}
