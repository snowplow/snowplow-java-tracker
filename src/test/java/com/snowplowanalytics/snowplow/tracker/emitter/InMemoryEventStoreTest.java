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

import com.snowplowanalytics.snowplow.tracker.DevicePlatform;
import com.snowplowanalytics.snowplow.tracker.events.PageView;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerEvent;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerParameters;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class InMemoryEventStoreTest {

    private TrackerEvent trackerEvent;
    private InMemoryEventStore eventStore;
    private List<TrackerEvent> singleEventList;
    private List<TrackerEvent> twoEventsList;


    @Before
    public void setUp() {
        trackerEvent = createEvent();
        eventStore = new InMemoryEventStore();
        singleEventList = new ArrayList<>();
        twoEventsList = new ArrayList<>();

        singleEventList.add(trackerEvent);
        twoEventsList.add(trackerEvent);
        twoEventsList.add(trackerEvent);
    }

    @Test
    public void correctlyAddAnEventToStore() {
        boolean result = eventStore.add(trackerEvent);

        Assert.assertTrue(result);
    }

    @Test
    public void getSize_returnsCorrectNumberOfStoredEvents() {
        storeTwoEvents();

        Assert.assertEquals(2, eventStore.getSize());
    }

    @Test
    public void removeAddedEvent() {
        storeTwoEvents();

        List<TrackerEvent> removedEventList = eventStore.removeEvents(1);
        Assert.assertEquals(singleEventList, removedEventList);
        Assert.assertEquals(1, eventStore.getSize());
    }

    @Test
    public void removeAllEventsIfAskedForMoreEventsThanAreStored() {
        storeTwoEvents();

        List<TrackerEvent> removedEventList = eventStore.removeEvents(100);
        Assert.assertEquals(twoEventsList, removedEventList);
        Assert.assertEquals(0, eventStore.getSize());
    }

    @Test
    public void getAllEvents_doesNotRemoveEventsFromStore() {
        storeTwoEvents();

        List<TrackerEvent> retrievedEventsList = eventStore.getAllEvents();
        Assert.assertEquals(twoEventsList, retrievedEventsList);
        Assert.assertEquals(2, eventStore.getSize());
    }

    private TrackerEvent createEvent() {
        PageView pv = PageView.builder()
                .pageUrl("https://www.snowplowanalytics.com/")
                .pageTitle("Snowplow")
                .referrer("https://www.google.com/")
                .build();

        return new TrackerEvent(pv, new TrackerParameters("appId", DevicePlatform.ServerSideApp, "namespace", "0.0.0", false), null);
    }

    private void storeTwoEvents() {
        for (TrackerEvent event : twoEventsList) {
            eventStore.add(event);
        }
    }
}
