package com.snowplowanalytics.snowplow.tracker;

import com.snowplowanalytics.snowplow.tracker.core.payload.SchemaPayload;

import java.util.List;

public class TransactionItem extends com.snowplowanalytics.snowplow.tracker.core.TransactionItem {

    public TransactionItem(String order_id, String sku, double price, int quantity, String name,
                           String category, String currency) {
        super(order_id, sku, price, quantity, name, category, currency);
    }

    public TransactionItem(String order_id, String sku, double price, int quantity, String name,
                           String category, String currency, List<SchemaPayload> context) {
        super(order_id, sku, price, quantity, name, category, currency, context);
    }
}
