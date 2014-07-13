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
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * Tracker Interface
 * The tracker interface contains all usable tracking commands that are implemented
 *  in the TrackerC class.
 *  {@inheritDoc}
 * @see com.snowplowanalytics.snowplow.tracker.TrackerC
 * @version 0.3.0
 * @author Kevin Gleason, Jonathan Almeida, Alex Dean
 */

public interface Tracker {
    /**
     * The basic track command. All other track functions eventually call this.
     * The function compiles all the parameters in the PayloadMap into a proper
     * URI which then is made into a HttpGet instance. The GET request is then sent to
     * the collector URI where it is logged.
     * @throws URISyntaxException
     * @throws IOException
     */
    public void track() throws URISyntaxException, IOException;

    /**
     * Track a single page view on a java web applications.
     * @param page_url The url of the page where the tracking call lies.
     * @param page_title The title of the page where the tracking call lies. (optional)
     * @param referrer The one who referred you to the page (optional)
     * @param context Additional JSON context for the tracking call (optional)
     * @param timestamp User-input timestamp or 0
     * @throws URISyntaxException
     * @throws IOException
     */
    public void trackPageView(String page_url, String page_title, String referrer, Map context, long timestamp)
            throws IOException, URISyntaxException;

    /**
     * Track a structured event. Useful for tracking data transfer and other structured transactions.
     * @param category The category of the structured event.
     * @param action The action that is being tracked. (optional)
     * @param label A label for the tracking event. (optional)
     * @param property The property of the structured event being tracked. (optional)
     * @param value The value associated with the property being tracked.
     * @param vendor The vendor the the property being tracked. (optional)
     * @param context Additional JSON context for the tracking call (optional)
     * @param timestamp User-input timestamp or 0
     * @throws URISyntaxException If there is an issue with the tracking call.
     * @throws IOException If there is an issue with processing the HTTP GET
     */
    public void trackStructuredEvent(String category, String action, String label, String property,
                                     int value, String vendor, Map context, long timestamp) throws URISyntaxException, IOException;

    /**
     * Track an unstructured event. Allowed to use String or Map<String,Object> as input
     * @param eventVendor The vendor the the event information.
     * @param eventName A name for the unstructured event being tracked.
     * @param dictInfo The unstructured information being tracked in dictionary form.
     * @param context Additional JSON context for the tracking call (optional)
     * @param timestamp User-input timestamp or 0
     * @throws IOException If there is an issue with the tracking call.
     * @throws URISyntaxException If there is an issue with processing the HTTP GET
     */
    public void trackUnstructuredEvent(String eventVendor, String eventName, Map<String, Object> dictInfo, Map context, long timestamp)
            throws IOException, URISyntaxException;

    /**
     * Track a screen view
     * @param name The name of the screen view being tracked
     * @param id The ID of the screen view being tracked.
     * @param context Additional JSON context for the tracking call (optional)
     * @param timestamp User-input timestamp or 0
     * @throws IOException
     * @throws URISyntaxException
     */
    public void trackScreenView(String name, String id, Map context, long timestamp)
            throws IOException, URISyntaxException;

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
     * @param items A list containing a Map of Strings. Each item must have order ID, sku, price, and quantity.
     * @param context Additional JSON context for the tracking call (optional)
     * @param timestamp User-input timestamp or 0
     * @throws IOException
     * @throws URISyntaxException
     */
    public void trackEcommerceTransaction(String order_id, Double total_value, String affiliation, Double tax_value,
                                          Double shipping, String city, String state, String country, String currency, List<TransactionItem> items, Map context, long timestamp)
            throws IOException, URISyntaxException;

    /**
     * Set Contractors
     *  Not required, but useful if you want to make a contractor with a custom checker for processing.
     *  Requires three inputs, a contractor class of each type.
     * @param stringContractor A contractory of type String
     * @param dictionaryContractor A Contractor of type Map with key value of String, Object
     */
    public void setContractors(ContractManager<String> stringContractor,
            ContractManager<Map<String, Object>> dictionaryContractor);

    /**
     * Used to add custom parameter. Be careful with use, must abide by snowplow table standards.
     * See snowplow documentation
     * @param param Parameter to be set.
     * @param val Value for the parameter.
     */
    public void setParam(String param, String val);

    /**
     * Set the platform for the tracking instance. (optional)
     * The default platform is PC.
     * @param platform The platform being tracked, currently supports "pc", "tv", "mob", "cnsl", and "iot".
     */
    public void setPlatform(String platform);

    /**
     * Set the uesrID parameter (optional)
     * @param userID The User ID String.
     */
    public void setUserID(String userID);

    /**
     * Set the screen resolution width and height (optional)
     * @param width Width of the screen in pixels.
     * @param height Height of the screen in pixels.
     */
    public void setScreenResolution(int width, int height);

    /**
     * Set the viewport of the screen.
     * @param width Width of the viewport in pixels.
     * @param height Height of the viewport in pixels.
     */
    public void setViewport(int width, int height);

    /**
     * Set the color depth (optional)
     * @param depth Depth of the color.
     */
    public void setColorDepth(int depth);

    /**
     * Set the timezone (optioanl)
     * @param timezone Timezone where tracking takes place.
     */
    public void setTimezone(String timezone);

    /**
     * Set the language (optional)
     * @param language Language for info tracked.
     */
    public void setLanguage(String language);

    /**
     * Get the payload, use if you want to understand how it is set up.
     * @return Returns the payload, can be used with caution to customize parameters.
     */
    public PayloadMap getPayload();
}
