package com.snowplowanalytics.snowplow.tracker.emitter;

import com.snowplowanalytics.snowplow.tracker.payload.TrackerEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;

public class InMemoryStorage implements EventStore {
    // Queue for immediate buffering of events
    @Override
    public BlockingQueue<TrackerEvent> getInitialEventBuffer() {
        return eventBuffer;
    }

    // Queue for storing events until bufferSize is reached
    @Override
    public BlockingQueue<TrackerEvent> getStagingEventBuffer() {
        return eventsToSend;
    }

    // Queue for immediate buffering of events
    public final BlockingQueue<TrackerEvent> eventBuffer = new LinkedBlockingQueue<>();

    // Queue for storing events until bufferSize is reached
    public final BlockingQueue<TrackerEvent> eventsToSend = new LinkedBlockingQueue<>();

    // maybe rename this?
    @Override
    public boolean add(TrackerEvent trackerEvent) {
        return eventBuffer.offer(trackerEvent);
    }

    @Override
    public void retrieveEvents(List<TrackerEvent> eventsList) {
        eventsToSend.drainTo(eventsList);
    }
}
