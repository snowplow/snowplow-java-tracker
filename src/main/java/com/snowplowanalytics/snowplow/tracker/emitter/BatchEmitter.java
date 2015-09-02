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

// Java
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Google
import com.google.common.base.Preconditions;
import com.google.common.annotations.VisibleForTesting;

// Slf4j
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This library
import com.snowplowanalytics.snowplow.tracker.constants.Constants;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;

/**
 * An emitter that emit a batch of events in a single call
 * It uses the post method of under-laying http adapter
 */
public class BatchEmitter extends AbstractEmitter implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchEmitter.class);
    private List<TrackerPayload> buffer = new ArrayList<TrackerPayload>();
    private int bufferSize = 10;

    /**
     * Builds a BatchEmitter which will combine many
     * events into one for sending as a POST.
     *
     * @param httpClientAdapter the http adapter to use
     * @param requestCallback Request callback functions
     */
    public BatchEmitter(HttpClientAdapter httpClientAdapter, RequestCallback requestCallback) {
        super(httpClientAdapter, requestCallback);
    }

    /**
     * Adds a payload to the buffer and checks whether
     * we have reached the buffer limit yet.
     *
     * @param payload an event payload
     */
    @Override
    public synchronized void emit(TrackerPayload payload) {
        buffer.add(payload);
        if (buffer.size() >= bufferSize) {
            flushBuffer();
        }
    }

    /**
     * When the buffer limit is reached sending of the buffer is
     * initiated.
     */
    void flushBuffer() {
        if (buffer.isEmpty()) {
            LOGGER.debug("Buffer is empty, exiting flush operation.");
            return;
        }

        // Build POST String
        final List<Map> toSendPayloads = new ArrayList<Map>();
        for (TrackerPayload payload : buffer) {
            toSendPayloads.add(payload.getMap());
        }
        final SelfDescribingJson selfDescribingJson = new SelfDescribingJson(
                Constants.SCHEMA_PAYLOAD_DATA,
                toSendPayloads
        );

        // Process result of send
        int success = 0;
        int failure = 0;

        int code = httpClientAdapter.post(selfDescribingJson);
        if (!isSuccessfulSend(code)) {
            LOGGER.error("Batch Emitter failed to send {} events: code: {}", buffer.size(), code);
            failure += buffer.size();
        } else {
            success += buffer.size();
        }

        // Send the callback if available
        if (requestCallback != null) {
            if (failure != 0) {
                List<TrackerPayload> temp = buffer;
                requestCallback.onFailure(success, temp);
            } else {
                requestCallback.onSuccess(success);
            }
        }

        buffer.clear();
    }

    /**
     * On close attempt to send all remaining events.
     */
    @Override
    public void close() {
        flushBuffer();
    }

    /**
     * Customize the emitter buffer size to any valid integer
     * greater than zero.
     *
     * @param bufferSize number of events to collect before
     *                   sending
     */
    public void setBufferSize(int bufferSize) {
        Preconditions.checkArgument(bufferSize > 0);
        this.bufferSize = bufferSize;
    }

    /**
     * Returns the List of Payloads that are in the buffer.
     *
     * @return the buffer payloads
     */
    @VisibleForTesting
    public List<TrackerPayload> getBuffer() {
        return buffer;
    }
}

