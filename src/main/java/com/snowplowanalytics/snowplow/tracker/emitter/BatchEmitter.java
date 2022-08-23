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
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.snowplowanalytics.snowplow.tracker.configuration.EmitterConfiguration;
import com.snowplowanalytics.snowplow.tracker.configuration.NetworkConfiguration;
import com.snowplowanalytics.snowplow.tracker.constants.Constants;
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.http.OkHttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

import okhttp3.CookieJar;
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
    private final Map<Integer, Boolean> customRetryForStatusCodes;
    private final EmitterCallback callback;

    public static abstract class Builder<T extends Builder<T>> {
        protected abstract T self();

        private HttpClientAdapter httpClientAdapter; // Optional
        private String collectorUrl = null; // Required if not specifying a httpClientAdapter
        private int batchSize = 50; // Optional
        private int bufferCapacity = 10000;
        private EventStore eventStore = null;  // Optional
        private Map<Integer, Boolean> customRetryForStatusCodes = null; // Optional
        private int threadCount = 50; // Optional
        private CookieJar cookieJar = null; // Optional
        private ScheduledExecutorService requestExecutorService = null; // Optional
        private EmitterCallback callback = null; // Optional

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
         * The default buffer capacity is 10 000 events.
         * When the buffer is full (due to network outage), new events are lost.
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
         * Set custom retry rules for HTTP status codes received in emit responses from the Collector.
         * By default, retry will not occur for status codes 400, 401, 403, 410 or 422. This can be overridden here.
         * Note that 2xx codes will never retry as they are considered successful.
         * @param customRetryForStatusCodes Mapping of integers (status codes) to booleans (true for retry and false for not retry)
         * @return itself
         */
        public T customRetryForStatusCodes(Map<Integer, Boolean> customRetryForStatusCodes) {
            this.customRetryForStatusCodes = customRetryForStatusCodes;
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

        /**
         * Provide a custom EmitterCallback to access successfully sent or failed event payloads.
         *
         * @param callback an EmitterCallback
         * @return itself
         */
        public T callback(final EmitterCallback callback) {
            this.callback = callback;
            return self();
        }

        public BatchEmitter build() {
            NetworkConfiguration networkConfig = new NetworkConfiguration()
                    .collectorUrl(collectorUrl)
                    .httpClientAdapter(httpClientAdapter)
                    .cookieJar(cookieJar);

            EmitterConfiguration emitterConfig = new EmitterConfiguration()
                    .batchSize(batchSize)
                    .bufferCapacity(bufferCapacity)
                    .eventStore(eventStore)
                    .customRetryForStatusCodes(customRetryForStatusCodes)
                    .threadCount(threadCount)
                    .requestExecutorService(requestExecutorService)
                    .callback(callback);

            return new BatchEmitter(networkConfig, emitterConfig);
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

    /**
     * Creates a BatchEmitter object from configuration objects.
     *
     * @param networkConfig a NetworkConfiguration object
     * @param emitterConfig an EmitterConfiguration object
     */
    public BatchEmitter(NetworkConfiguration networkConfig, EmitterConfiguration emitterConfig) {
        OkHttpClient client;

        // Precondition checks
        if (emitterConfig.getThreadCount() <= 0) {
            throw new IllegalArgumentException("threadCount must be greater than 0");
        }
        if (emitterConfig.getBatchSize() <= 0) {
            throw new IllegalArgumentException("batchSize must be greater than 0");
        }
        if (emitterConfig.getBufferCapacity() <= 0) {
            throw new IllegalArgumentException("bufferCapacity must be greater than 0");
        }

        if (networkConfig.getHttpClientAdapter() != null) {
            httpClientAdapter = networkConfig.getHttpClientAdapter();
        } else {
            Objects.requireNonNull(networkConfig.getCollectorUrl(), "Collector url must be specified if not using a httpClientAdapter");

            if (networkConfig.getCookieJar() != null) {
                client = new OkHttpClient.Builder()
                        .cookieJar(networkConfig.getCookieJar())
                        .build();
            } else {
                client = new OkHttpClient.Builder().build();
            }

            httpClientAdapter = OkHttpClientAdapter.builder() // use okhttp as a default
                    .url(networkConfig.getCollectorUrl())
                    .httpClient(client)
                    .build();
        }

        retryDelay = new AtomicInteger(0);
        batchSize = emitterConfig.getBatchSize();

        if (emitterConfig.getCallback() != null) {
            callback = emitterConfig.getCallback();
        } else {
            callback = new EmitterCallback() {
                @Override
                public void onSuccess(List<TrackerPayload> payloads) {}
                @Override
                public void onFailure(FailureType failureType, boolean willRetry, List<TrackerPayload> payloads) {}
            };
        }

        if (emitterConfig.getEventStore() != null) {
            eventStore = emitterConfig.getEventStore();
        } else {
            eventStore = new InMemoryEventStore(emitterConfig.getBufferCapacity());
        }

        if (emitterConfig.getCustomRetryForStatusCodes() != null) {
            customRetryForStatusCodes = emitterConfig.getCustomRetryForStatusCodes();
        } else {
            customRetryForStatusCodes = new HashMap<>();
        }

        if (emitterConfig.getRequestExecutorService() != null) {
            executor = emitterConfig.getRequestExecutorService();
        } else {
            executor = Executors.newScheduledThreadPool(emitterConfig.getThreadCount(), new EmitterThreadFactory());
        }
    }

    /**
     * Creates a BatchEmitter instance using a NetworkConfiguration.
     *
     * @param networkConfig a NetworkConfiguration object
     */
    public BatchEmitter(NetworkConfiguration networkConfig) {
        this(networkConfig, new EmitterConfiguration());
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
            callback.onFailure(FailureType.TRACKER_STORAGE_FULL, false, Collections.singletonList(payload));
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
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be greater than 0");
        }
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

    protected boolean shouldRetry(int code) {
        // don't retry if successful
        if (isSuccessfulSend(code)) {
            return false;
        }

        // status code has a custom retry rule
        if (customRetryForStatusCodes.containsKey(code)) {
            return Objects.requireNonNull(customRetryForStatusCodes.get(code));
        }

        // retry if status code is not in the list of no-retry status codes
        Set<Integer> dontRetryStatusCodes = new HashSet<>(Arrays.asList(400, 401, 403, 410, 422));
        return !dontRetryStatusCodes.contains(code);
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

            // If the InMemoryEventStore queue is full when events are returned for retry,
            // newer events are removed to make space
            List<TrackerPayload> eventsDeletedFromStorage = new ArrayList<>();

            try {
                batchedEvents = eventStore.getEventsBatch(numberOfEvents);

                if (batchedEvents == null || batchedEvents.size() == 0) {
                    return;
                }

                List<TrackerPayload> eventsInRequest = new ArrayList<>(batchedEvents.getPayloads());
                final SelfDescribingJson post = getFinalPost(eventsInRequest);
                final int code = httpClientAdapter.post(post);

                // Process results
                if (isSuccessfulSend(code)) {
                    LOGGER.debug("BatchEmitter successfully sent {} events: code: {}", eventsInRequest.size(), code);
                    retryDelay.set(0);
                    eventStore.cleanupAfterSendingAttempt(false, batchedEvents.getBatchId());
                    callback.onSuccess(eventsInRequest);

                } else if (!shouldRetry(code)) {
                    LOGGER.debug("BatchEmitter failed to send {} events. No retry for code {}: events dropped", eventsInRequest.size(), code);
                    eventStore.cleanupAfterSendingAttempt(false, batchedEvents.getBatchId());
                    callback.onFailure(FailureType.REJECTED_BY_COLLECTOR, false, eventsInRequest);

                } else {
                    LOGGER.error("BatchEmitter failed to send {} events: code: {}", eventsInRequest.size(), code);
                    eventsDeletedFromStorage = eventStore.cleanupAfterSendingAttempt(true, batchedEvents.getBatchId());

                    if (code == -1) {
                        callback.onFailure(FailureType.HTTP_CONNECTION_FAILURE, true, eventsInRequest);
                    } else {
                        callback.onFailure(FailureType.REJECTED_BY_COLLECTOR, true, eventsInRequest);
                    }

                    if (!eventsDeletedFromStorage.isEmpty()) {
                        callback.onFailure(FailureType.TRACKER_STORAGE_FULL, false, eventsDeletedFromStorage);
                    }

                    // exponentially increase retry backoff time after the first failure, up to the maximum wait time
                    if (!retryDelay.compareAndSet(0, 100)) {
                        retryDelay.updateAndGet(this::calculateRetryDelay);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("BatchEmitter event sending error: {}", e.getMessage());
                if (batchedEvents != null) {
                    eventsDeletedFromStorage = eventStore.cleanupAfterSendingAttempt(true, batchedEvents.getBatchId());
                    callback.onFailure(FailureType.EMITTER_REQUEST_FAILURE, true, new ArrayList<>(batchedEvents.getPayloads()));

                    if (!eventsDeletedFromStorage.isEmpty()) {
                        callback.onFailure(FailureType.TRACKER_STORAGE_FULL, false, eventsDeletedFromStorage);
                    }
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
