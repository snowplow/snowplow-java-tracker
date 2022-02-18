package com.snowplowanalytics.snowplow.tracker.emitter;

import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryEventStore implements EventStore {
    private final AtomicLong batchId = new AtomicLong(1);

    public final LinkedBlockingDeque<TrackerPayload> eventBuffer = new LinkedBlockingDeque<>();
    public final ConcurrentHashMap<Long, List<TrackerPayload>> eventsBeingSent = new ConcurrentHashMap<>();


    @Override
    public boolean addEvent(TrackerPayload trackerPayload) {
        return eventBuffer.offer(trackerPayload);
    }

    @Override
    public BatchPayload getEventBatch(int numberToGet) {
        List<TrackerPayload> eventsToSend = new ArrayList<>();

        eventBuffer.drainTo(eventsToSend, numberToGet);

        // The batch of events is wrapped as a BatchPayload
        // They're also added to the "pending" event buffer, the eventsBeingSent HashMap
        BatchPayload batchedEvents = new BatchPayload(batchId.getAndIncrement(), eventsToSend);
        eventsBeingSent.put(batchedEvents.getBatchId(), batchedEvents.getPayload());
        return batchedEvents;
    }

    @Override
    public void cleanupAfterSendingAttempt(boolean successfullySent, long batchId) {
        // Events that successfully sent are deleted from the pending buffer
        List<TrackerPayload> events = eventsBeingSent.remove(batchId);

        // Events that didn't send are inserted at the head of the eventBuffer
        // for immediate resending.
        if (!successfullySent) {
            for (TrackerPayload event : events) {
                eventBuffer.addFirst(event);
            }
        }
    }

    @Override
    public List<TrackerPayload> getAllEvents() {
        TrackerPayload[] events = eventBuffer.toArray(new TrackerPayload[0]);
        return Arrays.asList(events);
    }

    @Override
    public int getSize() {
        return eventBuffer.size();
    }
}
