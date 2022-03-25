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
 * EventStore interface. For buffering events in the Emitter.
 */
public interface EventStore {

    /**
     * Add TrackerPayload to buffer.
     *
     * @param trackerPayload the payload to add
     * @return success or not
     */
    boolean addEvent(TrackerPayload trackerPayload);

    /**
     * Remove some TrackerPayloads from the buffer.
     *
     * @param numberToGet how many payloads to get
     * @return a BatchPayload wrapper
     */
    BatchPayload getEventsBatch(int numberToGet);

    /**
     * Get a copy of all the TrackerPayloads in the buffer.
     *
     * @return List of all the stored events
     */
    List<TrackerPayload> getAllEvents();

    /**
     * Finish processing events after a request has been made.
     *
     * @param needRetry if another attempt should be made to send the events
     * @param batchId the ID of the batch of events
     */
    void cleanupAfterSendingAttempt(boolean needRetry, long batchId);

    /**
     * Get the current size of the buffer.
     *
     * @return number of events currently in the buffer
     */
    int size();
}
