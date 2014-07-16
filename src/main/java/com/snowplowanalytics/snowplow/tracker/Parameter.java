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

public class Parameter {
    public static final String SCHEMA = "$schema";
    public static final String DATA = "data";
    public static final String EVENT = "e";
    public static final String TID = "tid";
    public static final String EID = "eid";
    public static final String TIMESTAMP = "dtm";

    public static final String UID = "uid";
    public static final String CONTEXT = "co";

    public static final String ITEM_ID = "ti_id";
    public static final String ITEM_SKU = "ti_sk";
    public static final String ITEM_NAME = "ti_nm";
    public static final String ITEM_CATEGORY = "ti_ca";
    public static final String ITEM_PRICE = "ti_pr";
    public static final String ITEM_QUANTITY = "ti_qu";
    public static final String ITEM_CURRENCY = "ti_cu";
    //TODO: Finish when time permits
}
