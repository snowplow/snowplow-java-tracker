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
 * Provides a callback interface for reporting counts of successfully sent
 * events and returning any failed events to be handled by the developer.
 */
public interface RequestCallback {

    /**
     * If all events are sent successfully then the count
     * of sent events are returned.
     *
     * @param successCount the successful count
     */
    void onSuccess(int successCount);

    /**
     * If all/some events failed then the count of successful
     * events is returned along with all the failed Payloads.
     *
     * @param successCount the successful count
     * @param failedEvents the list of failed payloads
     */
    void onFailure(int successCount, List<TrackerEvent> failedEvents);
}
