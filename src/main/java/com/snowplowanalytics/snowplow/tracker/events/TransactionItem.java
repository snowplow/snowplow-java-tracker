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
package com.snowplowanalytics.snowplow.tracker.events;

// Java
import java.util.HashMap;
import java.util.List;

// This library
import com.snowplowanalytics.snowplow.tracker.constants.Constants;
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;

/**
 * A TransactionItem object which contains all the keys and values
 * for a valid TransactionItem
 */
public class TransactionItem extends HashMap {

    /**
     * @param itemId Order ID
     * @param sku Item SKU
     * @param price Item price
     * @param quantity Item quantity
     * @param name Item name
     * @param category Item category
     * @param currency The currency the price is expressed in
     */
    @Deprecated
    public TransactionItem(String itemId, String sku, Double price, Integer quantity, String name, String category,
                            String currency) {
        this(itemId, sku, price, quantity, name, category, currency, null, String.valueOf(System.currentTimeMillis()));
    }

    /**
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
    public TransactionItem(String itemId, String sku, Double price, Integer quantity, String name, String category,
                            String currency, List<SelfDescribingJson> context, String timestamp) {
        put(Parameter.EVENT, Constants.EVENT_ECOMM_ITEM);
        put(Parameter.TI_ITEM_ID, itemId);
        put(Parameter.TI_ITEM_SKU, sku);
        put(Parameter.TI_ITEM_NAME, name);
        put(Parameter.TI_ITEM_CATEGORY, category);
        put(Parameter.TI_ITEM_PRICE, price);
        put(Parameter.TI_ITEM_QUANTITY, quantity);
        put(Parameter.TI_ITEM_CURRENCY, currency);
        put(Parameter.CONTEXT, context);
        put(Parameter.TIMESTAMP, timestamp);
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    public Object put(Object key, Object value) {
        if (value != null || value != "") {
            return super.put(key, value);
        }
        else {
            return null;
        }
    }
}
