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

import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

import java.util.List;

/**
 * This interface allows the user to provide callbacks for when events are
 * successfully sent to the event collector, or at other times when data loss
 * may occur, specified using the FailureType enum.
 */
public interface EmitterCallback {
    void onSuccess(List<TrackerPayload> payloads);

    void onFailure(FailureType failureType, boolean willRetry, List<TrackerPayload> payloads);
}
