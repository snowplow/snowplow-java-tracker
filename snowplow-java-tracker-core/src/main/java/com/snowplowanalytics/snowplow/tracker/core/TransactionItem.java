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

import com.snowplowanalytics.snowplow.tracker.core.payload.SchemaPayload;

import java.util.HashMap;
import java.util.List;

public class TransactionItem extends HashMap {

    public TransactionItem (String order_id, String sku, double price, int quantity, String name,
                            String category, String currency) {
        this(order_id,sku, price, quantity, name, category, currency, null);
    }

    public TransactionItem (String order_id, String sku, double price, int quantity, String name,
                            String category, String currency, List<SchemaPayload> context) {
        put(Parameter.EVENT, "ti");
        put(Parameter.TI_ITEM_ID, order_id);
        put(Parameter.TI_ITEM_SKU, sku);
        put(Parameter.TI_ITEM_NAME, name);
        put(Parameter.TI_ITEM_CATEGORY, category);
        put(Parameter.TI_ITEM_PRICE, price);
        put(Parameter.TI_ITEM_QUANTITY, quantity);
        put(Parameter.TI_ITEM_CURRENCY, currency);

        put(Parameter.CONTEXT, context);

        put(Parameter.TIMESTAMP, Util.getTimestamp());
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    public Object put(Object key, Object value) {
        if (value != null || value != "") return super.put(key, value);
        else
            return null;
    }
}
