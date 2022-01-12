package com.snowplowanalytics.snowplow.tracker.emitter;

import java.util.Collection;
import java.util.List;

import com.snowplowanalytics.snowplow.tracker.payload.TrackerEvent;

public interface EventStore {

    boolean add(TrackerEvent trackerEvent);

    List<TrackerEvent> removeEvents(int numberToRemove);

    int getSize();

    List<TrackerEvent> getAllEvents();
}
