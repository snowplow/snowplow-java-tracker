/*
 * Copyright (c) 2014-2021 Snowplow Analytics Ltd. All rights reserved.
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

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;
import com.snowplowanalytics.snowplow.tracker.constants.Constants;
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerEvent;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An emitter that emit a batch of events in a single call 
 * It uses the post method of under-laying http adapter
 */
public class BatchEmitter extends AbstractEmitter implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchEmitter.class);
    private static final AtomicInteger EVENTS_CHECK_THREAD_NUMBER = new AtomicInteger(1);
    private static final String EVENTS_CHECK_THREAD_NAME_PREFIX = "snowplow-emitter-areThereEvents-thread-";

    private final Thread areThereEventsToSend;
    private boolean isClosing = false;

    private int bufferSize = 1;
    private InMemoryEventStore storage;

    private final long closeTimeout = 5;

    public static abstract class Builder<T extends Builder<T>> extends AbstractEmitter.Builder<T> {

        private int bufferSize = 50; // Optional
        private InMemoryEventStore storage = new InMemoryEventStore();

        /**
         * @param bufferSize The count of events to buffer before sending
         * @return itself
         */
        public T bufferSize(final int bufferSize) {
            this.bufferSize = bufferSize;
            return self();
        }

        public T storage(final InMemoryEventStore storage) {
            this.storage = storage;
            return self();
        }

        public BatchEmitter build() {
            return new BatchEmitter(this);
        }
    }

    private static class Builder2 extends Builder<Builder2> {
        @Override
        protected Builder2 self() {
            return this;
        }
    }

    public static Builder<?> builder() {
        return new Builder2();
    }

    protected BatchEmitter(final Builder<?> builder) {
        super(builder);

        // Precondition checks
        Preconditions.checkArgument(builder.bufferSize > 0, "bufferSize must be greater than 0");

        this.bufferSize = builder.bufferSize;
        this.storage = builder.storage;

        areThereEventsToSend = new Thread(
                getAreThereEventsToSendRunnable(),
                EVENTS_CHECK_THREAD_NAME_PREFIX + EVENTS_CHECK_THREAD_NUMBER.getAndIncrement()
        );
        areThereEventsToSend.start();
    }

    /**
     * Adds a TrackerEvent to the concurrent queue buffer
     *
     * @param event an event
     */
    @Override
    public void emit(final TrackerEvent event) {
        boolean result = storage.add(event); // Add to buffer and quickly return back to application
        
        if (!result) {
            LOGGER.error("Unable to add event to emitter, emitter buffer is full");
        }
    }

    /*
     * Forces the events currently in the buffer to be sent
     */
    @Override
    public void flushBuffer() {
        // Drain immediate event buffer
        while (true) {
            TrackerEvent event = storage.getInitialEventBuffer().poll();
            if (event == null) {
                break;
            } else {
                storage.getEventStagingBuffer().offer(event);
            }
        }

        drainEventsAndSend();
    }

    /**
     * Returns List of Events that are in the buffer.
     *
     * @return the buffered events
     */
    @Override
    public List<TrackerEvent> getBuffer() {
        return new ArrayList<>(storage.getEventStagingBuffer());
    }

    /**
     * Customize the emitter buffer size to any valid integer greater than zero.
     *
     * @param bufferSize number of events to collect before sending
     */
    @Override
    public void setBufferSize(final int bufferSize) {
        Preconditions.checkArgument(bufferSize > 0, "bufferSize must be greater than 0");
        this.bufferSize = bufferSize;
    }

    /**
     * Gets the Emitter Buffer Size
     *
     * @return the buffer size
     */
    @Override
    public int getBufferSize() {
        return this.bufferSize;
    }

    /**
     * Returns a Consumer for the concurrent queue buffer
     * Consumes events onto another queue to be sent when bufferSize is reached
     *
     * @return the new Runnable object
     */
    private Runnable getAreThereEventsToSendRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                while (!isClosing) {
                    if (storage.getSize() >= bufferSize) {
                        drainEventsAndSend();
                    }
                }
            }
        };
    }

    private void drainEventsAndSend() {
        List<TrackerEvent> events = new ArrayList<>();
        storage.removeAllEvents(events);
        execute(getPostRequestRunnable(events));
    }

    /**
     * Returns a Runnable POST Request operation
     *
     * @param buffer the event buffer to be sent
     * @return the new Runnable object
     */
    private Runnable getPostRequestRunnable(final List<TrackerEvent> buffer) {
        return new Runnable() {
            @Override
            public void run() {
                if (buffer.size() == 0) {
                    return;
                }

                final SelfDescribingJson post = getFinalPost(buffer);
                final int code = httpClientAdapter.post(post);

                // Process results
                int success = 0;
                int failure = 0;
                if (!isSuccessfulSend(code)) {
                    LOGGER.error("BatchEmitter failed to send {} events: code: {}", buffer.size(), code);
                    failure += buffer.size();
                } else {
                    LOGGER.debug("BatchEmitter successfully sent {} events: code: {}", buffer.size(), code);
                    success += buffer.size();
                }

                // Send the callback if available
                if (requestCallback != null) {
                    if (failure != 0) {
                        requestCallback.onFailure(success,
                                buffer.stream().map(TrackerEvent::getEvent).collect(Collectors.toList()));
                    } else {
                        requestCallback.onSuccess(success);
                    }
                }
            }
        };
    }

    /**
     * Constructs the SelfDescribingJson to be sent to the endpoint
     *
     * @param buffer the event buffer
     * @return the constructed POST payload
     */
    private SelfDescribingJson getFinalPost(final List<TrackerEvent> buffer) {
        final List<Map<String, String>> toSendPayloads = new ArrayList<>();
        final String sentTimestamp = Long.toString(System.currentTimeMillis());

        for (TrackerEvent event : buffer) {
            List<TrackerPayload> payloads = event.getTrackerPayloads();
            for (TrackerPayload payload : payloads) {
                payload.add(Parameter.DEVICE_SENT_TIMESTAMP, sentTimestamp);
                toSendPayloads.add(payload.getMap());
            }
        }

        return new SelfDescribingJson(Constants.SCHEMA_PAYLOAD_DATA, toSendPayloads);
    }

    /**
     * On close attempt to send all remaining events.
     */
    @Override
    public void close() {
        isClosing = true;

        areThereEventsToSend.interrupt(); // Kill buffer consumer
        flushBuffer(); // Attempt to send all remaining events

        //Shutdown executor threadpool
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(closeTimeout, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(closeTimeout, TimeUnit.SECONDS))
                        LOGGER.warn("Executor did not terminate");
                }
            } catch (final InterruptedException ie) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
