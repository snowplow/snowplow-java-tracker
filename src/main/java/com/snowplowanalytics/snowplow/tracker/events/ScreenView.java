/*
 * Copyright (c) 2014-2022 Snowplow Analytics Ltd. All rights reserved.
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

import com.google.common.base.Preconditions;

import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.constants.Constants;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;

import java.util.LinkedHashMap;

/**
 * Constructs a ScreenView event object.
 *
 * When tracked, generates an "unstructured" or "ue" event.
 */
public class ScreenView extends AbstractEvent {

    private final String name;
    private final String id;

    public static abstract class Builder<T extends Builder<T>> extends AbstractEvent.Builder<T> {

        private String name;
        private String id;

        /**
         * @param name The (human-readable) name of the screen view
         * @return itself
         */
        public T name(String name) {
            this.name = name;
            return self();
        }

        /**
         * @param id Screen view ID
         * @return itself
         */
        public T id(String id) {
            this.id = id;
            return self();
        }

        public ScreenView build() {
            return new ScreenView(this);
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

    protected ScreenView(Builder<?> builder) {
        super(builder);

        // Precondition checks
        Preconditions.checkArgument(builder.name != null || builder.id != null);

        this.name = builder.name;
        this.id = builder.id;
    }

    /**
     * Return the payload wrapped into a SelfDescribingJson. When a ScreenView is tracked,
     * the Tracker creates and tracks an Unstructured event from this SelfDescribingJson.
     *
     * @return the payload as a SelfDescribingJson.
     */
    public SelfDescribingJson getPayload() {
        LinkedHashMap<String,Object> payload = new LinkedHashMap<>();
        payload.put(Parameter.SV_ID, this.id);
        payload.put(Parameter.SV_NAME, this.name);
        return new SelfDescribingJson(Constants.SCHEMA_SCREEN_VIEW, payload);
    }
}
