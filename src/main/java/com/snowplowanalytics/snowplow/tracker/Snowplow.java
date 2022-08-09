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
package com.snowplowanalytics.snowplow.tracker;

import com.snowplowanalytics.snowplow.tracker.emitter.BatchEmitter;

import java.util.HashMap;
import java.util.Map;

public class Snowplow {

    private static final Map<String, Tracker> trackers = new HashMap<>();
    private static Tracker defaultTracker;

    public static Map<String, Tracker> getTrackers() {
        return trackers;
    }

    public static Tracker createTracker(String collectorUrl, String namespace, String appId) {
        BatchEmitter emitter = BatchEmitter.builder().url(collectorUrl).build();
        Tracker tracker = new Tracker.TrackerBuilder(emitter, namespace, appId).build();
        registerTracker(tracker);
        return tracker;
    }

    public static void registerTracker(Tracker tracker) {
        String namespace = tracker.getNamespace();
        if (trackers.containsKey(namespace)) {
            throw new IllegalArgumentException("Tracker with this namespace already exists.");
        }

        trackers.put(namespace, tracker);

        if (defaultTracker == null) {
            defaultTracker = tracker;
        }
    }

    public static Tracker getTracker(String namespace) {
        return trackers.get(namespace);
    }

    public static boolean removeTracker(String namespace) {
        Tracker removedTracker = trackers.remove(namespace);
        if ((defaultTracker != null) && defaultTracker.getNamespace().equals(namespace)) {
            defaultTracker = null;
        }
        return removedTracker != null;
    }

    public static boolean removeTracker(Tracker tracker) {
        return removeTracker(tracker.getNamespace());
    }


    public static Tracker getDefaultTracker() {
        return defaultTracker;
    }

    public static void setDefaultTracker(Tracker tracker) {
        defaultTracker = tracker;
    }


}
