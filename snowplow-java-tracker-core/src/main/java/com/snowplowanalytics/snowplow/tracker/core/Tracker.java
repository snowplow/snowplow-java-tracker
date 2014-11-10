/*
 * Copyright (c) 2014 Snowplow Analytics Ltd. All rights reserved.
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

package com.snowplowanalytics.snowplow.tracker.core;

import com.google.common.base.Preconditions;
import com.snowplowanalytics.snowplow.tracker.core.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.core.payload.Payload;
import com.snowplowanalytics.snowplow.tracker.core.payload.SchemaPayload;
import com.snowplowanalytics.snowplow.tracker.core.payload.TrackerPayload;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Tracker {

    private boolean base64Encoded = true;
    private Emitter emitter;
    private String appId;
    private String namespace;
    private String contextSchema;
    private String baseSchemaPath;
    private String schemaTag;
    private String schemaVersion;
    private String trackerVersion;
    private String unstructSchema;
    private Subject subject;

    /**
     * @param emitter Emitter to which events will be sent
     * @param namespace Identifier for the Tracker instance
     * @param appId Application ID
     */
    public Tracker(Emitter emitter, String namespace, String appId) {
        this(emitter, null, namespace, appId, true);
    }

    /**
     * @param emitter Emitter to which events will be sent
     * @param subject Subject to be tracked
     * @param namespace Identifier for the Tracker instance
     * @param appId Application ID
     */
    public Tracker(Emitter emitter, Subject subject, String namespace, String appId) {
        this(emitter, subject, namespace, appId, true);
    }

    /**
     * @param emitter Emitter to which events will be sent
     * @param namespace Identifier for the Tracker instance
     * @param appId Application ID
     * @param base64Encoded Whether JSONs in the payload should be base-64 encoded
     */
    public Tracker(Emitter emitter, String namespace, String appId, boolean base64Encoded) {
        this(emitter, null, namespace, appId, base64Encoded);
    }

    /**
     * @param emitter Emitter to which events will be sent
     * @param subject Subject to be tracked
     * @param namespace Identifier for the Tracker instance
     * @param appId Application ID
     * @param base64Encoded Whether JSONs in the payload should be base-64 encoded
     */
    public Tracker(Emitter emitter, Subject subject, String namespace, String appId,
                   boolean base64Encoded) {
        this.emitter = emitter;
        this.appId = appId;
        this.base64Encoded = base64Encoded;
        this.namespace = namespace;
        this.subject = subject;
        this.trackerVersion = Version.TRACKER;
        this.setSchema(Constants.DEFAULT_IGLU_VENDOR, Constants.DEFAULT_SCHEMA_TAG,
                Constants.DEFAULT_SCHEMA_VERSION);
    }

    /**
     * @param payload Payload builder
     * @param context Custom context for the event
     * @param timestamp Optional user-provided timestamp for the event
     * @return A completed Payload
     */
    protected Payload completePayload(Payload payload, List<SchemaPayload> context,
                                      long timestamp) {
        payload.add(Parameter.APPID, this.appId);
        payload.add(Parameter.NAMESPACE, this.namespace);
        payload.add(Parameter.TRACKER_VERSION, this.trackerVersion);

        // If timestamp is set to 0, generate one
        payload.add(Parameter.TIMESTAMP,
                (timestamp == 0 ? Util.getTimestamp() : Long.toString(timestamp)));

        // Encodes context data
        if (context != null) {
            SchemaPayload envelope = new SchemaPayload();
            envelope.setSchema(contextSchema);

            // We can do better here, rather than re-iterate through the list
            List<Map> contextDataList = new LinkedList<Map>();
            for (SchemaPayload schemaPayload : context) {
                contextDataList.add(schemaPayload.getMap());
            }

            envelope.setData(contextDataList);
            payload.addMap(envelope.getMap(), this.base64Encoded, Parameter.CONTEXT_ENCODED,
                    Parameter.CONTEXT);
        }

        if (this.subject != null) payload.addMap(new HashMap<String, Object>(subject.getSubject()));

        return payload;
    }

    protected void setTrackerVersion(String version) {
        this.trackerVersion = version;
    }

    private void addTrackerPayload(Payload payload) {
        this.emitter.addToBuffer(payload);
    }

    private void setSubject(Subject subject) {
        this.subject = subject;
    }

    /**
     * Sets the JSON schema to be used mainly for self-describing JSON.
     * @param vendor Schema vendor
     * @param schemaTag Schema tag type
     * @param version Schema version tag
     */
    public void setSchema(String vendor, String schemaTag, String version) {
        this.contextSchema = vendor + "/contexts/" + schemaTag + "/" + version;
        this.unstructSchema = vendor + "/unstruct_event/" + schemaTag + "/" + version;
        this.baseSchemaPath = vendor;
        this.schemaTag = schemaTag;
        this.schemaVersion = version;
    }

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
    public void trackPageView(String pageUrl, String pageTitle, String referrer,
                              List<SchemaPayload> context) {
        trackPageView(pageUrl,pageTitle, referrer, context, 0);
    }

    /**
     * @param pageUrl URL of the viewed page
     * @param pageTitle Title of the viewed page
     * @param referrer Referrer of the page
     * @param timestamp Optional user-provided timestamp for the event
     */
    public void trackPageView(String pageUrl, String pageTitle, String referrer,
                              long timestamp) {
        trackPageView(pageUrl, pageTitle, referrer, null, timestamp);
    }

    /**
     * @param pageUrl URL of the viewed page
     * @param pageTitle Title of the viewed page
     * @param referrer Referrer of the page
     * @param context Custom context for the event
     * @param timestamp Optional user-provided timestamp for the event
     */
    public void trackPageView(String pageUrl, String pageTitle, String referrer,
                              List<SchemaPayload> context, long timestamp) {
        // Precondition checks
        Preconditions.checkNotNull(pageUrl);
        Preconditions.checkArgument(!pageUrl.isEmpty(), "pageUrl cannot be empty");
        Preconditions.checkArgument(!pageTitle.isEmpty(), "pageTitle cannot be empty");
        Preconditions.checkArgument(!referrer.isEmpty(), "referrer cannot be empty");

        Payload payload = new TrackerPayload();
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
    public void trackStructuredEvent(String category, String action, String label, String property,
                                     int value) {
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
    public void trackStructuredEvent(String category, String action, String label, String property,
                                     int value, List<SchemaPayload> context) {
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
    public void trackStructuredEvent(String category, String action, String label, String property,
                                     int value, long timestamp) {
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
    public void trackStructuredEvent(String category, String action, String label, String property,
                                     int value, List<SchemaPayload> context, long timestamp) {
        // Precondition checks
        Preconditions.checkNotNull(label);
        Preconditions.checkNotNull(property);
        Preconditions.checkArgument(!label.isEmpty(), "label cannot be empty");
        Preconditions.checkArgument(!property.isEmpty(), "property cannot be empty");
        Preconditions.checkArgument(!category.isEmpty(), "property cannot be empty");
        Preconditions.checkArgument(!action.isEmpty(), "property cannot be empty");

        Payload payload = new TrackerPayload();
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
     *
     * @param eventData The properties of the event. Has two field:
                        A "data" field containing the event properties and
                        A "schema" field identifying the schema against which the data is validated
     */
    public void trackUnstructuredEvent(Map<String, Object> eventData) {
        trackUnstructuredEvent(eventData, null, 0);
    }

    /**
     *
     * @param eventData The properties of the event. Has two field:
     *                   A "data" field containing the event properties and
     *                   A "schema" field identifying the schema against which the data is validated
     * @param context Custom context for the event
     */
    public void trackUnstructuredEvent(Map<String, Object> eventData, List<SchemaPayload> context) {
        trackUnstructuredEvent(eventData, context, 0);
    }

    /**
     *
     * @param eventData The properties of the event. Has two field:
     *                   A "data" field containing the event properties and
     *                   A "schema" field identifying the schema against which the data is validated
     * @param timestamp Optional user-provided timestamp for the event
     */
    public void trackUnstructuredEvent(Map<String, Object> eventData, long timestamp) {
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
    public void trackUnstructuredEvent(Map<String, Object> eventData, List<SchemaPayload> context,
                                       long timestamp) {
        Payload payload = new TrackerPayload();
        SchemaPayload envelope = new SchemaPayload();

        envelope.setSchema(unstructSchema);
        envelope.setData(eventData);

        payload.add(Parameter.EVENT, Constants.EVENT_UNSTRUCTURED);
        payload.addMap(envelope.getMap(), base64Encoded,
                Parameter.UNSTRUCTURED_ENCODED, Parameter.UNSTRUCTURED);

        completePayload(payload, context, timestamp);

        addTrackerPayload(payload);
    }

    /**
     * This is an internal method called by track_ecommerce_transaction. It is not for public use.
     * @param order_id Order ID
     * @param sku Item SKU
     * @param price Item price
     * @param quantity Item quantity
     * @param name Item name
     * @param category Item category
     * @param currency The currency the price is expressed in
     * @param context Custom context for the event
     * @param timestamp Optional user-provided timestamp for the event
     */
    protected void trackEcommerceTransactionItem(String order_id, String sku, Double price,
                                                 Integer quantity, String name, String category,
                                                 String currency, List<SchemaPayload> context,
                                                 long timestamp) {
        // Precondition checks
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(category);
        Preconditions.checkNotNull(currency);
        Preconditions.checkArgument(!order_id.isEmpty(), "order_id cannot be empty");
        Preconditions.checkArgument(!sku.isEmpty(), "sku cannot be empty");
        Preconditions.checkArgument(!name.isEmpty(), "name cannot be empty");
        Preconditions.checkArgument(!category.isEmpty(), "category cannot be empty");
        Preconditions.checkArgument(!currency.isEmpty(), "currency cannot be empty");

        Payload payload = new TrackerPayload();
        payload.add(Parameter.EVENT, Constants.EVENT_ECOMM_ITEM);
        payload.add(Parameter.TI_ITEM_ID, order_id);
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
     * @param order_id ID of the eCommerce transaction
     * @param total_value Total transaction value
     * @param affiliation Transaction affiliation
     * @param tax_value Transaction tax value
     * @param shipping Delivery cost charged
     * @param city Delivery address city
     * @param state Delivery address state
     * @param country Delivery address country
     * @param currency The currency the price is expressed in
     * @param items The items in the transaction
     */
    public void trackEcommerceTransaction(String order_id, Double total_value, String affiliation,
                                          Double tax_value, Double shipping, String city,
                                          String state, String country, String currency,
                                          List<TransactionItem> items) {
        trackEcommerceTransaction(order_id, total_value, affiliation, tax_value, shipping, city,
                state, country, currency, items, null, 0);
    }

    /**
     * @param order_id ID of the eCommerce transaction
     * @param total_value Total transaction value
     * @param affiliation Transaction affiliation
     * @param tax_value Transaction tax value
     * @param shipping Delivery cost charged
     * @param city Delivery address city
     * @param state Delivery address state
     * @param country Delivery address country
     * @param currency The currency the price is expressed in
     * @param items The items in the transaction
     * @param context Custom context for the event
     */
    public void trackEcommerceTransaction(String order_id, Double total_value, String affiliation,
                                          Double tax_value, Double shipping, String city,
                                          String state, String country, String currency,
                                          List<TransactionItem> items, List<SchemaPayload> context) {
        trackEcommerceTransaction(order_id, total_value, affiliation, tax_value, shipping, city,
                state, country, currency, items, context, 0);
    }

    /**
     * @param order_id ID of the eCommerce transaction
     * @param total_value Total transaction value
     * @param affiliation Transaction affiliation
     * @param tax_value Transaction tax value
     * @param shipping Delivery cost charged
     * @param city Delivery address city
     * @param state Delivery address state
     * @param country Delivery address country
     * @param currency The currency the price is expressed in
     * @param items The items in the transaction
     * @param timestamp Optional user-provided timestamp for the event
     */
    public void trackEcommerceTransaction(String order_id, Double total_value, String affiliation,
                                          Double tax_value, Double shipping, String city,
                                          String state, String country, String currency,
                                          List<TransactionItem> items, long timestamp) {
        trackEcommerceTransaction(order_id, total_value, affiliation, tax_value, shipping, city,
                state, country, currency, items, null, timestamp);
    }

    /**
     * @param order_id ID of the eCommerce transaction
     * @param total_value Total transaction value
     * @param affiliation Transaction affiliation
     * @param tax_value Transaction tax value
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
    public void trackEcommerceTransaction(String order_id, Double total_value, String affiliation,
                                          Double tax_value, Double shipping, String city,
                                          String state, String country, String currency,
                                          List<TransactionItem> items, List<SchemaPayload> context,
                                          long timestamp) {
        // Precondition checks
        Preconditions.checkNotNull(affiliation);
        Preconditions.checkNotNull(city);
        Preconditions.checkNotNull(state);
        Preconditions.checkNotNull(country);
        Preconditions.checkNotNull(currency);
        Preconditions.checkArgument(!order_id.isEmpty(), "order_id cannot be empty");
        Preconditions.checkArgument(!affiliation.isEmpty(), "affiliation cannot be empty");
        Preconditions.checkArgument(!city.isEmpty(), "city cannot be empty");
        Preconditions.checkArgument(!state.isEmpty(), "state cannot be empty");
        Preconditions.checkArgument(!country.isEmpty(), "country cannot be empty");
        Preconditions.checkArgument(!currency.isEmpty(), "currency cannot be empty");

        Payload payload = new TrackerPayload();
        payload.add(Parameter.EVENT, Constants.EVENT_ECOMM);
        payload.add(Parameter.TR_ID, order_id);
        payload.add(Parameter.TR_TOTAL, Double.toString(total_value));
        payload.add(Parameter.TR_AFFILIATION, affiliation);
        payload.add(Parameter.TR_TAX, Double.toString(tax_value));
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
                    (List<SchemaPayload>) item.get(Parameter.CONTEXT),
                    timestamp);
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
    public void trackScreenView(String name, String id, List<SchemaPayload> context) {
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
    public void trackScreenView(String name, String id, List<SchemaPayload> context,
                                long timestamp) {
        Preconditions.checkArgument(name != null || id != null);
        TrackerPayload trackerPayload = new TrackerPayload();

        trackerPayload.add(Parameter.SV_NAME, name);
        trackerPayload.add(Parameter.SV_ID, id);

        SchemaPayload payload = new SchemaPayload();

        payload.setSchema( this.baseSchemaPath + "/contexts/" +
                this.schemaTag + "/" + this.schemaVersion);
        payload.setData(trackerPayload);

        trackUnstructuredEvent(payload.getMap(), context, timestamp);
    }
}
