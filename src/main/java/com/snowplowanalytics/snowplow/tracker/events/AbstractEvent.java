/*
 * Copyright (c) 2014-2021 Snowplow Analytics Ltd. All rights reserved.
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
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.payload.Payload;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

/**
 * Base AbstractEvent class which contains
 * elements that can be set in all events. These are context, trueTimestamp, and Subject.
 *
 * Context is a list of custom SelfDescribingJson entities.
 * TrueTimestamp is a user-defined timestamp.
 * Subject is an event-specific Subject. Its fields will override those of the
 * Tracker-associated Subject, if present.
 */
public abstract class AbstractEvent implements Event {

    protected final List<SelfDescribingJson> context;

    /**
     * The trueTimestamp may be null if none is set.
     */
    protected Long trueTimestamp;
    protected final Subject subject;

    public static abstract class Builder<T extends Builder<T>> {

        private List<SelfDescribingJson> context = new LinkedList<>();
        protected Long trueTimestamp = null;
        private Subject subject = null;

        protected abstract T self();

        /**
         * Adds a list of custom context entities.
         *
         * @param context the list of entities
         * @return itself
         */
        public T customContext(List<SelfDescribingJson> context) {
            this.context = context;
            return self();
        }

        /**
         * The true timestamp of that event (as determined by the user).
         *
         * @param timestamp the event timestamp as
         *                  unix epoch
         * @return itself
         */
        public T trueTimestamp(Long timestamp) {
            this.trueTimestamp = timestamp;
            return self();
        }

        /**
         * A custom subject for the event. Its fields will override those of the
         * Tracker-associated Subject, if present.
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

        this.context = builder.context;
        this.trueTimestamp = builder.trueTimestamp;
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
     * @return the event's true timestamp.
     */
    @Override
    public Long getTrueTimestamp() {
        return trueTimestamp;
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
     * @param payload the payload to add to.
     * @return the TrackerPayload with appended values.
     */
    protected TrackerPayload putTrueTimestamp(TrackerPayload payload) {
        if (getTrueTimestamp() != null) {
            payload.add(Parameter.TRUE_TIMESTAMP, Long.toString(getTrueTimestamp()));
        }
        return payload;
    }
}
