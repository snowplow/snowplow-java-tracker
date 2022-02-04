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
package com.snowplowanalytics.snowplow.tracker.emitter;

import com.snowplowanalytics.snowplow.tracker.events.PageView;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class InMemoryEventStoreTest {

    private TrackerPayload trackerPayload;
    private InMemoryEventStore eventStore;

    @Before
    public void setUp() {
        trackerPayload = createTrackerPayload();
        eventStore = new InMemoryEventStore();
    }

    @Test
    public void correctlyAddAnEventToStore() {
        boolean result = eventStore.addEvent(trackerPayload);

        Assert.assertTrue(result);
    }

    @Test
    public void getSize_returnsCorrectNumberOfStoredEvents() {
        eventStore.addEvent(trackerPayload);
        eventStore.addEvent(trackerPayload);

        Assert.assertEquals(2, eventStore.getSize());
    }

    @Test
    public void getEventsFromStorage() {
        eventStore.addEvent(trackerPayload);
        eventStore.addEvent(trackerPayload);
        eventStore.addEvent(trackerPayload);
        eventStore.addEvent(trackerPayload);

        System.out.println("EventStore has this many events right now: " + eventStore.getSize());

        List<EmitterPayload> retrievedEventList = eventStore.getEvents(2);
        Assert.assertEquals(2, retrievedEventList.size());
        Assert.assertEquals(4, eventStore.getSize());
    }

    @Test
    public void getAllEventsIfAskedForMoreEventsThanAreStored() {
        eventStore.addEvent(trackerPayload);
        eventStore.addEvent(trackerPayload);

        List<EmitterPayload> retrievedEventList = eventStore.getEvents(10);
        Assert.assertEquals(2, retrievedEventList.size());
    }

    @Test
    public void removeEventsFromStorage() {
        eventStore.addEvent(trackerPayload);

        List<EmitterPayload> retrievedEventList = eventStore.getEvents(1);
        List<String> eventIds = new ArrayList<>();
        eventIds.add(retrievedEventList.get(0).getEventId());

        eventStore.removeEvents(eventIds);

        Assert.assertEquals(0, eventStore.getSize());
    }

    private TrackerPayload createTrackerPayload() {
        PageView pv = PageView.builder()
                .pageUrl("https://www.snowplowanalytics.com/")
                .pageTitle("Snowplow")
                .referrer("https://www.google.com/")
                .build();

        return pv.getPayload();
    }
}
