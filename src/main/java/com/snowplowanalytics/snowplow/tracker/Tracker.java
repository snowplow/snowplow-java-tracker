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

package com.snowplowanalytics.snowplow.tracker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Tracker {

    private Emitter emitter;
    private DevicePlatform platform;
    private String trackerVersion;
    private Subject subject;
    private String appId;
    private String namespace;
    private ObjectMapper objectMapper;
    private Provider provider;

    public final static BaseEncoding BASE_64 =  BaseEncoding.base64();
    
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
        this(emitter, subject, namespace, appId, new ObjectMapper());
    }

    /**
     * @param emitter Emitter to which events will be sent
     * @param subject Subject to be tracked
     * @param namespace Identifier for the Tracker instance
     * @param appId Application ID
     */
    public Tracker(Emitter emitter, Subject subject, String namespace, String appId, ObjectMapper objectMapper) {
        this.emitter = emitter;
        this.appId = appId;
        this.namespace = namespace;
        this.subject = subject;
        this.trackerVersion = Version.TRACKER;
        this.platform = DevicePlatform.Desktop;
        this.objectMapper = objectMapper;
        this.provider = new Provider();
    }


    /**
     * @param data Payload builder
     * @param context Custom context for the event
     * @param timestamp Optional user-provided timestamp for the event
     * @return A completed Payload
     */
   
    protected Map<String, Object> completePayload(Map<String, Object> data, List<SchemaPayload> context,
                                      long timestamp) {
        
        data.put(Parameter.PLATFORM, platform.toString());
        data.put(Parameter.APPID, appId);
        data.put(Parameter.NAMESPACE, namespace);
        data.put(Parameter.TRACKER_VERSION, trackerVersion);
        data.put(Parameter.EID, provider.getUUID().toString());

        // If timestamp is set to 0, generate one
        data.put(Parameter.TIMESTAMP,
                (timestamp == 0 ? String.valueOf(provider.getTimestamp()) : Long.toString(timestamp)));

        // Encodes context data
        if (context != null) {
            SchemaPayload envelope = new SchemaPayload();
            envelope.setSchema(Constants.SCHEMA_CONTEXTS);

            // We can do better here, rather than re-iterate through the list
            List<Map> contextDataList = new LinkedList<Map>();
            for (SchemaPayload schemaPayload : context) {
                contextDataList.add(schemaPayload.getMap());
            }

            envelope.setData(contextDataList);

            data.put(Parameter.CONTEXT_ENCODED, base64Json(envelope));
        }

        if (this.subject != null) data.putAll(subject.getSubject());

        return data;
    }

    private String base64Json(SchemaPayload schemaPayload) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(schemaPayload.getMap());
            return BASE_64.encode(bytes);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void setPlatform(DevicePlatform platform) {
        this.platform = platform;
    }

    public DevicePlatform getPlatform() {
        return this.platform;
    }

    protected void setTrackerVersion(String version) {
        this.trackerVersion = version;
    }

    private void addTrackerPayload(Map<String, Object> payload) {
        this.emitter.addToBuffer(payload);
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Subject getSubject() {
        return this.subject;
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

        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put(Parameter.EVENT, Constants.EVENT_PAGE_VIEW);
        payload.put(Parameter.PAGE_URL, pageUrl);
        payload.put(Parameter.PAGE_TITLE, pageTitle);
        payload.put(Parameter.PAGE_REFR, referrer);

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
        Preconditions.checkArgument(!category.isEmpty(), "category cannot be empty");
        Preconditions.checkArgument(!action.isEmpty(), "action cannot be empty");

        HashMap<String, Object> payload = new HashMap<String, Object>();
        payload.put(Parameter.EVENT, Constants.EVENT_STRUCTURED);
        payload.put(Parameter.SE_CATEGORY, category);
        payload.put(Parameter.SE_ACTION, action);
        payload.put(Parameter.SE_LABEL, label);
        payload.put(Parameter.SE_PROPERTY, property);
        payload.put(Parameter.SE_VALUE, Double.toString(value));

        completePayload(payload, context, timestamp);

        addTrackerPayload(payload);
    }

    /**
     *
     * @param eventData The properties of the event. Has two field:
     *                   A "data" field containing the event properties and
     *                  A "schema" field identifying the schema against which the data is validated
     */
    public void trackUnstructuredEvent(SchemaPayload eventData) {
        trackUnstructuredEvent(eventData, null, 0);
    }

    /**
     *
     * @param eventData The properties of the event. Has two field:
     *                   A "data" field containing the event properties and
     *                   A "schema" field identifying the schema against which the data is validated
     * @param context Custom context for the event
     */
    public void trackUnstructuredEvent(SchemaPayload eventData, List<SchemaPayload> context) {
        trackUnstructuredEvent(eventData, context, 0);
    }

    /**
     *
     * @param eventData The properties of the event. Has two field:
     *                   A "data" field containing the event properties and
     *                   A "schema" field identifying the schema against which the data is validated
     * @param timestamp Optional user-provided timestamp for the event
     */
    public void trackUnstructuredEvent(SchemaPayload eventData, long timestamp) {
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
    public void trackUnstructuredEvent(SchemaPayload eventData, List<SchemaPayload> context,
                                       long timestamp) {
        Map<String, Object> payload = new HashMap<String, Object>();
        SchemaPayload envelope = new SchemaPayload();
        envelope.setSchema(Constants.SCHEMA_UNSTRUCT_EVENT);
        envelope.setData(eventData.getMap());
        payload.put(Parameter.EVENT, Constants.EVENT_UNSTRUCTURED);
        payload.put(Parameter.UNSTRUCTURED_ENCODED, base64Json(envelope));
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

        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put(Parameter.EVENT, Constants.EVENT_ECOMM_ITEM);
        payload.put(Parameter.TI_ITEM_ID, order_id);
        payload.put(Parameter.TI_ITEM_SKU, sku);
        payload.put(Parameter.TI_ITEM_NAME, name);
        payload.put(Parameter.TI_ITEM_CATEGORY, category);
        payload.put(Parameter.TI_ITEM_PRICE, Double.toString(price));
        payload.put(Parameter.TI_ITEM_QUANTITY, Double.toString(quantity));
        payload.put(Parameter.TI_ITEM_CURRENCY, currency);

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


        HashMap<String, Object> data = new HashMap<String, Object>();

        data.put(Parameter.EVENT, Constants.EVENT_ECOMM);
        data.put(Parameter.TR_ID, order_id);
        data.put(Parameter.TR_TOTAL, Double.toString(total_value));
        data.put(Parameter.TR_AFFILIATION, affiliation);
        data.put(Parameter.TR_TAX, Double.toString(tax_value));
        data.put(Parameter.TR_SHIPPING, Double.toString(shipping));
        data.put(Parameter.TR_CITY, city);
        data.put(Parameter.TR_STATE, state);
        data.put(Parameter.TR_COUNTRY, country);
        data.put(Parameter.TR_CURRENCY, currency);

        completePayload(data, context, timestamp);

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

        addTrackerPayload(data);
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
        Map<String, Object> data = new HashMap<String, Object>();

        data.put(Parameter.SV_NAME, name);
        data.put(Parameter.SV_ID, id);

        SchemaPayload payload = new SchemaPayload();
        payload.setSchema(Constants.SCHEMA_SCREEN_VIEW);
        payload.setData(data);

        trackUnstructuredEvent(payload, context, timestamp);
    }


    /**
     * Visible for testing
     */
    public void setProvider(Provider provider) {
        this.provider = provider;
    }
}
