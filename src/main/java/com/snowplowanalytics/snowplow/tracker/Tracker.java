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
package com.snowplowanalytics.snowplow.tracker;

import com.snowplowanalytics.snowplow.tracker.configuration.TrackerConfiguration;
import com.snowplowanalytics.snowplow.tracker.constants.Constants;
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.events.*;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerParameters;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

import java.util.*;

/**
 * Allows tracking of Events.
 */
public class Tracker {

    private Emitter emitter;
    private Subject subject;
    private final TrackerParameters parameters;

    /**
     * Creates a new Snowplow Tracker.
     *
     * @param trackerConfig a TrackerConfiguration object
     * @param emitter an Emitter
     * @param subject a Subject
     *
     */
    public Tracker(TrackerConfiguration trackerConfig, Emitter emitter, Subject subject) {

        // Precondition checks
        Objects.requireNonNull(emitter);
        Objects.requireNonNull(trackerConfig.getNamespace());
        Objects.requireNonNull(trackerConfig.getAppId());
        if (trackerConfig.getNamespace().isEmpty()) {
            throw new IllegalArgumentException("namespace cannot be empty");
        }
        if (trackerConfig.getAppId().isEmpty()) {
            throw new IllegalArgumentException("appId cannot be empty");
        }

        this.parameters = new TrackerParameters(trackerConfig.getAppId(), trackerConfig.getPlatform(), trackerConfig.getNamespace(), Version.TRACKER, trackerConfig.isBase64Encoded());
        this.emitter = emitter;
        this.subject = subject;

    }

    /**
     * Creates a new Snowplow Tracker.
     *
     * @param trackerConfig a TrackerConfiguration object
     * @param emitter an Emitter
     *
     */
    public Tracker(TrackerConfiguration trackerConfig, Emitter emitter) {
        this(trackerConfig, emitter, null);
    }

    /**
     * Builder for the Tracker
     * @deprecated Use TrackerConfiguration class instead
     */
    @Deprecated
    public static class TrackerBuilder {

        private final Emitter emitter; // Required
        private final String namespace; // Required
        private final String appId; // Required
        private Subject subject = null; // Optional
        private DevicePlatform platform = DevicePlatform.ServerSideApp; // Optional
        private boolean base64Encoded = true; // Optional

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
         * The {@link DevicePlatform} the tracker is running on (default is "srv", ServerSideApp).
         *
         * @param platform The device platform the tracker is running on
         * @return itself
         */
        public TrackerBuilder platform(DevicePlatform platform) {
            this.platform = platform;
            return this;
        }

        /**
         * Whether JSONs in the payload should be base-64 encoded (default is true)
         *
         * @param base64 JSONs should be encoded or not
         * @return itself
         */
        public TrackerBuilder base64(Boolean base64) {
            this.base64Encoded = base64;
            return this;
        }

        /**
         * Creates a new Tracker
         *
         * @return a new Tracker object
         */
        public Tracker build() {
            TrackerConfiguration trackerConfig = new TrackerConfiguration(namespace, appId)
                    .platform(platform)
                    .base64Encoded(base64Encoded);
            return new Tracker(trackerConfig, emitter, subject);
        }
    }

    /**
     * @deprecated Use TrackerConfiguration class instead
     * @param emitter Emitter object
     * @param namespace unique tracker namespace
     * @param appId application ID
     * @return TrackerBuilder object
     */
    @Deprecated
    public static TrackerBuilder builder(Emitter emitter, String namespace, String appId) {
        return new TrackerBuilder(emitter, namespace, appId);
    }

    // --- Setters

    /**
     * Change the Emitter used to send events.
     *
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
     * @return the Tracker-associated Subject
     */
    public Subject getSubject() {
        return this.subject;
    }

    /**
     * The Java tracker release version, e.g. 0.12.0.
     *
     * @return the tracker version
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
     * @return the tracker Application ID
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
     * @return the Tracker platform, e.g. "srv"
     */
    public DevicePlatform getPlatform() {
        return this.parameters.getPlatform();
    }

    /**
     * @return the wrapper containing the Tracker parameters
     */
    public TrackerParameters getParameters() {
        return parameters;
    }

    // --- Event Tracking Functions

    /**
     * Handles tracking the different types of events.
     *
     * A TrackerPayload object - or more than one, in the case of eCommerceTransaction events -
     * will be created from the Event. This is passed to the configured Emitter.
     * If the event was successfully added to the Emitter buffer for sending,
     * a list containing the payload's eventId string (a UUID) is returned.
     * EcommerceTransactions will return all the relevant eventIds in the list.
     * If the Emitter event buffer is full, the payload will be lost. In this case, this method
     * returns a list containing null.
     * <p>
     * <b>Implementation note: </b><em>As a side effect of adding a payload to the Emitter,
     * it triggers an Emitter thread to emit a batch of events.</em>
     *
     * @param event the event to track
     * @return a list of eventIDs (UUIDs)
     */
    public List<String> track(Event event) {
        List<String> results = new ArrayList<>();
        // a list because Ecommerce events become multiple Payloads
        List<Event> processedEvents = eventTypeSpecificPreProcessing(event);
        for (Event processedEvent : processedEvents) {
            // Event ID (eid) and device_created_timestamp (dtm) are generated now when
            // the TrackerPayload is created
            TrackerPayload payload = (TrackerPayload) processedEvent.getPayload();

            addTrackerParameters(payload);
            addContext(processedEvent, payload);
            addSubject(processedEvent, payload);

            boolean addedToBuffer = emitter.add(payload);
            if (addedToBuffer) {
                results.add(payload.getEventId());
            } else {
                results.add(null);
            }
        }
        return results;
    }

    private List<Event> eventTypeSpecificPreProcessing(Event event) {
        // Different event types must be processed in slightly different ways.
        // EcommerceTransaction events are an outlier, as they are processed into
        // multiple payloads (a "tr" event plus one "ti" event per item).
        // Because of this, this method returns a list of Events.
        List<Event> eventList = new ArrayList<>();
        final Class<?> eventClass = event.getClass();

        if (eventClass.equals(SelfDescribing.class)) {
            // Need to set the Base64 rule for SelfDescribing events
            final SelfDescribing selfDescribing = (SelfDescribing) event;
            selfDescribing.setBase64Encode(parameters.getBase64Encoded());
            eventList.add(selfDescribing);

        } else if (eventClass.equals(EcommerceTransaction.class)) {
            final EcommerceTransaction ecommerceTransaction = (EcommerceTransaction) event;
            eventList.add(ecommerceTransaction);

            // Track each item individually
            eventList.addAll(ecommerceTransaction.getItems());

        } else if (eventClass.equals(Timing.class) || eventClass.equals(ScreenView.class)) {
            // Timing and ScreenView events are wrapper classes for SelfDescribing events
            // Need to create SelfDescribing events from them to send.
            final SelfDescribing selfDescribing = SelfDescribing.builder()
                    .eventData((SelfDescribingJson) event.getPayload())
                    .customContext(event.getContext())
                    .trueTimestamp(event.getTrueTimestamp())
                    .subject(event.getSubject())
                    .build();

            selfDescribing.setBase64Encode(parameters.getBase64Encoded());
            eventList.add(selfDescribing);

        } else {
            eventList.add(event);
        }
        return eventList;
    }

    private void addTrackerParameters(TrackerPayload payload) {
        payload.add(Parameter.PLATFORM, parameters.getPlatform().toString());
        payload.add(Parameter.APP_ID, parameters.getAppId());
        payload.add(Parameter.NAMESPACE, parameters.getNamespace());
        payload.add(Parameter.TRACKER_VERSION, parameters.getTrackerVersion());
    }

    private void addContext(Event event, TrackerPayload payload) {
        List<SelfDescribingJson> entities = event.getContext();

        // Build the final context and add it to the payload
        if (entities != null && entities.size() > 0) {
            SelfDescribingJson envelope = getFinalContext(entities);
            payload.addMap(envelope.getMap(), parameters.getBase64Encoded(), Parameter.CONTEXT_ENCODED, Parameter.CONTEXT);
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
        } else if (subject != null) {
            payload.addMap(new HashMap<>(subject.getSubject()));
        }
    }

    /**
     * Attempts to send all remaining events, then shuts down the Emitter so that no more events can be sent.
     * This method calls the Emitter.close() method. For the default BatchEmitter,
     * this stops the ScheduledExecutorService and the thread pool (non-daemon threads).
     * There is no way to restart the Emitter after calling close().
     */
    public void close() {
        emitter.close();
    }

}
