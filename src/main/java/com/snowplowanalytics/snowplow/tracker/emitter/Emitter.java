/*
 * Copyright (c) 2014 Snowplow Analytics Ltd. All rights reserved.
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.snowplowanalytics.snowplow.tracker.Constants;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Emitter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Emitter.class);

    private RequestMethod requestMethod = RequestMethod.Synchronous;
    private List<Map<String,Object>> buffer = new ArrayList<Map<String,Object>>();
    private HttpClientAdapter httpClientAdapter;
    protected Integer bufferSize = 10;
    protected RequestCallback requestCallback;
    protected HttpMethod httpMethod = HttpMethod.GET;

    /**
     * Create an Emitter instance with a collector URL and HttpMethod to send requests.
     * @param httpMethod The HTTP request method. If GET, <code>BufferOption</code> is set to <code>Instant</code>.
     * @param callback The callback function to handle success/failure cases when sending events.
     */
    public Emitter(HttpMethod httpMethod, RequestCallback callback, HttpClientAdapter httpClientAdapter) {

        this.requestCallback = callback;
        this.httpMethod = httpMethod;
        this.httpClientAdapter = httpClientAdapter;
    }

    /**
     * Sets whether the buffer should send events instantly or after the buffer has reached
     * it's limit. By default, this is set to BufferOption Default.
     * @param size The size of the buffer
     */
    public void setBufferSize(int size) {
        this.bufferSize = size;
    }

    /**
     * Sets whether requests should be sent synchronously or asynchronously.
     * @param option The HTTP request method
     */
    public void setRequestMethod(RequestMethod option) {
        this.requestMethod = option;
    }

    /**
     * Add event payloads to the emitter's buffer
     * @param payload Payload to be added
     * @return Returns the boolean value if the event was successfully added to the buffer
     */
    public boolean addToBuffer(Map<String, Object> payload) {
        boolean ret = buffer.add(payload);
        if (buffer.size() >= bufferSize) {
            flushBuffer();
        }
        return ret;
    }

    /**
     * Sends all events in the buffer to the collector.
     */
    public void flushBuffer() {
        if (buffer.isEmpty()) {
            LOGGER.debug("Buffer is empty, exiting flush operation..");
            return;
        }

        final List<Map<String, Object>> toSendPayloads = Lists.newArrayList(buffer);
        buffer.clear();

        final List<Map<String, Object>> unsentPayloads = Lists.newArrayList();
        final List<Map<String, Object>> sentPayloads = Lists.newArrayList();

        // TODO emerge a dedicated object in order to do the send
        if (httpMethod == HttpMethod.GET) {
            for (Map<String, Object> payload : toSendPayloads) {
                try {
                    final int status_code = httpClientAdapter.get(payload);

                    if (status_code == 200) {
                        sentPayloads.add(payload);
                    } else {
                        unsentPayloads.add(payload);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to emit payload, <method={}>", httpMethod, e);
                    unsentPayloads.add(payload);
                }
            }

        } else if (httpMethod == HttpMethod.POST) {
            try {
                final SchemaPayload selfDescribedJson = new SchemaPayload();
                selfDescribedJson.setSchema(Constants.SCHEMA_PAYLOAD_DATA);
                selfDescribedJson.setData(toSendPayloads);

                final int status_code = httpClientAdapter.post(selfDescribedJson);

                if (status_code == 200) {
                    sentPayloads.addAll(toSendPayloads);
                } else {
                    unsentPayloads.add(selfDescribedJson.getMap());
                }
            } catch (Exception e) {
                LOGGER.error("Failed to emit payload, <method={}>", httpMethod,  e);
                unsentPayloads.addAll(toSendPayloads);
            }
        }

        if (requestCallback != null) {
            if (unsentPayloads.size() == 0) {
                requestCallback.onSuccess(sentPayloads.size());
            } else {
                requestCallback.onFailure(sentPayloads.size(), unsentPayloads);
            }
        }
    }

    @VisibleForTesting
    protected List<Map<String, Object>> getBuffer() {
        return buffer;
    }
}
