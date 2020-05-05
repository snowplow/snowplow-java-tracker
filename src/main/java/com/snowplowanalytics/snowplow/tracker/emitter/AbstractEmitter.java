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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.base.Preconditions;

import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerEvent;

/**
 * AbstractEmitter class which contains common elements to
 * the emitters wrapped in a builder format.
 */
public abstract class AbstractEmitter implements Emitter {

    protected HttpClientAdapter httpClientAdapter;
    protected RequestCallback requestCallback;
    protected ExecutorService executor;
    protected List<TrackerEvent> buffer = new ArrayList<>();
    protected int bufferSize = 1;

    public static abstract class Builder<T extends Builder<T>> {

        private HttpClientAdapter httpClientAdapter; // Required
        private RequestCallback requestCallback = null; // Optional
        private int threadCount = 50; // Optional
        protected abstract T self();

        /**
         * Adds the HttpClientAdapter to the AbstractEmitter
         *
         * @param httpClientAdapter the adapter to use
         * @return itself
         */
        public T httpClientAdapter(final HttpClientAdapter httpClientAdapter) {
            this.httpClientAdapter = httpClientAdapter;
            return self();
        }

        /**
         * An optional Request Callback for adding the ability to handle failure cases
         * for sending.
         *
         * @param requestCallback the emitter request callback
         * @return itself
         */
        public T requestCallback(final RequestCallback requestCallback) {
            this.requestCallback = requestCallback;
            return self();
        }

        /**
         * Sets the Thread Count for the ExecutorService
         *
         * @param threadCount the size of the thread pool
         * @return itself
         */
        public T threadCount(final int threadCount) {
            this.threadCount = threadCount;
            return self();
        }
    }

    private static class Builder2 extends Builder<Builder2> {
        @Override
        protected Builder2 self() {
            return this;
        }
    }

    public static Builder<?> builder() {
        return new Builder2();
    }

    protected AbstractEmitter(final Builder<?> builder) {

        // Precondition checks
        Preconditions.checkNotNull(builder.httpClientAdapter);
        Preconditions.checkArgument(builder.threadCount > 0, "threadCount must be greater than 0");

        this.httpClientAdapter = builder.httpClientAdapter;
        this.requestCallback = builder.requestCallback;
        this.executor = Executors.newScheduledThreadPool(builder.threadCount);
    }

    /**
     * Adds a payload to the buffer and checks whether we have reached the buffer
     * limit yet.
     *
     * @param payload an event payload
     */
    @Override
    public abstract void emit(TrackerEvent payload);

    /**
     * Customize the emitter buffer size to any valid integer greater than zero. -
     * Will only effect the BatchEmitter
     *
     * @param bufferSize number of events to collect before sending
     */
    @Override
    public void setBufferSize(final int bufferSize) {
        Preconditions.checkArgument(bufferSize > 0, "bufferSize must be greater than 0");
        this.bufferSize = bufferSize;
    }

    /**
     * When the buffer limit is reached sending of the buffer is initiated.
     */
    @Override
    public abstract void flushBuffer();

    /**
     * Gets the Emitter Buffer Size - Will always be 1 for SimpleEmitter
     *
     * @return the buffer size
     */
    @Override
    public int getBufferSize() {
        return this.bufferSize;
    }

    /**
     * Returns the List of Payloads that are in the buffer.
     *
     * @return the buffer payloads
     */
    @Override
    public List<TrackerEvent> getBuffer() {
        return this.buffer;
    }

    /**
     * Sends a runnable to the executor service.
     *
     * @param runnable the runnable to be queued
     */
    protected void execute(final Runnable runnable) {
        this.executor.execute(runnable);
    }

    /**
     * Checks whether the response code was a success or not.
     *
     * @param code the response code
     * @return whether it is in the success range
     */
    protected boolean isSuccessfulSend(final int code) {
        return code >= 200 && code < 300;
    }
}
