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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.snowplowanalytics.snowplow.tracker.payload.TrackerEvent;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.events.Event;

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
     * @param event an event
     */
    @Override
    public void add(final TrackerEvent event) {
        execute(getGetRequestRunnable(event));
    }

    @Override
    public void add(TrackerPayload payload) {
        // nothing happens
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
     * @param event the event to be sent
     * @return the new Callable object
     */
    private Runnable getGetRequestRunnable(final TrackerEvent event) {
        return new Runnable() {
            @Override
            public void run() {
                int success = 0;
                int failure = 0;
                
                List<TrackerPayload> payloads = event.getTrackerPayloads();

                for (TrackerPayload payload : payloads) {
                    payload.add(Parameter.DEVICE_SENT_TIMESTAMP, Long.toString(System.currentTimeMillis()));
                    final int code = httpClientAdapter.get(payload);

                    // Process results
                    if (!isSuccessfulSend(code)) {
                        LOGGER.error("SimpleEmitter failed to send {} events: code: {}", 1, code);
                        failure += 1;
                    } else {
                        LOGGER.debug("SimpleEmitter successfully sent {} events: code: {}", 1, code);
                        success += 1;
                    }
                }

                // Send the callback if available
                if (requestCallback != null) {
                    if (failure != 0) {
                        final List<Event> buffer = new ArrayList<>();
                        buffer.add(event.getEvent());
                        requestCallback.onFailure(success, buffer);
                    } else {
                        requestCallback.onSuccess(success);
                    }
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
    public List<TrackerEvent> getBuffer() {
        return new ArrayList<>();
    }

    /**
     * Customize the emitter buffer size to any valid integer greater than zero.
     * Has no effect on SimpleEmitter
     *
     * @param bufferSize number of events to collect before sending
     */
    @Override
    public void setBufferSize(final int bufferSize) {
        if (bufferSize != 1) {
            LOGGER.debug("Noop. SimpleEmitter buffer size must always be 1.");
        }
    }

    /**
     * Gets the Emitter Buffer Size - Will always be 1 for SimpleEmitter
     *
     * @return the buffer size
     */
    @Override
    public int getBufferSize() {
        return 1;
    }
}
