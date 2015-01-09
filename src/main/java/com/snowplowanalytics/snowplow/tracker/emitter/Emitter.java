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

import com.snowplowanalytics.snowplow.tracker.Constants;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Emitter {

    private final Logger LOGGER = LoggerFactory.getLogger(Emitter.class);
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
        if (buffer.size() == bufferSize) {
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

        if (httpMethod == HttpMethod.GET) {
            int success_count = 0;
            List<Map<String, Object>> unsentPayloads = new LinkedList<Map<String, Object>>();
            for (Map<String, Object> payload : buffer) {
                int status_code = httpClientAdapter.get(payload);
                if (status_code == 200) {
                    success_count++;
                } else {
                    unsentPayloads.add(payload);
                }
            }
            if (unsentPayloads.size() == 0) {
                if (requestCallback != null) {
                    requestCallback.onSuccess(success_count);
                }
            }
            else if (requestCallback != null) {
                requestCallback.onFailure(success_count, unsentPayloads);
            }
        } else if (httpMethod == HttpMethod.POST) {
            List<Map<String, Object>> unsentPayload = new LinkedList<Map<String, Object>>();

            SchemaPayload selfDescribedJson = new SchemaPayload();
            selfDescribedJson.setSchema(Constants.SCHEMA_PAYLOAD_DATA);

            List<Map<String, Object>> eventMaps = new ArrayList<Map<String, Object>>();
            for (Map<String, Object> payload : buffer) {
                eventMaps.add(payload);
            }
            selfDescribedJson.setData(eventMaps);
            int status_code = httpClientAdapter.post(selfDescribedJson);
            if (status_code == 200 && requestCallback != null) {
                requestCallback.onSuccess(buffer.size());
            } else if (requestCallback != null) {
                unsentPayload.add(selfDescribedJson.getMap());
                requestCallback.onFailure(0, unsentPayload);
            }
        }
        
        buffer.clear();
    }
}
