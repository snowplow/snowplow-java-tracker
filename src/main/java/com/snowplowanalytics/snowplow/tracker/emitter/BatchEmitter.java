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

// Java
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// Google
import com.google.common.base.Preconditions;

// Slf4j
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This library
import com.snowplowanalytics.snowplow.tracker.constants.Constants;
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;

/**
 * An emitter that emit a batch of events in a single call
 * It uses the post method of under-laying http adapter
 */
public class BatchEmitter extends AbstractEmitter implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchEmitter.class);

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
    }

    /**
     * Adds a payload to the buffer and checks whether we have reached the buffer
     * limit yet.
     *
     * @param payload an event payload
     */
    @Override
    public synchronized void emit(final TrackerPayload payload) {
        buffer.add(payload);
        if (buffer.size() >= bufferSize) {
            flushBuffer();
        }
    }

    /**
     * When the buffer limit is reached sending of the buffer is initiated.
     */
    public void flushBuffer() {
        execute(getRequestRunnable(buffer));
        buffer = new ArrayList<>();
    }

    /**
     * Returns a Runnable POST Request operation
     *
     * @param buffer the event buffer to be sent
     * @return the new Callable object
     */
    private Runnable getRequestRunnable(final List<TrackerPayload> buffer) {
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
                        requestCallback.onFailure(success, buffer);
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
    private SelfDescribingJson getFinalPost(final List<TrackerPayload> buffer) {
        final List<Map> toSendPayloads = new ArrayList<>();
        for (final TrackerPayload payload : buffer) {
            payload.add(Parameter.DEVICE_SENT_TIMESTAMP, Long.toString(System.currentTimeMillis()));
            toSendPayloads.add(payload.getMap());
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
