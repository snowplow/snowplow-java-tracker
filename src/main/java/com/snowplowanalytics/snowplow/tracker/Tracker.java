/*
 * Copyright (c) 2014-2021 Snowplow Analytics Ltd. All rights reserved.
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
package com.snowplowanalytics.snowplow.tracker;

import com.google.common.base.Preconditions;

import com.snowplowanalytics.snowplow.tracker.constants.Constants;
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.events.*;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerParameters;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class Tracker {

    private Emitter emitter;
    private Subject subject;
    private final TrackerParameters parameters;
    protected ExecutorService executor;

    /**
     * Creates a new Snowplow Tracker.
     *
     * @param builder The builder that constructs a tracker
     */
    private Tracker(TrackerBuilder builder) {

        // Precondition checks
        Preconditions.checkNotNull(builder.emitter);
        Preconditions.checkNotNull(builder.namespace);
        Preconditions.checkNotNull(builder.appId);
        Preconditions.checkArgument(!builder.namespace.isEmpty(), "namespace cannot be empty");
        Preconditions.checkArgument(!builder.appId.isEmpty(), "appId cannot be empty");

        this.parameters = new TrackerParameters(builder.appId, builder.platform, builder.namespace, Version.TRACKER, builder.base64Encoded);
        this.emitter = builder.emitter;
        this.subject = builder.subject;

        if (builder.requestExecutorService != null) {
            this.executor = builder.requestExecutorService;
        } else {
            this.executor = Executors.newScheduledThreadPool(builder.threadCount, new TrackerThreadFactory());
        }
    }

    /**
     * Builder for the Tracker
     */
    public static class TrackerBuilder {

        private final Emitter emitter; // Required
        private final String namespace; // Required
        private final String appId; // Required
        private Subject subject = null; // Optional
        private DevicePlatform platform = DevicePlatform.ServerSideApp; // Optional
        private boolean base64Encoded = true; // Optional
        private int threadCount = 50; // Optional
        private ExecutorService requestExecutorService = null; // Optional

        /**
         * @param emitter Emitter to which events will be sent
         * @param namespace Identifier for the Tracker instance
         * @param appId Application ID
         */
        public TrackerBuilder(Emitter emitter, String namespace, String appId) {
            this.emitter = emitter;
            this.namespace = namespace;
            this.appId = appId;
        }

        /**
         * @param subject Subject to be tracked
         * @return itself
         */
        public TrackerBuilder subject(Subject subject) {
            this.subject = subject;
            return this;
        }

        /**
         * @param platform The device platform the tracker is running on
         * @return itself
         */
        public TrackerBuilder platform(DevicePlatform platform) {
            this.platform = platform;
            return this;
        }

        /**
         * @param base64 Whether JSONs in the payload should be base-64 encoded
         * @return itself
         */
        public TrackerBuilder base64(Boolean base64) {
            this.base64Encoded = base64;
            return this;
        }

        /**
         * Sets the Thread Count for the ExecutorService
         *
         * @param threadCount the size of the thread pool
         * @return itself
         */
        public TrackerBuilder threadCount(final int threadCount) {
            this.threadCount = threadCount;
            return this;
        }

        /**
         * Set a custom ExecutorService to send http request.
         *
         * @param executorService the ExecutorService to use
         * @return itself
         */
        public TrackerBuilder requestExecutorService(final ExecutorService executorService) {
            this.requestExecutorService = executorService;
            return this;
        }



        /**
         * Creates a new Tracker
         *
         * @return a new Tracker object
         */
        public Tracker build() {
            return new Tracker(this);
        }
    }

    // --- Setters

    /**
     * @param emitter a new emitter
     */
    public void setEmitter(Emitter emitter) {
        this.emitter = emitter;
    }

    /**
     * Sets a new Subject object which will get attached to
     * each event payload.
     *
     * @param subject the new Subject
     */
    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    // --- Getters

    /**
     * @return the emitter associated with the tracker
     */
    public Emitter getEmitter() {
        return this.emitter;
    }

    /**
     * @return the Tracker Subject
     */
    public Subject getSubject() {
        return this.subject;
    }

    /**
     * @return the tracker version that was set
     */
    public String getTrackerVersion() {
        return this.parameters.getTrackerVersion();
    }

    /**
     * @return the trackers namespace
     */
    public String getNamespace() {
        return this.parameters.getNamespace();
    }

    /**
     * @return the trackers set Application ID
     */
    public String getAppId() {
        return this.parameters.getAppId();
    }

    /**
     * @return the base64 setting of the tracker
     */
    public boolean getBase64Encoded() {
        return this.parameters.getBase64Encoded();
    }

    /**
     * @return the Tracker platform
     */
    public DevicePlatform getPlatform() {
        return this.parameters.getPlatform();
    }

    /**
     * @return the wrapper containing the Tracker parameters
     */
    public TrackerParameters getParameters() {
        return this.parameters;
    }

    // --- Event Tracking Functions

    /**
     * Sends a runnable to the executor service.
     *
     * @param runnable the runnable to be queued
     */
    protected void execute(final Runnable runnable) {
        this.executor.execute(runnable);
    }

    /**
     * Copied from `Executors.defaultThreadFactory()`.
     * The only change is the generated name prefix.
     */
    static class TrackerThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        TrackerThreadFactory() {
            SecurityManager securityManager = System.getSecurityManager();
            this.group = securityManager != null ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
            this.namePrefix = "snowplow-tracker-pool-" + poolNumber.getAndIncrement() + "-event-thread-";
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

    /**
     * Handles tracking the different types of events that
     * the Tracker can encounter.
     *
     * @param event the event to track
     */
    public void track(Event event) {
        execute(getProcessEventRunnable(event));
    }

    private Runnable getProcessEventRunnable(Event event) {
        return () -> {
            // a list because Ecommerce events become multiple Payloads
            List<Event> processedEvents = eventTypeSpecificPreProcessing(event);
            for (Event processedEvent : processedEvents) {
                TrackerPayload payload = (TrackerPayload) processedEvent.getPayload();

                addTrackerParameters(payload);
                addContext(processedEvent, payload);
                addSubject(processedEvent, payload);
                this.emitter.add(payload);
            }
        };
    }

    private List<Event> eventTypeSpecificPreProcessing(Event event) {
        // Different event types must be processed in slightly different ways.
        // EcommerceTransaction events are an outlier, as they are processed into
        // multiple payloads (a "tr" event plus one "ti" event per item).
        // Because of this, this method returns a list of Events.
        List<Event> eventList = new ArrayList<>();
        final Class<?> eventClass = event.getClass();

        if (eventClass.equals(Unstructured.class)) {
            // Need to set the Base64 rule for Unstructured events
            final Unstructured unstructured = (Unstructured) event;
            unstructured.setBase64Encode(this.parameters.getBase64Encoded());
            eventList.add(unstructured);

        } else if (eventClass.equals(EcommerceTransaction.class)) {
            final EcommerceTransaction ecommerceTransaction = (EcommerceTransaction) event;
            eventList.add(ecommerceTransaction);

            // Track each item individually
            for (final EcommerceTransactionItem item : ecommerceTransaction.getItems()) {
                item.setDeviceCreatedTimestamp(ecommerceTransaction.getDeviceCreatedTimestamp());
                eventList.add(item);
            }
        } else if (eventClass.equals(Timing.class) || eventClass.equals(ScreenView.class)) {
            // Timing and ScreenView events are wrapper classes for Unstructured events
            // Need to create Unstructured events from them to send.
            final Unstructured unstructured = Unstructured.builder()
                    .eventData((SelfDescribingJson) event.getPayload())
                    .customContext(event.getContext())
                    .deviceCreatedTimestamp(event.getDeviceCreatedTimestamp())
                    .trueTimestamp(event.getTrueTimestamp())
                    .eventId(event.getEventId())
                    .subject(event.getSubject())
                    .build();

            unstructured.setBase64Encode(this.parameters.getBase64Encoded());
            eventList.add(unstructured);

        } else {
            eventList.add(event);
        }
        return eventList;
    }

    private void addTrackerParameters(TrackerPayload payload) {
        payload.add(Parameter.PLATFORM, this.parameters.getPlatform().toString());
        payload.add(Parameter.APP_ID, this.parameters.getAppId());
        payload.add(Parameter.NAMESPACE, this.parameters.getNamespace());
        payload.add(Parameter.TRACKER_VERSION, this.parameters.getTrackerVersion());
    }

    private void addContext(Event event, TrackerPayload payload) {
        List<SelfDescribingJson> entities = event.getContext();

        // Build the final context and add it to the payload
        if (entities != null && entities.size() > 0) {
            SelfDescribingJson envelope = getFinalContext(entities);
            payload.addMap(envelope.getMap(), this.parameters.getBase64Encoded(), Parameter.CONTEXT_ENCODED, Parameter.CONTEXT);
        }
    }

    /**
     * Builds the final event context.
     *
     * @param entities the base event context
     * @return the final event context json with many entities inside
     */
    private SelfDescribingJson getFinalContext(List<SelfDescribingJson> entities) {
        List<Map<String, Object>> entityMaps = new LinkedList<>();
        for (SelfDescribingJson selfDescribingJson : entities) {
            entityMaps.add(selfDescribingJson.getMap());
        }
        return new SelfDescribingJson(Constants.SCHEMA_CONTEXTS, entityMaps);
    }

    private void addSubject(Event event, TrackerPayload payload) {
        Subject eventSubject = event.getSubject();

        // Add subject if available
        if (eventSubject != null) {
            payload.addMap(new HashMap<>(eventSubject.getSubject()));
        } else if (this.subject != null) {
            payload.addMap(new HashMap<>(this.subject.getSubject()));
        }
    }
}
