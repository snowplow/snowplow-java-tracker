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
     * @param successfullySent if the batch of events was successfully sent
     * @param batchId the ID of the batch of events
     */
    void cleanupAfterSendingAttempt(boolean successfullySent, long batchId);

    /**
     * Get the current size of the buffer.
     *
     * @return number of events currently in the buffer
     */
    int size();
}
