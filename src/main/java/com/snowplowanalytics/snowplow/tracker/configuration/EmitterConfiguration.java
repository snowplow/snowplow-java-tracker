/*
 * Copyright (c) 2014-present Snowplow Analytics Ltd. All rights reserved.
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
package com.snowplowanalytics.snowplow.tracker.configuration;

import com.snowplowanalytics.snowplow.tracker.emitter.EmitterCallback;
import com.snowplowanalytics.snowplow.tracker.emitter.EventStore;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

public class EmitterConfiguration {

    private int batchSize; // Optional
    private int bufferCapacity; // Optional
    private EventStore eventStore;  // Optional
    private Map<Integer, Boolean> customRetryForStatusCodes;  // Optional
    private int threadCount; // Optional
    private ScheduledExecutorService requestExecutorService; // Optional
    private EmitterCallback callback; // Optional

    // Getters and Setters

    /**
     * Returns the number of events to send per request (batched).
     * @return the batch size
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * Returns the maximum number of events to buffer in memory.
     * @return maximum buffer capacity
     */
    public int getBufferCapacity() {
        return bufferCapacity;
    }

    /**
     * Returns the EventStore used to buffer events.
     * @return EventStore instance
     */
    public EventStore getEventStore() {
        return eventStore;
    }

    /**
     * Returns the custom configuration for HTTP status codes. "True" means the
     * @return map of integers (status codes) to booleans (true for retry and false for not retry)
     */
    public Map<Integer, Boolean> getCustomRetryForStatusCodes() {
        return customRetryForStatusCodes;
    }

    /**
     * Returns the number of threads used for event sending using the ScheduledExecutorService.
     * @return thread count
     */
    public int getThreadCount() {
        return threadCount;
    }

    /**
     * Returns the ScheduledExecutorService used for sending events.
     * @return ScheduledExecutorService object
     */
    public ScheduledExecutorService getRequestExecutorService() {
        return requestExecutorService;
    }

    /**
     * Returns the custom callback which is called when events are successfully sent to the collector,
     * or after certain failure conditions.
     *
     * @return EmitterCallback object
     */
    public EmitterCallback getCallback() {
        return callback;
    }

    // Constructor

    /**
     * Create an EmitterConfiguration instance. The default configuration is:
     * 50 batched events per request;
     * maximum 10 000 events buffered in memory;
     * 50 threads;
     * no retry for request status codes 400, 401, 403, 410 or 422;
     * and OkHttp (OkHttpClientAdapter) used for HTTP requests.
     */
    public EmitterConfiguration() {
        batchSize = 50;
        bufferCapacity = 10000;
        eventStore = null;
        customRetryForStatusCodes = null;
        threadCount = 50;
        requestExecutorService = null;
        callback = null;
    }

    // Builder methods

    /**
     * The default batch size is 50.
     *
     * @param batchSize The count of events to send in one HTTP request
     * @return itself
     */
    public EmitterConfiguration batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    /**
     * The default buffer capacity is 10 000 events.
     * When the buffer is full (due to network outage), new events are lost.
     *
     * @param bufferCapacity The maximum capacity of the default InMemoryEventStore event buffer
     * @return itself
     */
    public EmitterConfiguration bufferCapacity(int bufferCapacity) {
        this.bufferCapacity = bufferCapacity;
        return this;
    }

    /**
     * The default EventStore is InMemoryEventStore.
     *
     * @param eventStore The EventStore to use
     * @return itself
     */
    public EmitterConfiguration eventStore(EventStore eventStore) {
        this.eventStore = eventStore;
        return this;
    }

    /**
     * Set custom retry rules for HTTP status codes received in emit responses from the Collector.
     * By default, retry will not occur for status codes 400, 401, 403, 410 or 422. This can be overridden here.
     * Note that 2xx codes will never retry as they are considered successful.
     * @param customRetryForStatusCodes Mapping of integers (status codes) to booleans (true for retry and false for not retry)
     * @return itself
     */
    public EmitterConfiguration customRetryForStatusCodes(Map<Integer, Boolean> customRetryForStatusCodes) {
        this.customRetryForStatusCodes = customRetryForStatusCodes;
        return this;
    }

    /**
     * Sets the Thread Count for the ScheduledExecutorService (default is 50).
     *
     * @param threadCount the size of the thread pool
     * @return itself
     */
    public EmitterConfiguration threadCount(int threadCount) {
        this.threadCount = threadCount;
        return this;
    }

    /**
     * Set a custom ScheduledExecutorService to send http requests (default is ScheduledThreadPoolExecutor).
     * <p>
     * <b>Implementation note: </b><em>Be aware that calling `close()` on a BatchEmitter instance
     * has a side-effect and will shutdown that ExecutorService.</em>
     *
     * @param requestExecutorService the ScheduledExecutorService to use
     * @return itself
     */
    public EmitterConfiguration requestExecutorService(ScheduledExecutorService requestExecutorService) {
        this.requestExecutorService = requestExecutorService;
        return this;
    }

    /**
     * Provide a custom EmitterCallback to access successfully sent or failed event payloads.
     *
     * @param callback an EmitterCallback
     * @return itself
     */
    public EmitterConfiguration callback(EmitterCallback callback) {
        this.callback = callback;
        return this;
    }
}
