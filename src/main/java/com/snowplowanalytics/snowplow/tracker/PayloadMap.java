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

// Java
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Set;

// JSON
import com.fasterxml.jackson.databind.JsonNode;

/**
 * PayloadMap interface
 *  The PayloadMap is used to store all the parameters and configurations that are used
 *  to send data via the HTTP GET request.
 * PayloadMaps have an immutable structure for secure and accurate transfers of information.
 * @version 0.2.0
 * @author Kevin Gleason
 */
// Immutable structure -- Payload will always be a string.
public interface PayloadMap {
    /**
     * Add a basic parameter.
     * @param key The parameter key
     * @param val The parameter value
     * @return Returns a new PayloadMap with the key-value-pair
     */
    public PayloadMap add(String key, String val);

    /**
     * Add an unstructured source
     * @param dictInfo Information is parsed elsewhere from string to JSON then passed here
     * @param encode_base64 Whether or not to encode before transferring to web. Default true.
     * @return Returns a new PayloadMap with the key-value-pair
     * @throws UnsupportedEncodingException
     */
    public PayloadMap addUnstructured(JsonNode dictInfo, boolean encode_base64)
            throws UnsupportedEncodingException;

    /**
     * Add a JSON Object to the Payload
     * @param jsonObject JSON object to be added
     * @param encode_base64 Whether or not to encode before transferring to web. Default true.
     * @return Returns a new PayloadMap with the key-value-pair
     * @throws UnsupportedEncodingException
     */
    public PayloadMap addJson(JsonNode jsonObject, boolean encode_base64)
            throws UnsupportedEncodingException;

    /**
     * Add the standard name-value-pairs, snowplow depends on them.
     * @see com.snowplowanalytics.snowplow.tracker.TrackerC
     * @param p Platform
     * @param tv Tracker Version
     * @param tna Namespace
     * @param aid App_id
     * @return Returns a new PayloadMap with the key-value-pair
     */
    public PayloadMap addStandardNvPairs(String p, String tv, String tna, String aid);

    /**
     * Add a configuration to the payload. Used currently for "base64_encode"
     * @param config_title Key of the configuration
     * @param config Value of the configuration
     * @return Returns a new PayloadMap with the key-value-pair
     */
    public PayloadMap addConfig(String config_title, boolean config);

    /**
     * Configuration of the PayloadMap for a track page view call.
     * @param page_url The URL or the page being tracked.
     * @param page_title The Title of the page being tracked.
     * @param referrer The referrer of the page being tracked.
     * @param context Additional JSON context (optional)
     * @param timestamp User-input timestamp or 0
     * @return Returns a new PayloadMap with the key-value-pair
     * @throws UnsupportedEncodingException
     */
    public PayloadMap trackPageViewConfig(String page_url, String page_title, String referrer,
                                          JsonNode context, long timestamp) throws UnsupportedEncodingException;

    /**
     * Configuration for tracking a structured event
     * @param category Category of the event being tracked.
     * @param action Action of the event being tracked
     * @param label Label of the event being tracked.
     * @param property Property of the event being tracked.
     * @param value Value associated with the property being tracked.
     * @param context Additional JSON context (optional)
     * @param timestamp User-input timestamp or 0
     * @return Returns a new PayloadMap with the key-value-pairs
     * @throws UnsupportedEncodingException
     */
    public PayloadMap trackStructuredEventConfig(String category, String action, String label, String property,
                                                 String value, JsonNode context, long timestamp)throws UnsupportedEncodingException;

    /**
     * Configuration to track an unstructured event.
     * @param eventVendor The vendor the the event information.
     * @param eventName A name for the unstructured event being tracked.
     * @param dictInfo The unstructured information being tracked in dictionary form.
     * @param context Additional JSON context for the tracking call (optional)
     * @param timestamp User-input timestamp or 0
     * @throws UnsupportedEncodingException If JSON is in improper formatting
     */
    public PayloadMap trackUnstructuredEventConfig(String eventVendor, String eventName, JsonNode dictInfo,
                                                   JsonNode context, long timestamp) throws UnsupportedEncodingException;
    /**
     * Configuration to track an ecommerce transaction item. Not usually called alone, but called for each
     * individual item of the ecommerce transaction function.
     * @param order_id ID of the item.
     * @param sku SKU value of the item.
     * @param price Prive of the item.
     * @param quantity Quantity of the item.
     * @param name Name of the item.
     * @param category Category of the item.
     * @param currency Currency used for the purchase.
     * @param context Additional JSON context for the tracking call (optional)
     * @param transaction_id Transaction ID, if left blank new value will be generated.
     * @param timestamp User-input timestamp or 0
     * @throws java.io.UnsupportedEncodingException
     * @return Returns a new PayloadMap with the key-value-pairs
     */
    public PayloadMap trackEcommerceTransactionItemConfig(String order_id, String sku, String price, String quantity,
                                                          String name, String category, String currency, JsonNode context, String transaction_id, long timestamp)
            throws UnsupportedEncodingException;

    /**
     * Track an Ecommerce Transaction
     * Option to provide a Map of only strings of items in the transaction which can be used
     * to track each individual ecommerce transaction item
     * @param order_id The transaction ID, will be generated if left null
     * @param total_value The total value of the transaction.
     * @param affiliation Affiliations to the transaction (optional)
     * @param tax_value Tax value of the transaction (optional)
     * @param shipping Shipping costs of the transaction (optional)
     * @param city The customers city.
     * @param state The customers state.
     * @param country The customers country.
     * @param currency The currency used for the purchase
     * @param context Additional JSON context for the tracking call (optional)
     * @param timestamp
     * @return Returns a new PayloadMap with the key-value-pair
     * @throws UnsupportedEncodingException
     */
    public PayloadMap trackEcommerceTransactionConfig(String order_id, String total_value, String affiliation, String tax_value,
                                                      String shipping, String city, String state, String country, String currency, JsonNode context, long timestamp)
            throws UnsupportedEncodingException;

    /**
     * Sets the timestamp on a PayloadMap
     * @return Returns a new PayloadMap with the key-value-pair
     */
    public void setTimestamp();

    /**
     * Sets the timestamp on a PayloadMap
     * @return Returns a new PayloadMap with the key-value-pair
     */
    public void setTimestamp(float timestamp);

    //Getters
    public Set<String> getParamKeySet();
    public Set<String> getConfigKeySet();
    public LinkedHashMap<String,String> getParams();
    public LinkedHashMap<String,Boolean> getConfigs();
    public String getParam(String key);
    public boolean getConfig(String key);
    public String toString();
}
