/*
 * Copyright (c) 2014-2020 Snowplow Analytics Ltd. All rights reserved.
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

// Google
import com.google.common.base.Preconditions;

// This library
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.constants.Constants;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

/**
 * Constructs an Unstructured event object.
 */
public class Unstructured extends AbstractEvent {

    private final SelfDescribingJson eventData;
    private boolean base64Encode;

    public static abstract class Builder<T extends Builder<T>> extends AbstractEvent.Builder<T> {

        private SelfDescribingJson eventData;

        /**
         * @param selfDescribingJson The properties of the event. Has two field:
         *                  A "data" field containing the event properties and
         *                  A "schema" field identifying the schema against which the data is validated
         * @return itself
         */
        public T eventData(SelfDescribingJson selfDescribingJson) {
            this.eventData = selfDescribingJson;
            return self();
        }

        public Unstructured build() {
            return new Unstructured(this);
        }
    }

    private static class Builder2 extends Builder<Builder2> {
        @Override
        protected Builder2 self() {
            return this;
        }
    }

    public static Builder<?> builder() {
        return new Builder2();
    }

    protected Unstructured(Builder<?> builder) {
        super(builder);

        // Precondition checks
        Preconditions.checkNotNull(builder.eventData);

        this.eventData = builder.eventData;
    }

    /**
     * @param base64Encode whether to base64Encode the event data
     */
    public void setBase64Encode(boolean base64Encode) {
        this.base64Encode = base64Encode;
    }

    /**
     * Returns a TrackerPayload which can be stored into
     * the local database.
     *
     * @return the payload to be sent.
     */
    public TrackerPayload getPayload() {
        TrackerPayload payload = new TrackerPayload();
        SelfDescribingJson envelope = new SelfDescribingJson(
                Constants.SCHEMA_UNSTRUCT_EVENT, this.eventData.getMap());
        payload.add(Parameter.EVENT, Constants.EVENT_UNSTRUCTURED);
        payload.addMap(envelope.getMap(), this.base64Encode,
                Parameter.UNSTRUCTURED_ENCODED, Parameter.UNSTRUCTURED);
        return putDefaultParams(payload);
    }
}
