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
package com.snowplowanalytics.snowplow.tracker.payload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.snowplowanalytics.snowplow.tracker.Subject;
import com.snowplowanalytics.snowplow.tracker.constants.Constants;
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.events.*;

/**
 * A TrackerEvent which allows the TrackerPayload to be filled later. The payload will be
 * filled by the Emitter in the Emitter thread, using the getTrackerPayload() method.
 */
public class TrackerEvent {

    private final Event event;
    private final TrackerParameters parameters;
    private final Subject subject;

    public TrackerEvent(final Event event, final TrackerParameters parameters, final Subject subject) {
        this.event = event;
        this.parameters = parameters;
        this.subject = subject;
    }

    /**
     * Returns the {@link Event} 
     * 
     * @return The {@link Event}
     */
    public Event getEvent() {
        return this.event;
    }

    /**
     * Converts a {@link Event} to a list of {@link TrackerPayload} and caches the values.
     * Returns a list as some Events contain nested payloads (e.g. {@link EcommerceTransaction})
     * Adds fields to the {@link TrackerPayload} based on the type of the {@link Event}.
     * 
     * @return The populated TrackerPayloads
     */
    public List<TrackerPayload> getTrackerPayloads() {
        final List<TrackerPayload> payloads = new ArrayList<>();
        final List<SelfDescribingJson> contexts = event.getContext();
        final Subject subject = event.getSubject();

        // Figure out what type of event it is
        final Class<?> eventClass = event.getClass();
        
        if (eventClass.equals(Unstructured.class)) {

            // Need to set the Base64 rule for Unstructured events
            final Unstructured unstructured = (Unstructured) event;
            unstructured.setBase64Encode(this.parameters.getBase64Encoded());
            completePayloadCreation(payloads, unstructured.getPayload(), subject, contexts);

        } else if (eventClass.equals(Timing.class) || eventClass.equals(ScreenView.class)) {

            // These are wrapper classes for Unstructured events; need to create
            // Unstructured events from them and resend.
            final Unstructured unstructured = Unstructured.builder()
              .eventData((SelfDescribingJson) event.getPayload())
              .customContext(contexts)
              .deviceCreatedTimestamp(event.getDeviceCreatedTimestamp())
              .trueTimestamp(event.getTrueTimestamp())
              .eventId(event.getEventId())
              .subject(subject)
              .build();

            unstructured.setBase64Encode(this.parameters.getBase64Encoded());
            completePayloadCreation(payloads, unstructured.getPayload(), subject, contexts);

        } else if (eventClass.equals(EcommerceTransaction.class)) {

            final EcommerceTransaction ecommerceTransaction = (EcommerceTransaction) event;
            completePayloadCreation(payloads, ecommerceTransaction.getPayload(), subject, contexts);

            // Track each item individually
            for (final EcommerceTransactionItem item : ecommerceTransaction.getItems()) {

                item.setDeviceCreatedTimestamp(ecommerceTransaction.getDeviceCreatedTimestamp());
                completePayloadCreation(payloads, item.getPayload(), item.getSubject(), item.getContext());
            }
        } else {

            // For all other events, simply get the payload
            TrackerPayload payload = (TrackerPayload) event.getPayload();
            completePayloadCreation(payloads, payload, subject, contexts);
        }
        return payloads;
    }

    private void completePayloadCreation(List<TrackerPayload> payloads, TrackerPayload payload, Subject subject, List<SelfDescribingJson> contexts) {
        addTrackerParameters(payload);
        addContextsAndSubject(contexts, subject, payload);
        payloads.add(payload);
    }

    /**
     * Adds the context and subject to the event payload
     *
     * @param contexts the base event context - can be null or empty
     * @param subject the event subject - can be null
     * @param payload the payload to add the contexts and subjects to
     */
    private void addContextsAndSubject(final List<SelfDescribingJson> contexts, final Subject subject, TrackerPayload payload) {
        // Build the final context and add it to the payload
        if (contexts != null && contexts.size() > 0) {
            SelfDescribingJson envelope = getFinalContext(contexts);
            payload.addMap(envelope.getMap(), this.parameters.getBase64Encoded(), Parameter.CONTEXT_ENCODED, Parameter.CONTEXT);
        }

        // Add subject if available
        if (subject != null) {
            payload.addMap(new HashMap<>(subject.getSubject()));
        } else if (this.subject != null) {
            payload.addMap(new HashMap<>(this.subject.getSubject()));
        }
    }

    /**
     * Builds the final event context.
     *
     * @param contexts the base event context
     * @return the final event context json with many contexts inside
     */
    private SelfDescribingJson getFinalContext(List<SelfDescribingJson> contexts) {
        List<Map<String, Object>> contextMaps = new LinkedList<>();
        for (SelfDescribingJson selfDescribingJson : contexts) {
            contextMaps.add(selfDescribingJson.getMap());
        }
        return new SelfDescribingJson(Constants.SCHEMA_CONTEXTS, contextMaps);
    }

    private void addTrackerParameters(TrackerPayload payload) {
        payload.add(Parameter.PLATFORM, this.parameters.getPlatform().toString());
        payload.add(Parameter.APP_ID, this.parameters.getAppId());
        payload.add(Parameter.NAMESPACE, this.parameters.getNamespace());
        payload.add(Parameter.TRACKER_VERSION, this.parameters.getTrackerVersion());
    }
}
