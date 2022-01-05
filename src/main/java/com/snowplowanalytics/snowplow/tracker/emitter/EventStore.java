package com.snowplowanalytics.snowplow.tracker.emitter;

import java.util.Collection;
import java.util.List;

import com.snowplowanalytics.snowplow.tracker.payload.TrackerEvent;

public interface EventStore {

    Collection getInitialEventBuffer();
    Collection getStagingEventBuffer();

    boolean add(TrackerEvent trackerEvent);

    void retrieveEvents(List<TrackerEvent> eventsList);

}
