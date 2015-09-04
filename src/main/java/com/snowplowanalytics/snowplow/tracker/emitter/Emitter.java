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

// This library
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

import java.util.List;

/**
 * Emitter interface.
 */
public interface Emitter {

    /**
     * Adds a payload to the buffer and checks whether
     * we have reached the buffer limit yet.
     *
     * @param payload an event payload
     */
    void emit(TrackerPayload payload);

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
     * Gets the Emitter Buffer Size
     * - Will always be 1 for SimpleEmitter
     *
     * @return the buffer size
     */
    int getBufferSize();

    /**
     * Returns the List of Payloads that are in the buffer.
     *
     * @return the buffer payloads
     */
    List<TrackerPayload> getBuffer();
}
