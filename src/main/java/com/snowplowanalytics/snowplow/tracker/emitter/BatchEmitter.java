/*
 * Copyright (c) 2015 Snowplow Analytics Ltd. All rights reserved.
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.snowplowanalytics.snowplow.tracker.constants.Constants;
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerEvent;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An emitter that emit a batch of events in a single call It uses the post
 * method of under-laying http adapter
 */
public class BatchEmitter extends AbstractEmitter implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchEmitter.class);

    private int bufferSize = 1;
    private BlockingQueue<TrackerEvent> eventBuffer = new LinkedBlockingQueue<>();
    private final long closeTimeout = 5;

    public static abstract class Builder<T extends Builder<T>> extends AbstractEmitter.Builder<T> {

        private int bufferSize = 50; // Optional

        /**
         * @param bufferSize The count of events to buffer before sending
         * @return itself
         */
        public T bufferSize(final int bufferSize) {
            this.bufferSize = bufferSize;
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

        new Thread(getBufferConsumerRunnable()).start();
    }

    /**
     * Adds a TrackerEvent to the concurrent queue buffer
     *
     * @param event an event
     */
    @Override
    public void emit(final TrackerEvent event) {
        try {
            eventBuffer.put(event);
        } catch (Exception e) {
            LOGGER.error("Unable to add event to emitter", e);
        }
    }

    /*
     * Forces the events currently in the buffer to be sent
     */
    public void flushBuffer() {
        /**
         * The concurrent eventBuffer is always empty unless this is a sudden call to flushBuffer
         * for instance on application shutdown, so we wait for the eventBuffer to drain into buffer.
         */
        while(eventBuffer.size() != 0) {} // Wait for eventBuffer to drain

        // Once eventBuffer has drained then we can send the final pending requests.
        sendRequests();
    }

    /**
     * Create new Runnable to send requests and clears out buffer
     * and executes it on the Executor ThreadPool
     * Safe to do this un-atomically (i.e. no synchronized) as this is only called from
     * bufferConsumerRunnable (of which there is only one consumer thread) 
     * or flushBuffer when eventBuffer is empty so no new events should be being written
     */
    private void sendRequests() {
        execute(getRequestRunnable(buffer));
        buffer = new ArrayList<>();
    }

    /**
     * Returns a Consumer for the concurrent queue buffer
     * Writes to the AbstractEmitter buffer
     *
     * @return the new Runnable object
     */
    private Runnable getBufferConsumerRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        buffer.add(eventBuffer.take());
                        if (buffer.size() >= bufferSize) {
                            sendRequests();
                        }
                    } catch (InterruptedException e) {
                        LOGGER.warn("Buffer consumer interrupted", e);
                    }
                }
            }
        };
    }

    /**
     * Returns a Runnable POST Request operation
     *
     * @param buffer the event buffer to be sent
     * @return the new Runnable object
     */
    private Runnable getRequestRunnable(final List<TrackerEvent> buffer) {
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
                        requestCallback.onFailure(success, buffer.stream().map(te -> te.getEvent()).collect(Collectors.toList()));
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
        flushBuffer();
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
