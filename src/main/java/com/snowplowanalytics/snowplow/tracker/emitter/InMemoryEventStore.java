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
    public List<TrackerEvent> removeEvents(int numberToRemove) {
        // if numberToRemove is greater than the number of events present,
        // it will return all the events (there's no error)
        List<TrackerEvent> eventsList = new ArrayList<>();
        eventBuffer.drainTo(eventsList, numberToRemove);
        return eventsList;
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
