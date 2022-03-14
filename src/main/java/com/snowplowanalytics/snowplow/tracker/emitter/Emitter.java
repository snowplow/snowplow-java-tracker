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

import java.util.List;

import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

/**
 * Emitter interface.
 */
public interface Emitter {

    /**
     * Adds a payload to the buffer and checks whether
     * we have reached the buffer limit yet.
     *
     * @param payload a payload to be emitted
     */
    boolean add(TrackerPayload payload);

    /**
     * Customize the emitter batch size to any valid integer
     * greater than zero.
     * Will only affect the BatchEmitter
     *
     * @param batchSize number of events to collect before
     *                   sending
     */
    void setBatchSize(int batchSize);

    /**
     * This can be used to manually send all buffered events.
     */
    void flushBuffer();

    /**
     * Gets the Emitter Batch Size
     * Will always be 1 for SimpleEmitter. Note that SimpleEmitter has been deprecated.
     *
     * @return the batch size
     */
    int getBatchSize();

    /**
     * Returns the List of Payloads that are in the buffer.
     *
     * @return the buffer events
     */
    List<TrackerPayload> getBuffer();
}
