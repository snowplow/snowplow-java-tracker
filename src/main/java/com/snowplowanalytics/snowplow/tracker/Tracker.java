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
package com.snowplowanalytics.snowplow.tracker;

// Java
import java.util.*;

// This library
import com.snowplowanalytics.snowplow.tracker.constants.Constants;
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.events.EcommerceTransaction;
import com.snowplowanalytics.snowplow.tracker.events.EcommerceTransactionItem;
import com.snowplowanalytics.snowplow.tracker.events.PageView;
import com.snowplowanalytics.snowplow.tracker.events.ScreenView;
import com.snowplowanalytics.snowplow.tracker.events.Structured;
import com.snowplowanalytics.snowplow.tracker.events.TimingWithCategory;
import com.snowplowanalytics.snowplow.tracker.events.Unstructured;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

public class Tracker {

    private final String trackerVersion = Version.TRACKER;
    private Emitter emitter;
    private Subject subject;
    private String appId;
    private String namespace;
    private DevicePlatform platform = DevicePlatform.ServerSideApp;
    private boolean base64Encoded = true;

    /**
     * @param emitter   Emitter to which events will be sent
     * @param namespace Identifier for the Tracker instance
     * @param appId     Application ID
     */
    public Tracker(Emitter emitter, String namespace, String appId) {
        this(emitter, null, namespace, appId);
    }

    /**
     * @param emitter   Emitter to which events will be sent
     * @param subject   Subject to be tracked
     * @param namespace Identifier for the Tracker instance
     * @param appId     Application ID
     */
    public Tracker(Emitter emitter, Subject subject, String namespace, String appId) {
        this.emitter = emitter;
        this.appId = appId;
        this.namespace = namespace;
        this.subject = subject;
    }

    // --- Helpers

    /**
     * Builds and Adds a finalised payload which is ready for sending.
     *
     * @param payload   The raw event Payload
     * @param contexts  Custom context for the event
     */
    private void addTrackerPayload(TrackerPayload payload, List<SelfDescribingJson> contexts) {

        // Add default parameters to the payload
        payload.add(Parameter.PLATFORM, platform.toString());
        payload.add(Parameter.APP_ID, this.appId);
        payload.add(Parameter.NAMESPACE, this.namespace);
        payload.add(Parameter.TRACKER_VERSION, this.trackerVersion);

        // Build the final context and add it to the payload
        if (contexts != null && contexts.size() > 0) {
            SelfDescribingJson envelope = getFinalContext(contexts);
            payload.addMap(envelope.getMap(), this.base64Encoded, Parameter.CONTEXT_ENCODED, Parameter.CONTEXT);
        }

        // Add subject if available
        if (this.subject != null) {
            payload.addMap(new HashMap<String, Object>(subject.getSubject()));
        }

        // Send the event!
        this.emitter.emit(payload);
    }

    /**
     * Builds the final event context.
     *
     * @param contexts the base event context
     * @return the final event context json with
     * many contexts inside
     */
    private SelfDescribingJson getFinalContext(List<SelfDescribingJson> contexts) {
        List<Map> contextMaps = new LinkedList<>();
        for (SelfDescribingJson selfDescribingJson : contexts) {
            contextMaps.add(selfDescribingJson.getMap());
        }
        return new SelfDescribingJson(Constants.SCHEMA_CONTEXTS, contextMaps);
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

    /**
     * Sets the Trackers platform, defaults to a
     * Server Side Application.
     *
     * @param platform the DevicePlatform
     */
    public void setPlatform(DevicePlatform platform) {
        this.platform = platform;
    }

    /**
     * Sets whether to base64 Encode custom contexts
     * and unstructured events
     *
     * @param base64Encoded a boolean truth
     */
    public void setBase64Encoded(boolean base64Encoded) {
        this.base64Encoded = base64Encoded;
    }

    /**
     * Sets a new Application ID
     *
     * @param appId the new application id
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * Sets a new Tracker Namespace
     *
     * @param namespace the new tracker namespace
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
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
        return this.trackerVersion;
    }

    /**
     * @return the trackers namespace
     */
    public String getNamespace() {
        return this.namespace;
    }

    /**
     * @return the trackers set Application ID
     */
    public String getAppId() {
        return this.appId;
    }

    /**
     * @return the base64 setting of the tracker
     */
    public boolean getBase64Encoded() {
        return this.base64Encoded;
    }

    /**
     * @return the Tracker platform
     */
    public DevicePlatform getPlatform() {
        return this.platform;
    }

    // --- Event Tracking Functions

    /**
     * Tracks a PageView event
     *
     * @param event the PageView event.
     */
    public void track(PageView event) {
        List<SelfDescribingJson> context = event.getContext();
        TrackerPayload payload = event.getPayload();
        addTrackerPayload(payload, context);
    }

    /**
     * Tracks a Structured Event.
     *
     * @param event the Structured event.
     */
    public void track(Structured event) {
        List<SelfDescribingJson> context = event.getContext();
        TrackerPayload payload = event.getPayload();
        addTrackerPayload(payload, context);
    }

    /**
     * Tracks an Ecommerce Transaction Event.
     * - Will also track any Items in separate
     *   payloads.
     *
     * @param event the Ecommerce Transaction event.
     */
    public void track(EcommerceTransaction event) {
        List<SelfDescribingJson> context = event.getContext();
        TrackerPayload payload = event.getPayload();
        addTrackerPayload(payload, context);

        // Track each TransactionItem individually
        long timestamp = event.getTimestamp();
        for(EcommerceTransactionItem item : event.getItems()) {
            track(item, timestamp);
        }
    }

    /**
     * Tracks an Ecommerce Transaction Item event.
     *
     * @param event the Ecommerce Transaction Item event
     * @param timestamp the Timestamp of the Transaction
     */
    private void track(EcommerceTransactionItem event, long timestamp) {
        List<SelfDescribingJson> context = event.getContext();
        TrackerPayload payload = event.getPayload(timestamp);
        addTrackerPayload(payload, context);
    }

    /**
     * Tracks an Unstructured Event.
     *
     * @param event the Structured event.
     */
    public void track(Unstructured event) {
        List<SelfDescribingJson> context = event.getContext();
        TrackerPayload payload = event.getPayload(base64Encoded);
        addTrackerPayload(payload, context);
    }

    /**
     * Tracks a ScreenView Event.
     *
     * @param event the ScreenView event.
     */
    public void track(ScreenView event) {
        this.track(Unstructured.builder()
                .eventData(event.getSelfDescribingJson())
                .customContext(event.getContext())
                .timestamp(event.getTimestamp())
                .eventId(event.getEventId())
                .build());
    }

    /**
     * Tracks a TimingWithCategory Event.
     *
     * @param event the TimingWithCategory event.
     */
    public void track(TimingWithCategory event) {
        this.track(Unstructured.builder()
                .eventData(event.getSelfDescribingJson())
                .customContext(event.getContext())
                .timestamp(event.getTimestamp())
                .eventId(event.getEventId())
                .build());
    }
}
