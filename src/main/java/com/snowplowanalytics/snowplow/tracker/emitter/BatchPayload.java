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

import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

import java.util.List;


public class BatchPayload {

    private final Long batchId;
    private final List<TrackerPayload> payloads;
    private int remainingRetries = 3;

    public int getRemainingRetries() {
        return remainingRetries;
    }

    public void subtractRetry() {
        this.remainingRetries--;
    }



    public BatchPayload(Long payloadId, List<TrackerPayload> payloads) {
        this.batchId = payloadId;
        this.payloads = payloads;
    }

    public Long getBatchId() {
        return batchId;
    }

    public List<TrackerPayload> getPayload() {
        return payloads;
    }
}
