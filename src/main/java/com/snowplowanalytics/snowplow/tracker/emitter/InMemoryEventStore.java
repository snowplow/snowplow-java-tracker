package com.snowplowanalytics.snowplow.tracker.emitter;

import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryEventStore implements EventStore {

    private final AtomicLong batchId = new AtomicLong(1);

    public final ConcurrentLinkedDeque<TrackerPayload> eventBuffer = new ConcurrentLinkedDeque<>();
    public final ConcurrentHashMap<Long, List<TrackerPayload>> eventsBeingSent = new ConcurrentHashMap<>();


    @Override
    public boolean addEvent(TrackerPayload trackerPayload) {
        // Using a new UUID instead of the event UUID for the key
        // in case of problems with non-unique event IDs.
        // Event IDs can be set by the user.
        eventBuffer.add(trackerPayload);

        // returning true just because this was doing an offer to LinkedBlockingQueue before
        // but ConcurrentLinkedDeque is unbounded
        // will always return true!
        return true;
    }

    @Override
    public BatchPayload getEventBatch(int numberToGet) {
        List<TrackerPayload> eventsToSend = new ArrayList<>();

        for (int i = 0; i < numberToGet; i++) {
            TrackerPayload payload = eventBuffer.poll();
            if (payload == null) {
                break;
            }
            eventsToSend.add(payload);
        }

        // the batch of events is removed from the main buffer and added to the pending buffer
        BatchPayload batchedEvents = new BatchPayload(batchId.getAndIncrement(), eventsToSend);
        eventsBeingSent.put(batchedEvents.getBatchId(), batchedEvents.getPayload());

        return batchedEvents;
    }

    @Override
    public void cleanupAfterSendingAttempt(Boolean successfullySent, Long batchId) {
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
        // this might be slow?
        return getAllEvents().size();
    }
}
