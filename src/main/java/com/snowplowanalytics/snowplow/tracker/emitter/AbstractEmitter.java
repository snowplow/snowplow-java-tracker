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

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;

import com.google.common.base.Preconditions;

import com.snowplowanalytics.snowplow.tracker.http.CollectorCookieJar;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.http.OkHttpClientAdapter;

import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;

/**
 * AbstractEmitter class which contains common elements to
 * the emitters wrapped in a builder format.
 * Note that SimpleEmitter has been deprecated.
 */
public abstract class AbstractEmitter implements Emitter {

    protected HttpClientAdapter httpClientAdapter;
    protected ScheduledExecutorService executor;

    public static abstract class Builder<T extends Builder<T>> {

        private HttpClientAdapter httpClientAdapter; // Optional
        private int threadCount = 50; // Optional
        private ScheduledExecutorService requestExecutorService = null; // Optional
        private String collectorUrl = null; // Required if not specifying a httpClientAdapter
        private CookieJar cookieJar; // Optional
        protected abstract T self();

        /**
         * Set a custom ScheduledExecutorService to send http requests (default is ScheduledThreadPoolExecutor).
         * <p>
         * <b>Implementation note: </b><em>Be aware that calling `close()` on a BatchEmitter instance
         * has a side-effect and will shutdown that ExecutorService.</em>
         *
         * @param executorService the ScheduledExecutorService to use
         * @return itself
         */
        public T requestExecutorService(final ScheduledExecutorService executorService) {
            this.requestExecutorService = executorService;
            return self();
        }

        /**
         * Adds a custom HttpClientAdapter to the AbstractEmitter (default is OkHttpClientAdapter).
         *
         * @param httpClientAdapter the adapter to use
         * @return itself
         */
        public T httpClientAdapter(final HttpClientAdapter httpClientAdapter) {
            this.httpClientAdapter = httpClientAdapter;
            return self();
        }

        /**
         * Sets the Thread Count for the ScheduledExecutorService (default is 50).
         *
         * @param threadCount the size of the thread pool
         * @return itself
         */
        public T threadCount(final int threadCount) {
            this.threadCount = threadCount;
            return self();
        }

        /**
         * Sets the emitter url for when a httpClientAdapter is not specified.
         * It will be used to create the default OkHttpClientAdapter.
         *
         * @param collectorUrl the url for the default httpClientAdapter
         * @return itself
         */
        public T url(final String collectorUrl) {
            this.collectorUrl = collectorUrl;
            return self();
        }

        /**
         * Adds a custom CookieJar to be used with OkHttpClientAdapters.
         * Will be ignored if a custom httpClientAdapter is provided.
         *
         * @param cookieJar the CookieJar to use
         * @return itself
         */
        public T cookieJar(final CookieJar cookieJar) {
            this.cookieJar = cookieJar;
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

            this.httpClientAdapter = OkHttpClientAdapter.builder() // use okhttp as a default
                    .url(builder.collectorUrl)
                    .httpClient(
                        new OkHttpClient.Builder()
                                .cookieJar(builder.cookieJar == null ? new CollectorCookieJar() : builder.cookieJar)
                                .build())
                    .build();
        }

        if (builder.requestExecutorService != null) {
            this.executor = builder.requestExecutorService;
        } else {
            this.executor = Executors.newScheduledThreadPool(builder.threadCount, new EmitterThreadFactory());
        }
    }

    /**
     * Adds a payload to the buffer
     *
     * @param payload an payload
     */
    @Override
    public abstract boolean add(TrackerPayload payload);

    /**
     * Customize the emitter batch size to any valid integer greater than zero.
     * Has no effect on SimpleEmitter
     *
     * @param batchSize number of events to collect before sending
     */
    @Override
    public abstract void setBatchSize(final int batchSize);

    /**
     * Removes all payloads from the buffer and sends them
     */
    @Override
    public abstract void flushBuffer();

    /**
     * Gets the Emitter Batch Size - Will always be 1 for SimpleEmitter
     *
     * @return the batch size
     */
    @Override
    public abstract int getBatchSize();

    /**
     * Returns List of Payloads that are in the buffer.
     *
     * @return the buffered events
     */
    @Override
    public abstract List<TrackerPayload> getBuffer();

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
