package com.snowplowanalytics.snowplow.tracker.emitter;

import java.util.List;

import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

public interface EventStore {

    boolean addEvent(TrackerPayload trackerPayload);

    BatchPayload getEventBatch(int numberToRemove);

    List<TrackerPayload> getAllEvents();

    void cleanupAfterSendingAttempt(boolean successfullySent, long batchId);

    int size();
}
