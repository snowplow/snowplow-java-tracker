/*
 * Copyright (c) 2014-2020 Snowplow Analytics Ltd. All rights reserved.
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;

import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.http.OkHttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerEvent;

import okhttp3.OkHttpClient;

/**
 * AbstractEmitter class which contains common elements to
 * the emitters wrapped in a builder format.
 */
public abstract class AbstractEmitter implements Emitter {

    protected HttpClientAdapter httpClientAdapter;
    protected RequestCallback requestCallback;
    protected ExecutorService executor;

    public static abstract class Builder<T extends Builder<T>> {

        private HttpClientAdapter httpClientAdapter; // Optional
        private RequestCallback requestCallback = null; // Optional
        private int threadCount = 50; // Optional
        private ExecutorService requestExecutorService = null; // Optional
        private String collectorUrl = null; // Required if not specifying a httpClientAdapter
        protected abstract T self();

        /**
         * Set a custom ExecutorService to send http request.
         *
         *  /!\ Be aware that calling `close()` on a BatchEmitter instance has a side-effect and will shutdown that ExecutorService.
         * @param executorService the ExecutorService to use
         * @return itself
         */
        public T requestExecutorService(final ExecutorService executorService) {
            this.requestExecutorService = executorService;
            return self();
        }

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

        /**
         * Sets the emitter url for when a httpClientAdapter is not specified
         * Will be used to create the default OkHttpClientAdapter.
         *
         * @param collectorUrl the url for the default httpClientAdapter
         * @return itself
         */
        public T url(final String collectorUrl) {
            this.collectorUrl = collectorUrl;
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
        Preconditions.checkArgument(builder.threadCount > 0, "threadCount must be greater than 0");

        if (builder.httpClientAdapter != null) {
            this.httpClientAdapter = builder.httpClientAdapter;
        } else {
            Preconditions.checkNotNull(builder.collectorUrl, "Collector url must be specified if not using a httpClientAdapter");

            this.httpClientAdapter = OkHttpClientAdapter.builder()
                    .url(builder.collectorUrl)
                    .httpClient(
                        new OkHttpClient()) // use okhttp as a default
                    .build();
        }

        this.requestCallback = builder.requestCallback;
      
        if (builder.requestExecutorService != null) {
            this.executor = builder.requestExecutorService;
        } else {
            this.executor = Executors.newScheduledThreadPool(builder.threadCount, new EmitterThreadFactory());
        }
    }

    /**
     * Adds an event to the buffer
     *
     * @param event an event
     */
    @Override
    public abstract void emit(TrackerEvent event);

    /**
     * Customize the emitter buffer size to any valid integer greater than zero.
     * Has no effect on SimpleEmitter
     *
     * @param bufferSize number of events to collect before sending
     */
    @Override
    public abstract void setBufferSize(final int bufferSize);

    /**
     * Removes all events from the buffer and sends them
     */
    @Override
    public abstract void flushBuffer();

    /**
     * Gets the Emitter Buffer Size - Will always be 1 for SimpleEmitter
     *
     * @return the buffer size
     */
    @Override
    public abstract int getBufferSize();

    /**
     * Returns List of Events that are in the buffer.
     *
     * @return the buffered events
     */
    @Override
    public abstract List<TrackerEvent> getBuffer();

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

    /**
     * Copied from `Executors.defaultThreadFactory()`.
     * The only change is the generated name prefix.
     */
    static class EmitterThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        EmitterThreadFactory() {
            SecurityManager securityManager = System.getSecurityManager();
            this.group = securityManager != null ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
            this.namePrefix = "snowplow-emitter-pool-" + poolNumber.getAndIncrement() + "-request-thread-";
        }

        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(this.group, runnable, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
            if (thread.isDaemon()) {
                thread.setDaemon(false);
            }

            if (thread.getPriority() != 5) {
                thread.setPriority(5);
            }

            return thread;
        }
    }
}
