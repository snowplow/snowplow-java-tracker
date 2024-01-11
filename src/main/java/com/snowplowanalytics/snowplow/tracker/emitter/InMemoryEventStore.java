/*
 * Copyright (c) 2014-present Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.snowplowanalytics.snowplow.tracker.emitter;

import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Buffers events (as TrackerPayloads) in memory for sending via the BatchEmitter.
 *
 * The TrackerPayloads are stored in a queue. When the BatchEmitter calls {@link #getEventsBatch(int)},
 * the chosen number of TrackerPayloads are removed from the queue. The batch is added to a map of payloads
 * that are currently being sent, and wrapped as a BatchPayload. This BatchPayload wrapper is returned to the
 * Emitter.
 *
 * If the POST request is successful, the payloads are deleted from the map.
 * If not, they are removed from the map and reinserted into the queue to be sent again.
 */
public class InMemoryEventStore implements EventStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryEventStore.class);
    private static final int DEFAULT_BUFFER_SIZE = 10000;
    private final AtomicLong batchId = new AtomicLong(1);

    private final LinkedBlockingDeque<TrackerPayload> eventBuffer;
    private final ConcurrentHashMap<Long, List<TrackerPayload>> eventsBeingSent = new ConcurrentHashMap<>();

    /**
     * Create a InMemoryEventStore object with custom queue capacity. The default is 10 000 events.
     * @param bufferCapacity the maximum number of events to buffer at once
     */
    public InMemoryEventStore(int bufferCapacity) {
        eventBuffer = new LinkedBlockingDeque<>(bufferCapacity);
    }

    /**
     * Create a InMemoryEventStore object with default buffer size (10 000 events).
     */
    public InMemoryEventStore() {
        this(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Add TrackerPayload to buffer. Returns false if the buffer was full.
     * Note that the event is lost in this case.
     *
     * @param trackerPayload the payload to add
     * @return success or not
     */
    @Override
    public boolean addEvent(TrackerPayload trackerPayload) {
        return eventBuffer.offer(trackerPayload);
    }

    /**
     * Remove some TrackerPayloads from the buffer. They are wrapped as a BatchPayload to return,
     * and also stored in a separate collection inside InMemoryEventStore until the result of their POST request is known.
     *
     * @param numberToGet how many payloads to get
     * @return a BatchPayload wrapper, or null
     */
    @Override
    public BatchPayload getEventsBatch(int numberToGet) {
        List<TrackerPayload> eventsToSend = new ArrayList<>();

        synchronized (eventBuffer) {
            if (eventBuffer.size() < numberToGet) {
                return null;
            }
            eventBuffer.drainTo(eventsToSend, numberToGet);
        }

        // The batch of events is wrapped as a BatchPayload
        // They're also added to the "pending" event buffer, the eventsBeingSent HashMap
        BatchPayload batchedEvents = new BatchPayload(batchId.getAndIncrement(), eventsToSend);
        eventsBeingSent.put(batchedEvents.getBatchId(), batchedEvents.getPayloads());
        return batchedEvents;
    }

    /**
     * Finish processing events after a request has been made. If the request was successful,
     * the events are deleted from the InMemoryEventStore. If not, they are reinserted at the beginning
     * of the buffer queue for another attempt.
     *
     * @param needRetry if true, move events back to the buffer instead of deleting
     * @param batchId the ID of the batch of events
     * @return newer TrackerPayloads deleted from the queue to make space for older payloads
     */
    @Override
    public List<TrackerPayload> cleanupAfterSendingAttempt(boolean needRetry, long batchId) {
        // Events that successfully sent are deleted from the pending buffer
        List<TrackerPayload> events = eventsBeingSent.remove(batchId);
        List<TrackerPayload> removedEvents = new ArrayList<>();

        // Events that didn't send are inserted at the head of the eventBuffer
        // for immediate resending.
        if (needRetry) {
            while (events.size() > 0) {
                TrackerPayload payloadToReinsert = events.remove(0);
                boolean result = eventBuffer.offerFirst(payloadToReinsert);
                if (!result) {
                    LOGGER.error("Event buffer is full. Dropping newer payload to reinsert older payload");
                    removedEvents.add(eventBuffer.removeLast());
                    eventBuffer.offerFirst(payloadToReinsert);
                }
            }
        }
        return removedEvents;
    }

    /**
     * Get a copy of all the TrackerPayloads in the buffer. This does not include any events
     * currently being sent by the BatchEmitter.
     *
     * @return List of all the stored events
     */
    @Override
    public List<TrackerPayload> getAllEvents() {
        TrackerPayload[] events = eventBuffer.toArray(new TrackerPayload[0]);
        return Arrays.asList(events);
    }

    /**
     * Get the current size of the buffer. This does not include any events
     * currently being sent by the BatchEmitter.
     *
     * @return number of events currently in the buffer
     */
    @Override
    public int size() {
        return eventBuffer.size();
    }
}
