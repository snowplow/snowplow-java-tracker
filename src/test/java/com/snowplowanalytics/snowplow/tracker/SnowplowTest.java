/*
 * Copyright (c) 2014-present Snowplow Analytics Ltd. All rights reserved.
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

import com.snowplowanalytics.snowplow.tracker.configuration.NetworkConfiguration;
import com.snowplowanalytics.snowplow.tracker.configuration.TrackerConfiguration;
import com.snowplowanalytics.snowplow.tracker.emitter.BatchEmitter;
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
        assertTrue(Snowplow.getInstancedTrackerNamespaces().isEmpty());

        Tracker tracker = Snowplow.createTracker("namespace", "appId", "http://endpoint");
        Tracker retrievedTracker = Snowplow.getTracker("namespace");

        assertFalse(Snowplow.getInstancedTrackerNamespaces().isEmpty());
        assertEquals(tracker, retrievedTracker);
        assertEquals("namespace", tracker.getNamespace());
        assertEquals("appId", tracker.getAppId());
        assertTrue(tracker.getBase64Encoded());
    }

    @Test
    public void preventsDuplicateNamespaces() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Snowplow.createTracker("namespace", "appId", "http://endpoint");
            Snowplow.createTracker("namespace", "appId2", "http://collector");
        });

        assertEquals("Tracker with this namespace already exists.", exception.getMessage());
    }

    @Test
    public void deletesStoredTracker() {
        Snowplow.createTracker("namespace", "appId", "http://endpoint");
        boolean result = Snowplow.removeTracker("namespace");
        assertTrue(result);

        Tracker tracker = Snowplow.createTracker("namespace2", "appId", "http://endpoint");
        boolean result2 = Snowplow.removeTracker(tracker);
        assertTrue(result2);
    }

    @Test
    public void doesNotDeleteUnregisteredTracker() {
        BatchEmitter emitter = new BatchEmitter(new NetworkConfiguration("http://collector"));
        Tracker tracker = new Tracker(new TrackerConfiguration("namespace", "appId"), emitter);

        boolean result = Snowplow.removeTracker(tracker);
        assertFalse(result);

        boolean result2 = Snowplow.removeTracker("not registered");
        assertFalse(result2);
    }

    @Test
    public void setsDefaultTrackerFromObject() {
        assertNull(Snowplow.getDefaultTracker());

        Tracker tracker = Snowplow.createTracker("namespace", "appId", "http://endpoint");
        assertEquals(tracker, Snowplow.getDefaultTracker());

        Tracker tracker2 = Snowplow.createTracker("namespace2", "appId", "http://endpoint");
        // The first tracker is still the default
        assertEquals(tracker, Snowplow.getDefaultTracker());

        Snowplow.setDefaultTracker(tracker2);
        assertEquals(tracker2, Snowplow.getDefaultTracker());
        assertEquals(2, Snowplow.getInstancedTrackerNamespaces().size());
    }

    @Test
    public void setsDefaultTrackerFromNamespace() {
        assertNull(Snowplow.getDefaultTracker());

        Snowplow.createTracker("namespace", "appId", "http://endpoint");
        Tracker tracker2 = Snowplow.createTracker("namespace2", "appId", "http://endpoint");

        boolean result = Snowplow.setDefaultTracker("namespace2");
        assertTrue(result);
        assertEquals(tracker2, Snowplow.getDefaultTracker());
    }

    @Test
    public void registersATrackerMadeWithoutSnowplowClass() {
        BatchEmitter emitter = new BatchEmitter(new NetworkConfiguration("http://collector"));
        Tracker tracker = new Tracker(new TrackerConfiguration("namespace", "appId"), emitter);

        Snowplow.registerTracker(tracker);

        assertEquals(tracker, Snowplow.getDefaultTracker());
        assertEquals(1, Snowplow.getInstancedTrackerNamespaces().size());
    }

    @Test
    public void settingNewDefaultTrackerRegistersIt() {
        BatchEmitter emitter = new BatchEmitter(new NetworkConfiguration("http://collector"));
        Tracker tracker = new Tracker(new TrackerConfiguration("new_tracker", "appId"), emitter);

        Snowplow.setDefaultTracker(tracker);

        assertEquals(1, Snowplow.getInstancedTrackerNamespaces().size());
        assertEquals("new_tracker", Snowplow.getDefaultTracker().getNamespace());
    }

    @Test
    public void createsTrackerFromConfigs() {
        TrackerConfiguration trackerConfig = new TrackerConfiguration("namespace", "appId")
                .base64Encoded(false)
                .platform(DevicePlatform.Desktop);
        NetworkConfiguration networkConfig = new NetworkConfiguration("http://collector-endpoint");

        Tracker tracker = Snowplow.createTracker(trackerConfig, networkConfig);
        Tracker retrievedTracker = Snowplow.getTracker("namespace");

        assertFalse(Snowplow.getInstancedTrackerNamespaces().isEmpty());
        assertEquals(tracker, retrievedTracker);
        assertEquals("namespace", tracker.getNamespace());
        assertEquals("appId", tracker.getAppId());
        assertEquals(DevicePlatform.Desktop, tracker.getPlatform());
        assertFalse(tracker.getBase64Encoded());
    }
}
