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

// Google
import com.google.common.base.Preconditions;

// This library
import com.snowplowanalytics.snowplow.tracker.constants.Constants;
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.events.TransactionItem;
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
     * @param emitter Emitter to which events will be sent
     * @param namespace Identifier for the Tracker instance
     * @param appId Application ID
     */
    public Tracker(Emitter emitter, String namespace, String appId) {
        this(emitter, null, namespace, appId);
    }

    /**
     * @param emitter Emitter to which events will be sent
     * @param subject Subject to be tracked
     * @param namespace Identifier for the Tracker instance
     * @param appId Application ID
     */
    public Tracker(Emitter emitter, Subject subject, String namespace, String appId) {
        this.emitter = emitter;
        this.appId = appId;
        this.namespace = namespace;
        this.subject = subject;
    }

    /**
     * Adds the event payload to an emitter for sending.
     *
     * @param payload the final event payload
     */
    private void addTrackerPayload(TrackerPayload payload) {
        this.emitter.emit(payload);
    }

    /**
     * Builds a finalised payload which is ready for sending.
     *
     * @param payload The raw event Payload
     * @param contexts Custom context for the event
     * @param timestamp Optional user-provided timestamp for the event
     * @return the completed Payload
     */
    private TrackerPayload completePayload(TrackerPayload payload, List<SelfDescribingJson> contexts, long timestamp) {

        // Add default parameters to the payload
        payload.add(Parameter.PLATFORM, platform.toString());
        payload.add(Parameter.APP_ID, this.appId);
        payload.add(Parameter.NAMESPACE, this.namespace);
        payload.add(Parameter.TRACKER_VERSION, this.trackerVersion);
        payload.add(Parameter.EID, Utils.getEventId());

        // If timestamp is set to 0, generate one
        payload.add(Parameter.TIMESTAMP, (timestamp == 0 ?
                String.valueOf(Utils.getTimestamp()) : Long.toString(timestamp)));

        // Build the final context and add it to the payload
        if (contexts != null && contexts.size() > 0) {
            SelfDescribingJson envelope = getFinalContext(contexts);
            payload.addMap(envelope.getMap(), this.base64Encoded, Parameter.CONTEXT_ENCODED, Parameter.CONTEXT);
        }

        // Add subject if available
        if (this.subject != null) {
            payload.addMap(new HashMap<String, Object>(subject.getSubject()));
        }

        return payload;
    }

    /**
     * Builds the final event context.
     *
     * @param contexts the base event context
     * @return the final event context json with
     *         many contexts inside
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
     * @param pageUrl URL of the viewed page
     * @param pageTitle Title of the viewed page
     * @param referrer Referrer of the page
     */
    public void trackPageView(String pageUrl, String pageTitle, String referrer) {
        trackPageView(pageUrl, pageTitle, referrer, null, 0);
    }

    /**
     * @param pageUrl URL of the viewed page
     * @param pageTitle Title of the viewed page
     * @param referrer Referrer of the page
     * @param context Custom context for the event
     */
    public void trackPageView(String pageUrl, String pageTitle, String referrer, List<SelfDescribingJson> context) {
        trackPageView(pageUrl,pageTitle, referrer, context, 0);
    }

    /**
     * @param pageUrl URL of the viewed page
     * @param pageTitle Title of the viewed page
     * @param referrer Referrer of the page
     * @param timestamp Optional user-provided timestamp for the event
     */
    public void trackPageView(String pageUrl, String pageTitle, String referrer, long timestamp) {
        trackPageView(pageUrl, pageTitle, referrer, null, timestamp);
    }

    /**
     * @param pageUrl URL of the viewed page
     * @param pageTitle Title of the viewed page
     * @param referrer Referrer of the page
     * @param context Custom context for the event
     * @param timestamp Optional user-provided timestamp for the event
     */
    public void trackPageView(String pageUrl, String pageTitle, String referrer, List<SelfDescribingJson> context,
                              long timestamp) {
        // Precondition checks
        Preconditions.checkNotNull(pageUrl);
        Preconditions.checkArgument(!pageUrl.isEmpty(), "pageUrl cannot be empty");

        TrackerPayload payload = new TrackerPayload();
        payload.add(Parameter.EVENT, Constants.EVENT_PAGE_VIEW);
        payload.add(Parameter.PAGE_URL, pageUrl);
        payload.add(Parameter.PAGE_TITLE, pageTitle);
        payload.add(Parameter.PAGE_REFR, referrer);

        completePayload(payload, context, timestamp);

        addTrackerPayload(payload);
    }

    /**
     * @param category Category of the event
     * @param action The event itself
     * @param label Refer to the object the action is performed on
     * @param property Property associated with either the action or the object
     * @param value A value associated with the user action
     */
    public void trackStructuredEvent(String category, String action, String label, String property, int value) {
        trackStructuredEvent(category, action, label, property, value, null, 0);
    }

    /**
     * @param category Category of the event
     * @param action The event itself
     * @param label Refer to the object the action is performed on
     * @param property Property associated with either the action or the object
     * @param value A value associated with the user action
     * @param context Custom context for the event
     */
    public void trackStructuredEvent(String category, String action, String label, String property, int value,
                                     List<SelfDescribingJson> context) {
        trackStructuredEvent(category, action, label, property, value, context, 0);
    }

    /**
     * @param category Category of the event
     * @param action The event itself
     * @param label Refer to the object the action is performed on
     * @param property Property associated with either the action or the object
     * @param value A value associated with the user action
     * @param timestamp Optional user-provided timestamp for the event
     */
    public void trackStructuredEvent(String category, String action, String label, String property, int value,
                                     long timestamp) {
        trackStructuredEvent(category, action, label, property, value, null, timestamp);
    }

    /**
     * @param category Category of the event
     * @param action The event itself
     * @param label Refer to the object the action is performed on
     * @param property Property associated with either the action or the object
     * @param value A value associated with the user action
     * @param context Custom context for the event
     * @param timestamp Optional user-provided timestamp for the event
     */
    public void trackStructuredEvent(String category, String action, String label, String property, int value,
                                     List<SelfDescribingJson> context, long timestamp) {
        // Precondition checks
        Preconditions.checkNotNull(category);
        Preconditions.checkNotNull(action);
        Preconditions.checkArgument(!category.isEmpty(), "category cannot be empty");
        Preconditions.checkArgument(!action.isEmpty(), "action cannot be empty");

        TrackerPayload payload = new TrackerPayload();
        payload.add(Parameter.EVENT, Constants.EVENT_STRUCTURED);
        payload.add(Parameter.SE_CATEGORY, category);
        payload.add(Parameter.SE_ACTION, action);
        payload.add(Parameter.SE_LABEL, label);
        payload.add(Parameter.SE_PROPERTY, property);
        payload.add(Parameter.SE_VALUE, Double.toString(value));

        completePayload(payload, context, timestamp);

        addTrackerPayload(payload);
    }

    /**
     * @param eventData The properties of the event. Has two field:
     *                  A "data" field containing the event properties and
     *                  A "schema" field identifying the schema against which the data is validated
     */
    public void trackUnstructuredEvent(SelfDescribingJson eventData) {
        trackUnstructuredEvent(eventData, null, 0);
    }

    /**
     * @param eventData The properties of the event. Has two field:
     *                  A "data" field containing the event properties and
     *                  A "schema" field identifying the schema against which the data is validated
     * @param context Custom context for the event
     */
    public void trackUnstructuredEvent(SelfDescribingJson eventData, List<SelfDescribingJson> context) {
        trackUnstructuredEvent(eventData, context, 0);
    }

    /**
     * @param eventData The properties of the event. Has two field:
     *                  A "data" field containing the event properties and
     *                  A "schema" field identifying the schema against which the data is validated
     * @param timestamp Optional user-provided timestamp for the event
     */
    public void trackUnstructuredEvent(SelfDescribingJson eventData, long timestamp) {
        trackUnstructuredEvent(eventData, null, timestamp);
    }

    /**
     *
     * @param eventData The properties of the event. Has two field:
     *                   A "data" field containing the event properties and
     *                   A "schema" field identifying the schema against which the data is validated
     * @param context Custom context for the event
     * @param timestamp Optional user-provided timestamp for the event
     */
    public void trackUnstructuredEvent(SelfDescribingJson eventData, List<SelfDescribingJson> context, long timestamp) {
        // Precondition checks
        Preconditions.checkNotNull(eventData);

        TrackerPayload payload = new TrackerPayload();
        SelfDescribingJson envelope = new SelfDescribingJson(
                Constants.SCHEMA_UNSTRUCT_EVENT,
                eventData
        );
        payload.add(Parameter.EVENT, Constants.EVENT_UNSTRUCTURED);
        payload.addMap(envelope.getMap(), base64Encoded,
                Parameter.UNSTRUCTURED_ENCODED, Parameter.UNSTRUCTURED);

        completePayload(payload, context, timestamp);

        addTrackerPayload(payload);
    }

    /**
     * This is an internal method called by trackEcommerceTransaction
     *
     * @param itemId Order ID
     * @param sku Item SKU
     * @param price Item price
     * @param quantity Item quantity
     * @param name Item name
     * @param category Item category
     * @param currency The currency the price is expressed in
     * @param context Custom context for the event
     * @param timestamp Optional user-provided timestamp for the event
     */
    private void trackEcommerceTransactionItem(String itemId, String sku, Double price, Integer quantity, String name,
                                               String category, String currency, List<SelfDescribingJson> context,
                                               long timestamp) {
        // Precondition checks
        Preconditions.checkNotNull(itemId);
        Preconditions.checkNotNull(sku);
        Preconditions.checkNotNull(price);
        Preconditions.checkNotNull(quantity);
        Preconditions.checkArgument(!itemId.isEmpty(), "itemId cannot be empty");
        Preconditions.checkArgument(!sku.isEmpty(), "sku cannot be empty");

        TrackerPayload payload = new TrackerPayload();
        payload.add(Parameter.EVENT, Constants.EVENT_ECOMM_ITEM);
        payload.add(Parameter.TI_ITEM_ID, itemId);
        payload.add(Parameter.TI_ITEM_SKU, sku);
        payload.add(Parameter.TI_ITEM_NAME, name);
        payload.add(Parameter.TI_ITEM_CATEGORY, category);
        payload.add(Parameter.TI_ITEM_PRICE, Double.toString(price));
        payload.add(Parameter.TI_ITEM_QUANTITY, Double.toString(quantity));
        payload.add(Parameter.TI_ITEM_CURRENCY, currency);

        completePayload(payload, context, timestamp);

        addTrackerPayload(payload);
    }

    /**
     * @param orderId ID of the eCommerce transaction
     * @param totalValue Total transaction value
     * @param affiliation Transaction affiliation
     * @param taxValue Transaction tax value
     * @param shipping Delivery cost charged
     * @param city Delivery address city
     * @param state Delivery address state
     * @param country Delivery address country
     * @param currency The currency the price is expressed in
     * @param items The items in the transaction
     */
    public void trackEcommerceTransaction(String orderId, Double totalValue, String affiliation, Double taxValue,
                                          Double shipping, String city, String state, String country, String currency,
                                          List<TransactionItem> items) {
        trackEcommerceTransaction(orderId, totalValue, affiliation, taxValue, shipping, city, state, country, currency,
                items, null, 0);
    }

    /**
     * @param orderId ID of the eCommerce transaction
     * @param totalValue Total transaction value
     * @param affiliation Transaction affiliation
     * @param taxValue Transaction tax value
     * @param shipping Delivery cost charged
     * @param city Delivery address city
     * @param state Delivery address state
     * @param country Delivery address country
     * @param currency The currency the price is expressed in
     * @param items The items in the transaction
     * @param context Custom context for the event
     */
    public void trackEcommerceTransaction(String orderId, Double totalValue, String affiliation, Double taxValue,
                                          Double shipping, String city, String state, String country, String currency,
                                          List<TransactionItem> items, List<SelfDescribingJson> context) {
        trackEcommerceTransaction(orderId, totalValue, affiliation, taxValue, shipping, city, state, country, currency,
                items, context, 0);
    }

    /**
     * @param orderId ID of the eCommerce transaction
     * @param totalValue Total transaction value
     * @param affiliation Transaction affiliation
     * @param taxValue Transaction tax value
     * @param shipping Delivery cost charged
     * @param city Delivery address city
     * @param state Delivery address state
     * @param country Delivery address country
     * @param currency The currency the price is expressed in
     * @param items The items in the transaction
     * @param timestamp Optional user-provided timestamp for the event
     */
    public void trackEcommerceTransaction(String orderId, Double totalValue, String affiliation,
                                          Double taxValue, Double shipping, String city,
                                          String state, String country, String currency,
                                          List<TransactionItem> items, long timestamp) {
        trackEcommerceTransaction(orderId, totalValue, affiliation, taxValue, shipping, city, state, country, currency,
                items, null, timestamp);
    }

    /**
     * @param orderId ID of the eCommerce transaction
     * @param totalValue Total transaction value
     * @param affiliation Transaction affiliation
     * @param taxValue Transaction tax value
     * @param shipping Delivery cost charged
     * @param city Delivery address city
     * @param state Delivery address state
     * @param country Delivery address country
     * @param currency The currency the price is expressed in
     * @param items The items in the transaction
     * @param context Custom context for the event
     * @param timestamp Optional user-provided timestamp for the event
     */
    @SuppressWarnings("unchecked")
    public void trackEcommerceTransaction(String orderId, Double totalValue, String affiliation, Double taxValue,
                                          Double shipping, String city, String state, String country, String currency,
                                          List<TransactionItem> items, List<SelfDescribingJson> context,
                                          long timestamp) {
        // Precondition checks
        Preconditions.checkNotNull(orderId);
        Preconditions.checkNotNull(totalValue);
        Preconditions.checkNotNull(items);
        Preconditions.checkArgument(!orderId.isEmpty(), "orderId cannot be empty");

        TrackerPayload payload = new TrackerPayload();
        payload.add(Parameter.EVENT, Constants.EVENT_ECOMM);
        payload.add(Parameter.TR_ID, orderId);
        payload.add(Parameter.TR_TOTAL, Double.toString(totalValue));
        payload.add(Parameter.TR_AFFILIATION, affiliation);
        payload.add(Parameter.TR_TAX, Double.toString(taxValue));
        payload.add(Parameter.TR_SHIPPING, Double.toString(shipping));
        payload.add(Parameter.TR_CITY, city);
        payload.add(Parameter.TR_STATE, state);
        payload.add(Parameter.TR_COUNTRY, country);
        payload.add(Parameter.TR_CURRENCY, currency);

        completePayload(payload, context, timestamp);

        for (TransactionItem item : items) {
            trackEcommerceTransactionItem(
                    (String) item.get(Parameter.TI_ITEM_ID),
                    (String) item.get(Parameter.TI_ITEM_SKU),
                    (Double) item.get(Parameter.TI_ITEM_PRICE),
                    (Integer) item.get(Parameter.TI_ITEM_QUANTITY),
                    (String) item.get(Parameter.TI_ITEM_NAME),
                    (String) item.get(Parameter.TI_ITEM_CATEGORY),
                    (String) item.get(Parameter.TI_ITEM_CURRENCY),
                    (List<SelfDescribingJson>) item.get(Parameter.CONTEXT),
                    timestamp
            );
        }

        addTrackerPayload(payload);
    }

    /**
     * @param name The name of the screen view event
     * @param id Screen view ID
     */
    public void trackScreenView(String name, String id) {
        trackScreenView(name, id, null, 0);
    }

    /**
     * @param name The name of the screen view event
     * @param id Screen view ID
     * @param context Custom context for the event
     */
    public void trackScreenView(String name, String id, List<SelfDescribingJson> context) {
        trackScreenView(name, id, context, 0);
    }

    /**
     * @param name The name of the screen view event
     * @param id Screen view ID
     * @param timestamp Optional user-provided timestamp for the event
     */
    public void trackScreenView(String name, String id, long timestamp) {
        trackScreenView(name, id, null, timestamp);
    }

    /**
     * @param name The name of the screen view event
     * @param id Screen view ID
     * @param context Custom context for the event
     * @param timestamp Optional user-provided timestamp for the event
     */
    public void trackScreenView(String name, String id, List<SelfDescribingJson> context,
                                long timestamp) {
        // Precondition checks
        Preconditions.checkArgument(name != null || id != null);

        TrackerPayload data = new TrackerPayload();
        data.add(Parameter.SV_NAME, name);
        data.add(Parameter.SV_ID, id);
        SelfDescribingJson payload = new SelfDescribingJson(Constants.SCHEMA_SCREEN_VIEW, data);

        trackUnstructuredEvent(payload, context, timestamp);
    }
}
