/*
 * Copyright (c) 2015 Snowplow Analytics Ltd. All rights reserved.
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

// Google
import com.google.common.base.Preconditions;

// This library
import com.snowplowanalytics.snowplow.tracker.Subject;
import com.snowplowanalytics.snowplow.tracker.Utils;
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.payload.Payload;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

/**
 * Base AbstractEvent class which contains common
 * elements to all events:
 * - Custom Context: list of custom contexts or null
 * - Timestamp: user defined event timestamp or 0
 * - Event Id: a unique id for the event
 * - Subject: a unique Subject for the event
 */
public abstract class AbstractEvent implements Event {

    protected final List<SelfDescribingJson> context;
    protected long timestamp;
    protected final String eventId;
    protected final Subject subject;

    public static abstract class Builder<T extends Builder<T>> {

        private List<SelfDescribingJson> context = new LinkedList<>();
        private long timestamp = System.currentTimeMillis();
        private String eventId = Utils.getEventId();
        private Subject subject = null;

        protected abstract T self();

        /**
         * Adds a list of custom contexts.
         *
         * @param context the list of contexts
         * @return itself
         */
        public T customContext(List<SelfDescribingJson> context) {
            this.context = context;
            return self();
        }

        /**
         * A custom event timestamp.
         *
         * @param timestamp the event timestamp as
         *                  unix epoch
         * @return itself
         */
        public T timestamp(long timestamp) {
            this.timestamp = timestamp;
            return self();
        }

        /**
         * A custom eventId for the event.
         *
         * @param eventId the eventId
         * @return itself
         */
        public T eventId(String eventId) {
            this.eventId = eventId;
            return self();
        }

        /**
         * A custom subject for the event.
         *
         * @param subject the eventId
         * @return itself
         */
        public T subject(Subject subject) {
            this.subject = subject;
            return self();
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

    protected AbstractEvent(Builder<?> builder) {

        // Precondition checks
        Preconditions.checkNotNull(builder.context);
        Preconditions.checkNotNull(builder.eventId);
        Preconditions.checkArgument(!builder.eventId.isEmpty(), "eventId cannot be empty");

        this.context = builder.context;
        this.timestamp = builder.timestamp;
        this.eventId = builder.eventId;
        this.subject = builder.subject;
    }

    /**
     * @return the events custom context
     */
    @Override
    public List<SelfDescribingJson> getContext() {
        return new ArrayList<>(this.context);
    }

    /**
     * @return the events timestamp
     */
    @Override
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * @return the event id
     */
    @Override
    public String getEventId() {
        return this.eventId;
    }

    /**
     * @return the event subject
     */
    @Override
    public Subject getSubject() {
        return this.subject;
    }

    /**
     * @return the event payload
     */
    @Override
    public abstract Payload getPayload();

    /**
     * Adds the default parameters to a TrackerPayload object.
     *
     * @param payload the payload to add too.
     * @return the TrackerPayload with appended values.
     */
    protected TrackerPayload putDefaultParams(TrackerPayload payload) {
        payload.add(Parameter.EID, getEventId());
        payload.add(Parameter.TIMESTAMP, Long.toString(getTimestamp()));
        return payload;
    }
}
