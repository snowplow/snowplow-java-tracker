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

import com.google.common.base.Preconditions;
import com.snowplowanalytics.snowplow.tracker.payload.Payload;
import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

import java.util.ArrayList;
import java.util.HashMap;
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
    private String unstructSchema;
    private Subject subject;

    public Tracker(Emitter emitter, String namespace, String appId) {
        new Tracker(emitter, null, namespace, appId, true);
    }

    public Tracker(Emitter emitter, Subject subject, String namespace, String appId) {
        new Tracker(emitter, subject, namespace, appId, true);
    }

    public Tracker(Emitter emitter, String namespace, String appId, boolean base64Encoded) {
        new Tracker(emitter, null, namespace, appId, base64Encoded);
    }

    public Tracker(Emitter emitter, Subject subject, String namespace, String appId, boolean base64Encoded) {
        this.emitter = emitter;
        this.appId = appId;
        this.base64Encoded = base64Encoded;
        this.namespace = namespace;
        this.subject = subject;
        this.setSchema(Constants.DEFAULT_VENDOR, Constants.DEFAULT_SCHEMA_TAG,
                Constants.DEFAULT_SCHEMA_VERSION);
    }

    private Payload completePayload(Payload payload, Map context, double timestamp) {
        payload.add(Parameter.APPID, this.appId);
        payload.add(Parameter.NAMESPACE, this.namespace);
        payload.add(Parameter.TIMESTAMP, Util.getTimestamp());

        payload.add(Parameter.TRACKER_VERSION, Version.VERSION);

        // If timestamp is set to 0, generate one
        payload.add(Parameter.TIMESTAMP, (timestamp == 0 ? Util.getTimestamp() : timestamp));

        // Encodes context data
        if (context != null) {
            SchemaPayload envelope = new SchemaPayload();
            envelope.setSchema(contextSchema);
            envelope.setData(context);
            payload.addMap(context, this.base64Encoded, Parameter.CONTEXT_ENCODED,
                    Parameter.CONTEXT);
        }

        if (subject != null) {
            payload.addMap(subject.getSubject());
        }

        return payload;
    }

    private void addTrackerPayload(Payload payload) {
        this.emitter.addToBuffer(payload);
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public void setSchema(String vendor, String schemaTag, String version) {
        this.contextSchema = vendor + "/contexts/" + schemaTag + version;
        this.unstructSchema = vendor + "/unstruct_event/" + schemaTag + version;
        this.baseSchemaPath = vendor;
        this.schemaTag = schemaTag;
    }

    public void trackPageView(String pageUrl, String pageTitle, String referrer) {
        trackPageView(pageUrl, pageTitle, referrer, null, 0);
    }

    public void trackPageView(String pageUrl, String pageTitle, String referrer, Map context) {
        trackPageView(pageUrl,pageTitle, referrer, context, 0);
    }

    public void trackPageView(String pageUrl, String pageTitle, String referrer, double timestamp) {
        trackPageView(pageUrl, pageTitle, referrer, null, timestamp);
    }

    public void trackPageView(String pageUrl, String pageTitle, String referrer,
                              Map context, double timestamp) {
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

    public void trackStructuredEvent(String category, String action, String label, String property,
                                     int value) {
        trackStructuredEvent(category, action, label, property, value, null, 0);
    }

    public void trackStructuredEvent(String category, String action, String label, String property,
                                     int value, Map context) {
        trackStructuredEvent(category, action, label, property, value, context, 0);
    }

    public void trackStructuredEvent(String category, String action, String label, String property,
                                     int value, long timestamp) {
        trackStructuredEvent(category, action, label, property, value, null, timestamp);
    }

    public void trackStructuredEvent(String category, String action, String label, String property,
                                     int value, Map context, long timestamp) {
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
        payload.add(Parameter.SE_VALUE, value);

        completePayload(payload, context, timestamp);

        addTrackerPayload(payload);
    }

    public void trackUnstructuredEvent(Map<String, Object> eventData) {
        trackUnstructuredEvent(eventData, null, 0);
    }

    public void trackUnstructuredEvent(Map<String, Object> eventData, Map context) {
        trackUnstructuredEvent(eventData, context, 0);
    }

    public void trackUnstructuredEvent(Map<String, Object> eventData, long timestamp) {
        trackUnstructuredEvent(eventData, null, timestamp);
    }

    public void trackUnstructuredEvent(Map<String, Object> eventData, Map context, long timestamp) {
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

    protected void trackEcommerceTransactionItem(String order_id, String sku, Double price,
                                                 Integer quantity, String name, String category,
                                                 String currency, Map context, long timestamp) {
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
        payload.add(Parameter.TI_ITEM_PRICE, price);
        payload.add(Parameter.TI_ITEM_QUANTITY, quantity);
        payload.add(Parameter.TI_ITEM_CURRENCY, currency);

        completePayload(payload, context, timestamp);

        addTrackerPayload(payload);
    }

    public void trackEcommerceTransaction(String order_id, Double total_value, String affiliation,
                                          Double tax_value, Double shipping, String city,
                                          String state, String country, String currency,
                                          List<TransactionItem> items) {
        trackEcommerceTransaction(order_id, total_value, affiliation, tax_value, shipping, city,
                state, country, currency, items, null, 0);
    }

    public void trackEcommerceTransaction(String order_id, Double total_value, String affiliation,
                                          Double tax_value, Double shipping, String city,
                                          String state, String country, String currency,
                                          List<TransactionItem> items, Map context) {
        trackEcommerceTransaction(order_id, total_value, affiliation, tax_value, shipping, city,
                state, country, currency, items, context, 0);
    }

    public void trackEcommerceTransaction(String order_id, Double total_value, String affiliation,
                                          Double tax_value, Double shipping, String city,
                                          String state, String country, String currency,
                                          List<TransactionItem> items, long timestamp) {
        trackEcommerceTransaction(order_id, total_value, affiliation, tax_value, shipping, city,
                state, country, currency, items, null, timestamp);
    }

    public void trackEcommerceTransaction(String order_id, Double total_value, String affiliation,
                                          Double tax_value, Double shipping, String city,
                                          String state, String country, String currency,
                                          List<TransactionItem> items, Map context,long timestamp) {
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
        payload.add(Parameter.TR_TOTAL, total_value);
        payload.add(Parameter.TR_AFFILIATION, affiliation);
        payload.add(Parameter.TR_TAX, tax_value);
        payload.add(Parameter.TR_SHIPPING, shipping);
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
                    (Map) item.get(Parameter.CONTEXT),
                    timestamp);
        }
    }

    public void trackScreenView(String name, String id) {
        trackScreenView(name, id, null, 0);
    }

    public void trackScreenView(String name, String id, Map context) {
        trackScreenView(name, id, context, 0);
    }

    public void trackScreenView(String name, String id, long timestamp) {
        trackScreenView(name, id, null, timestamp);
    }

    public void trackScreenView(String name, String id, Map context, long timestamp) {
        // Precondition checks
        Preconditions.checkNotNull(id);
        Preconditions.checkArgument(!name.isEmpty(), "name cannot be empty");
        Preconditions.checkArgument(!id.isEmpty(), "id cannot be empty");

        Map<String, String> screenViewProperties = new HashMap<String, String>();

        screenViewProperties.put(Parameter.SV_NAME, name);
        screenViewProperties.put(Parameter.SV_ID, id);

        SchemaPayload payload = new SchemaPayload();

        payload.setSchema( this.baseSchemaPath + "/contexts/" + this.schemaTag + Version.VERSION);
        payload.setData(screenViewProperties);

        trackUnstructuredEvent(payload.getMap(), context, timestamp);
    }
}
