/*
 * Copyright (c) 2014-2022 Snowplow Analytics Ltd. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;

/**
 * An emitter which sends events as soon as they are received via
 * GET requests.
 */
public class SimpleEmitter extends AbstractEmitter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleEmitter.class);

    public static abstract class Builder<T extends Builder<T>> extends AbstractEmitter.Builder<T> {
        public SimpleEmitter build() {
            return new SimpleEmitter(this);
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

    protected SimpleEmitter(final Builder<?> builder) {
        super(builder);
    }

    /**
     * Adds an event to the buffer and instantly sends it
     *
     * @param payload a payload
     */
    @Override
    public boolean add(TrackerPayload payload) {
        executor.execute(getGetRequestRunnable(payload));

        // This result doesn't mean anything
        // The return type is for BatchEmitter's benefit
        return true;
    }

    /**
     * Sends buffered events, but SimpleEmitter does not buffer events
     * So has no effect
     */
    @Override
    public void flushBuffer() {
        // Do nothing!
    }

    /**
     * Returns a Runnable GET Request operation
     *
     * @param payload the event to be sent
     * @return the new Callable object
     */
    private Runnable getGetRequestRunnable(final TrackerPayload payload) {
        return new Runnable() {
            @Override
            public void run() {
                payload.add(Parameter.DEVICE_SENT_TIMESTAMP, Long.toString(System.currentTimeMillis()));
                final int code = httpClientAdapter.get(payload);

                // Process results
                if (!isSuccessfulSend(code)) {
                    LOGGER.error("SimpleEmitter failed to send {} events: code: {}", 1, code);
                } else {
                    LOGGER.debug("SimpleEmitter successfully sent {} events: code: {}", 1, code);
                }

            }
        };
    }

    /**
     * Returns List of Events that are in the buffer.
     * Always empty for SimpleEmitter
     *
     * @return the empty buffer
     */
    @Override
    public List<TrackerPayload> getBuffer() {
        return new ArrayList<>();
    }

    /**
     * Customize the emitter batch size to any valid integer greater than zero.
     * Has no effect on SimpleEmitter
     *
     * @param batchSize number of events to collect before sending
     */
    @Override
    public void setBatchSize(final int batchSize) {
        if (batchSize != 1) {
            LOGGER.debug("Noop. SimpleEmitter batch size must always be 1.");
        }
    }

    /**
     * Gets the Emitter batch Size - Will always be 1 for SimpleEmitter
     *
     * @return the batch size
     */
    @Override
    public int getBatchSize() {
        return 1;
    }
}
