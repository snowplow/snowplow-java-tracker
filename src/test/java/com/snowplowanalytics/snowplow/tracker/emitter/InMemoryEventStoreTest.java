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
    private List<TrackerPayload> singleEventList;
    private List<TrackerPayload> twoEventsList;


    @Before
    public void setUp() {
        trackerPayload = createPayload();
        eventStore = new InMemoryEventStore();
        singleEventList = new ArrayList<>();
        twoEventsList = new ArrayList<>();

        singleEventList.add(trackerPayload);
        twoEventsList.add(trackerPayload);
        twoEventsList.add(trackerPayload);
    }

    @Test
    public void correctlyAddAnEventToStore() {
        boolean result = eventStore.add(trackerPayload);

        Assert.assertTrue(result);
    }

    @Test
    public void getSize_returnsCorrectNumberOfStoredEvents() {
        storeTwoPayloads();

        Assert.assertEquals(2, eventStore.getSize());
    }

    @Test
    public void removeAddedEvent() {
        storeTwoPayloads();

        List<TrackerPayload> removedEventList = eventStore.removeEvents(1);
        Assert.assertEquals(singleEventList, removedEventList);
        Assert.assertEquals(1, eventStore.getSize());
    }

    @Test
    public void removeAllEventsIfAskedForMoreEventsThanAreStored() {
        storeTwoPayloads();

        List<TrackerPayload> removedEventList = eventStore.removeEvents(100);
        Assert.assertEquals(twoEventsList, removedEventList);
        Assert.assertEquals(0, eventStore.getSize());
    }

    @Test
    public void getAllEvents_doesNotRemoveEventsFromStore() {
        storeTwoPayloads();

        List<TrackerPayload> retrievedEventsList = eventStore.getAllEvents();
        Assert.assertEquals(twoEventsList, retrievedEventsList);
        Assert.assertEquals(2, eventStore.getSize());
    }

    private TrackerPayload createPayload() {
        PageView pv = PageView.builder()
                .pageUrl("https://www.snowplowanalytics.com/")
                .pageTitle("Snowplow")
                .referrer("https://www.google.com/")
                .build();

        return pv.getPayload();
    }

    private void storeTwoPayloads() {
        for (TrackerPayload payload : twoEventsList) {
            eventStore.add(payload);
        }
    }
}
