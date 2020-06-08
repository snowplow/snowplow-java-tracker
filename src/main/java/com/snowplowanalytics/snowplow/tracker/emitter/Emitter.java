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

import java.util.List;

import com.snowplowanalytics.snowplow.tracker.payload.TrackerEvent;

/**
 * Emitter interface.
 */
public interface Emitter {

    /**
     * Adds an event to the buffer and checks whether
     * we have reached the buffer limit yet.
     *
     * @param event an event to be emitted
     */
    void emit(TrackerEvent event);

    /**
     * Customize the emitter buffer size to any valid integer
     * greater than zero.
     * - Will only effect the BatchEmitter
     *
     * @param bufferSize number of events to collect before
     *                   sending
     */
    void setBufferSize(int bufferSize);

    /**
     * When the buffer limit is reached sending of the buffer is
     * initiated.
     *
     * This can be used to manually start sending.
     */
    void flushBuffer();

    /**
     * Gets the Emitter Buffer Size
     * - Will always be 1 for SimpleEmitter
     *
     * @return the buffer size
     */
    int getBufferSize();

    /**
     * Returns the List of Events that are in the buffer.
     *
     * @return the buffer events
     */
    List<TrackerEvent> getBuffer();
}
