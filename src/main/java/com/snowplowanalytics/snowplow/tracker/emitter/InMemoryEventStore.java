package com.snowplowanalytics.snowplow.tracker.emitter;

import com.snowplowanalytics.snowplow.tracker.payload.TrackerEvent;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;

public class InMemoryEventStore implements EventStore {
    public final BlockingQueue<TrackerPayload> eventBuffer = new LinkedBlockingQueue<>();

    @Override
    public boolean add(TrackerPayload trackerPayload) {
        return eventBuffer.offer(trackerPayload);
    }

    @Override
    public List<TrackerPayload> removeEvents(int numberToRemove) {
        // if numberToRemove is greater than the number of events present,
        // it will return all the events (there's no error)
        List<TrackerPayload> eventsList = new ArrayList<>();
        eventBuffer.drainTo(eventsList, numberToRemove);
        return eventsList;
    }

    @Override
    public int getSize() {
        return eventBuffer.size();
    }

    @Override
    public List<TrackerPayload> getAllEvents() {
        return new ArrayList<>(eventBuffer);
    }
}
