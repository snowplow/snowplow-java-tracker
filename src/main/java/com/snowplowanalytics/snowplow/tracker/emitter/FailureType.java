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

/**
 * The supported failure options for EmitterCallback.
 */
public enum FailureType {
    /**
     * A request status code other than 2xx is received. Payloads in the request
     * may be automatically retried or not following this kind of failure, depending on the status code
     * and the BatchEmitter configuration.
     */
    REJECTED_BY_COLLECTOR,

    /**
     * The InMemoryEventStore buffer is full. This could occur if the network connection
     * to the event collector is down, causing payloads to accumulate in the buffer.
     * This failure can occur either when the Tracker attempts to add new events to the BatchEmitter,
     * or when events that need to be retried are returned to the buffer, removing newer events
     * to make space if necessary.
     */
    TRACKER_STORAGE_FULL,

    /**
     * An exception or unsuccessful POST request in the HttpClientAdapter.
     */
    HTTP_CONNECTION_FAILURE,

    /**
     * An exception during POST request in BatchEmitter.
     */
    EMITTER_REQUEST_FAILURE
}
