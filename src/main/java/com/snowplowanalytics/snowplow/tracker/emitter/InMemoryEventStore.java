package com.snowplowanalytics.snowplow.tracker.emitter;

import com.snowplowanalytics.snowplow.tracker.payload.TrackerEvent;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;

public class InMemoryEventStore implements EventStore {
    public final BlockingQueue<TrackerEvent> eventBuffer = new LinkedBlockingQueue<>();

    @Override
    public boolean add(TrackerEvent trackerEvent) {
        return eventBuffer.offer(trackerEvent);
    }

    @Override
    public void removeEvents(List<TrackerEvent> eventsList, int numberToRemove) {
        eventBuffer.drainTo(eventsList, numberToRemove);
    }

    @Override
    public int getSize() {
        return eventBuffer.size();
    }

    @Override
    public List<TrackerEvent> getAllEvents() {
        return new ArrayList<>(eventBuffer);
    }
}
