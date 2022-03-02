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
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Preconditions;
import com.snowplowanalytics.snowplow.tracker.constants.Constants;
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An emitter that emit a batch of events in a single call 
 * It uses the post method of underlying http adapter
 */
public class BatchEmitter extends AbstractEmitter implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchEmitter.class);
    private boolean isClosing = false;
    private int bufferSize;
    private final EventStore eventStore;
    private final AtomicLong retryDelay;

    public static abstract class Builder<T extends Builder<T>> extends AbstractEmitter.Builder<T> {

        private int bufferSize = 50; // Optional
        private int bufferCapacity = Integer.MAX_VALUE;
        private EventStore eventStore;

        /**
         * @param bufferSize The count of events to buffer before sending
         * @return itself
         */
        public T bufferSize(final int bufferSize) {
            this.bufferSize = bufferSize;
            return self();
        }

        public T eventStore(final EventStore eventStore) {
            this.eventStore = eventStore;
            return self();
        }

        public T bufferCapacity(final int bufferCapacity) {
            this.bufferCapacity = bufferCapacity;
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
        bufferSize = builder.bufferSize;

        if (builder.eventStore == null) {
            eventStore = new InMemoryEventStore(builder.bufferCapacity);
        } else {
            eventStore = builder.eventStore;
        }
        retryDelay = new AtomicLong(0L);
    }

    /**
     * Adds a TrackerPayload to the concurrent queue buffer
     *
     * @param payload a payload
     * @implNote As a side effect it triggers an Emitter thread to emit a batch of events.
     */
    @Override
    public void add(final TrackerPayload payload) {
        boolean result = eventStore.addEvent(payload);

        if (!isClosing) {
            if (eventStore.size() >= bufferSize) {
                executor.schedule(getPostRequestRunnable(bufferSize), retryDelay.get(), TimeUnit.MILLISECONDS);
            }
        }
        
        if (!result) {
            LOGGER.error("Unable to add payload to emitter, emitter buffer is full");
        }
    }

    /*
     * Forces all the payloads currently in the buffer to be sent
     */
    @Override
    public void flushBuffer() {
        executor.schedule(getPostRequestRunnable(eventStore.size()), 0, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns List of Payloads that are in the buffer.
     *
     * @return the buffered events
     */
    @Override
    public List<TrackerPayload> getBuffer() {
        return eventStore.getAllEvents();
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
        return bufferSize;
    }

    long getRetryDelay() {
        return retryDelay.get();
    }

    /**
     * Returns a Runnable POST Request operation
     *
     * @param numberOfEvents the number of events to be sent in the request
     * @return the new Runnable object
     */
    private Runnable getPostRequestRunnable(int numberOfEvents) {
        return () -> {
            BatchPayload batchedEvents = null;
            try {
                batchedEvents = eventStore.getEventBatch(numberOfEvents);
                List<TrackerPayload> eventsInRequest = batchedEvents.getPayloads();

                if (eventsInRequest.size() == 0) {
                    return;
                }

                final SelfDescribingJson post = getFinalPost(eventsInRequest);
                final int code = httpClientAdapter.post(post);

                // Process results
                if (isSuccessfulSend(code)) {
                    LOGGER.debug("BatchEmitter successfully sent {} events: code: {}", eventsInRequest.size(), code);
                    retryDelay.set(0L);
                    eventStore.cleanupAfterSendingAttempt(true, batchedEvents.getBatchId());
                } else {
                    LOGGER.error("BatchEmitter failed to send {} events: code: {}", eventsInRequest.size(), code);
                    eventStore.cleanupAfterSendingAttempt(false, batchedEvents.getBatchId());

                    // exponentially increase retry backoff time after the first failure
                    if (!retryDelay.compareAndSet(0, 50L)) {
                        retryDelay.updateAndGet(currentDelay -> currentDelay * 2);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("BatchEmitter event sending error: {}", e.getMessage());
                if (batchedEvents != null) {
                    eventStore.cleanupAfterSendingAttempt(false, batchedEvents.getBatchId());
                }
            }
        };
    }

    /**
     * Constructs the SelfDescribingJson to be sent to the endpoint
     *
     * @param events the event buffer
     * @return the constructed POST payload
     */
    private SelfDescribingJson getFinalPost(final List<TrackerPayload> events) {
        final List<Map<String, String>> toSendPayloads = new ArrayList<>();
        final String sentTimestamp = Long.toString(System.currentTimeMillis());

        for (TrackerPayload payload : events) {
            payload.add(Parameter.DEVICE_SENT_TIMESTAMP, sentTimestamp);
            toSendPayloads.add(payload.getMap());
        }

        return new SelfDescribingJson(Constants.SCHEMA_PAYLOAD_DATA, toSendPayloads);
    }

    /**
     * On close attempt to send all remaining events.
     */
    @Override
    public void close() {
        final long closeTimeout = 5;
        isClosing = true;

        flushBuffer(); // Attempt to send all remaining events

        //Shutdown executor threadpool
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(closeTimeout, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(closeTimeout, TimeUnit.SECONDS))
                        LOGGER.warn("Emitter executor did not terminate");
                }
            } catch (final InterruptedException ie) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
