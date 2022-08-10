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

import com.snowplowanalytics.snowplow.tracker.configuration.EmitterConfiguration;
import com.snowplowanalytics.snowplow.tracker.configuration.NetworkConfiguration;
import com.snowplowanalytics.snowplow.tracker.configuration.TrackerConfiguration;
import com.snowplowanalytics.snowplow.tracker.emitter.BatchEmitter;
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class SnowplowTest {

    @After
    public void cleanUp(){
        Snowplow.reset();
    }

    @Test
    public void createsAndRetrievesATracker() {
        assertTrue(Snowplow.getTrackers().isEmpty());

        Tracker tracker = Snowplow.createTracker("http://endpoint", "namespace", "appId");
        Tracker retrievedTracker = Snowplow.getTracker("namespace");

        assertFalse(Snowplow.getTrackers().isEmpty());
        assertEquals(tracker, retrievedTracker);
        assertEquals("namespace", tracker.getNamespace());
        assertEquals("appId", tracker.getAppId());
        assertTrue(tracker.getBase64Encoded());
    }

    @Test
    public void preventsDuplicateNamespaces() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Snowplow.createTracker("http://endpoint", "namespace", "appId");
            Snowplow.createTracker("http://endpoint2", "namespace", "appId2");
        });

        assertEquals("Tracker with this namespace already exists.", exception.getMessage());
    }

    @Test
    public void deletesStoredTracker() {
        Snowplow.createTracker("http://endpoint", "namespace", "appId");
        boolean result = Snowplow.removeTracker("namespace");
        assertTrue(result);

        Tracker tracker = Snowplow.createTracker("http://endpoint", "namespace2", "appId");
        boolean result2 = Snowplow.removeTracker(tracker);
        assertTrue(result2);
    }

    @Test
    public void hasDefaultTracker() {
        assertNull(Snowplow.getDefaultTracker());

        Tracker tracker = Snowplow.createTracker("http://endpoint", "namespace", "appId");
        assertEquals(tracker, Snowplow.getDefaultTracker());

        Tracker tracker2 = Snowplow.createTracker("http://endpoint", "namespace2", "appId");
        assertEquals(tracker, Snowplow.getDefaultTracker());

        Snowplow.setDefaultTracker(tracker2);
        assertEquals(tracker2, Snowplow.getDefaultTracker());
    }

    @Test
    public void registersATrackerMadeWithoutSnowplowClass() {
        BatchEmitter emitter = BatchEmitter.builder().url("http://collector").build();
        Tracker tracker = new Tracker.TrackerBuilder(emitter, "namespace", "appId").build();

        Snowplow.registerTracker(tracker);
        assertEquals(tracker, Snowplow.getDefaultTracker());
        assertEquals(1, Snowplow.getTrackers().size());
    }

    @Test
    public void createsTrackerFromConfigs() {
        TrackerConfiguration trackerConfig = new TrackerConfiguration("namespace", "appId");
        NetworkConfiguration networkConfig = new NetworkConfiguration().collectorUrl("http://collector-endpoint");

        BatchEmitter emitter = new BatchEmitter(networkConfig);
        int size = emitter.getBatchSize();
        System.out.println(size);
//        Tracker tracker = Snowplow.createTracker(trackerConfig, networkConfig);
//        Tracker retrievedTracker = Snowplow.getTracker("namespace");
//
//        assertFalse(Snowplow.getTrackers().isEmpty());
//        assertEquals(tracker, retrievedTracker);
//        assertEquals("namespace", tracker.getNamespace());
//        assertEquals("appId", tracker.getAppId());
//        assertTrue(tracker.getBase64Encoded());
    }
}
