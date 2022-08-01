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

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;
import com.snowplowanalytics.snowplow.tracker.constants.Constants;
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.http.OkHttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An emitter that emits a batch of events in a single HTTP request.
 * It uses the POST method of the underlying HTTP adapter.
 *
 * When a new event (TrackerPayload) is received and added to the buffer, the BatchEmitter checks the
 * number of buffered events. If it is equal to or greater than the `batchSize`, an attempt is made to send
 * a batch of events as one request. Events are sent asynchronously.
 *
 * If the request is unsuccessful, the events are returned to the buffer. A delay is introduced for all
 * event sending attempts. This increases exponentially until a request succeeds, when it is reset to 0.
 * Retry will continue indefinitely.
 *
 * If the buffer becomes full due to network problems, newer events will be lost.
 */
public class BatchEmitter implements Emitter, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchEmitter.class);
    private boolean isClosing = false;
    private final AtomicInteger retryDelay;
    private final int maximumRetryDelay = 600000; // ms (10 min)
    private int batchSize;

    private final HttpClientAdapter httpClientAdapter;
    private final ScheduledExecutorService executor;
    private final EventStore eventStore;
    private final List<Integer> fatalResponseCodes;

    public static abstract class Builder<T extends Builder<T>> {
        protected abstract T self();

        private HttpClientAdapter httpClientAdapter; // Optional
        private String collectorUrl = null; // Required if not specifying a httpClientAdapter
        private int batchSize = 50; // Optional
        private int bufferCapacity = Integer.MAX_VALUE;
        private EventStore eventStore = null;  // Optional
        private List<Integer> fatalResponseCodes = null;  // Optional
        private int threadCount = 50; // Optional
        private ScheduledExecutorService requestExecutorService = null; // Optional

        /**
         * Adds a custom HttpClientAdapter to the Emitter (default is OkHttpClientAdapter).
         *
         * @param httpClientAdapter the adapter to use
         * @return itself
         */
        public T httpClientAdapter(final HttpClientAdapter httpClientAdapter) {
            this.httpClientAdapter = httpClientAdapter;
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
         * The default batch size is 50.
         *
         * @param batchSize The count of events to send in one HTTP request
         * @return itself
         */
        public T batchSize(final int batchSize) {
            this.batchSize = batchSize;
            return self();
        }

        /**
         * The default buffer capacity is Integer.MAX_VALUE. Your application would likely run out
         * of memory before buffering this many events. When the buffer is full, new events are lost.
         *
         * @param bufferCapacity The maximum capacity of the default InMemoryEventStore event buffer
         * @return itself
         */
        public T bufferCapacity(final int bufferCapacity) {
            this.bufferCapacity = bufferCapacity;
            return self();
        }

        /**
         * The default EventStore is InMemoryEventStore.
         *
         * @param eventStore The EventStore to use
         * @return itself
         */
        public T eventStore(final EventStore eventStore) {
            this.eventStore = eventStore;
            return self();
        }

        /**
         * Provide a denylist of HTTP response codes. Retry will not be attempted if one of these codes
         * is received. The events in the request will be dropped, but the Emitter will continue trying
         * to send as normal.
         *
         * @param fatalResponseCodes Event sending will not be retried on these codes
         * @return itself
         */
        public T fatalResponseCodes(final List<Integer> fatalResponseCodes) {
            this.fatalResponseCodes = fatalResponseCodes;
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
         * Set a custom ScheduledExecutorService to send http requests (default is ScheduledThreadPoolExecutor).
         * <p>
         * <b>Implementation note: </b><em>Be aware that calling `close()` on a BatchEmitter instance
         * has a side-effect and will shutdown that ExecutorService.</em>
         *
         * @param requestExecutorService the ScheduledExecutorService to use
         * @return itself
         */
        public T requestExecutorService(final ScheduledExecutorService requestExecutorService) {
            this.requestExecutorService = requestExecutorService;
            return self();
        }

        public BatchEmitter build() {
            return new BatchEmitter(this);
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

    protected BatchEmitter(final Builder<?> builder) {
        // Precondition checks
        Preconditions.checkArgument(builder.threadCount > 0, "threadCount must be greater than 0");
        Preconditions.checkArgument(builder.batchSize > 0, "batchSize must be greater than 0");

        if (builder.httpClientAdapter != null) {
            httpClientAdapter = builder.httpClientAdapter;
        } else {
            Preconditions.checkNotNull(builder.collectorUrl, "Collector url must be specified if not using a httpClientAdapter");

            httpClientAdapter = OkHttpClientAdapter.builder()
                    .url(builder.collectorUrl)
                    .httpClient(
                            new OkHttpClient()) // use okhttp as a default
                    .build();
        }

        retryDelay = new AtomicInteger(0);
        batchSize = builder.batchSize;

        if (builder.eventStore != null) {
            eventStore = builder.eventStore;
        } else {
            eventStore = new InMemoryEventStore(builder.bufferCapacity);
        }

        if (builder.fatalResponseCodes != null) {
            fatalResponseCodes = builder.fatalResponseCodes;
        } else {
            fatalResponseCodes = new ArrayList<>();
        }

        if (builder.requestExecutorService != null) {
            executor = builder.requestExecutorService;
        } else {
            executor = Executors.newScheduledThreadPool(builder.threadCount, new EmitterThreadFactory());
        }
    }

    /**
     * Adds a TrackerPayload to the EventStore buffer.
     * If the buffer is full, the payload will be lost.
     *
     * <p>
     * <b>Implementation note: </b><em>As a side effect it triggers an Emitter thread to emit a batch of events.</em>
     *
     * @param payload a TrackerPayload
     * @return whether the payload has been successfully added to the buffer.
     */
    @Override
    public boolean add(final TrackerPayload payload) {
        boolean result = eventStore.addEvent(payload);

        if (!isClosing) {
            if (eventStore.size() >= batchSize) {
                executor.schedule(getPostRequestRunnable(batchSize), retryDelay.get(), TimeUnit.MILLISECONDS);
            }
        }
        
        if (!result) {
            LOGGER.error("Unable to add payload to emitter, emitter buffer is full");
        }

        return result;
    }

    /**
     * Forces all the payloads currently in the buffer to be sent immediately, as a single request.
     */
    @Override
    public void flushBuffer() {
        executor.schedule(getPostRequestRunnable(eventStore.size()), 0, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns a List of Payloads that are in the buffer.
     *
     * @return the buffered events
     */
    @Override
    public List<TrackerPayload> getBuffer() {
        return eventStore.getAllEvents();
    }

    /**
     * Customize the emitter batch size to any valid integer greater than zero.
     *
     * @param batchSize number of events to send in one request
     */
    @Override
    public void setBatchSize(final int batchSize) {
        Preconditions.checkArgument(batchSize > 0, "batchSize must be greater than 0");
        this.batchSize = batchSize;
    }

    /**
     * Gets the Emitter batchSize
     *
     * @return the batch size
     */
    @Override
    public int getBatchSize() {
        return batchSize;
    }

    int getRetryDelay() {
        return retryDelay.get();
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
     * Returns a Runnable POST Request operation
     *
     * @param numberOfEvents the number of events to be sent in the request
     * @return the new Runnable object
     */
    private Runnable getPostRequestRunnable(int numberOfEvents) {
        return () -> {
            BatchPayload batchedEvents = null;
            try {
                batchedEvents = eventStore.getEventsBatch(numberOfEvents);

                if (batchedEvents == null || batchedEvents.size() == 0) {
                    return;
                }

                List<TrackerPayload> eventsInRequest = batchedEvents.getPayloads();
                final SelfDescribingJson post = getFinalPost(eventsInRequest);
                final int code = httpClientAdapter.post(post);

                // Process results
                if (isSuccessfulSend(code)) {
                    LOGGER.debug("BatchEmitter successfully sent {} events: code: {}", eventsInRequest.size(), code);
                    retryDelay.set(0);
                    eventStore.cleanupAfterSendingAttempt(false, batchedEvents.getBatchId());

                } else if (fatalResponseCodes.contains(code)) {
                    LOGGER.debug("BatchEmitter failed to send {} events. No retry for code {}: events dropped", eventsInRequest.size(), code);
                    eventStore.cleanupAfterSendingAttempt(false, batchedEvents.getBatchId());

                } else {
                    LOGGER.error("BatchEmitter failed to send {} events: code: {}", eventsInRequest.size(), code);
                    eventStore.cleanupAfterSendingAttempt(true, batchedEvents.getBatchId());

                    // exponentially increase retry backoff time after the first failure, up to the maximum wait time
                    if (!retryDelay.compareAndSet(0, 100)) {
                        retryDelay.updateAndGet(this::calculateRetryDelay);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("BatchEmitter event sending error: {}", e.getMessage());
                if (batchedEvents != null) {
                    eventStore.cleanupAfterSendingAttempt(true, batchedEvents.getBatchId());
                }
            }
        };
    }

    /**
     * Constructs the SelfDescribingJson to be sent to the endpoint
     *
     * @param events the event buffer
     * @return the constructed POST payload
     */
    private SelfDescribingJson getFinalPost(final List<TrackerPayload> events) {
        final List<Map<String, String>> toSendPayloads = new ArrayList<>();
        final String sentTimestamp = Long.toString(System.currentTimeMillis());

        for (TrackerPayload payload : events) {
            payload.add(Parameter.DEVICE_SENT_TIMESTAMP, sentTimestamp);
            toSendPayloads.add(payload.getMap());
        }

        return new SelfDescribingJson(Constants.SCHEMA_PAYLOAD_DATA, toSendPayloads);
    }

    private int calculateRetryDelay(int currentDelay) {
        double newDelay;
        double jitter = Math.random();
        int randomChoice = (Math.random() < 0.5) ? 0 : 1;

        switch (randomChoice) {
            case 0:
                newDelay = currentDelay * (2.0 + jitter);
                break;
            case 1:
                newDelay = currentDelay * (2.0 - jitter);
                break;
            default:
                newDelay = currentDelay;
        }
        return Math.min((int) newDelay, maximumRetryDelay);
    }

    /**
     * Attempt to send all remaining events, then shut down the ExecutorService.
     *
     * <p>
     *  <b>Implementation note: </b><em>Be aware that calling `close()`
     *  has a side-effect of shutting down the Emitter ScheduledExecutorService.</em>
     */
    @Override
    public void close() {
        final long closeTimeout = 5;
        isClosing = true;

        flushBuffer(); // Attempt to send all remaining events

        //Shutdown executor threadpool
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(closeTimeout, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(closeTimeout, TimeUnit.SECONDS))
                        LOGGER.warn("Emitter executor did not terminate");
                }
            } catch (final InterruptedException ie) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
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
            group = securityManager != null ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "snowplow-emitter-pool-" + poolNumber.getAndIncrement() + "-request-thread-";
        }

        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement(), 0L);
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
