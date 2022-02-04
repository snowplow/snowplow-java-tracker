package com.snowplowanalytics.snowplow.tracker.emitter;

import com.snowplowanalytics.snowplow.tracker.Utils;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

import java.util.*;

public class InMemoryEventStore implements EventStore {

    // this is not thread safe!
    public final LinkedHashMap<String, TrackerPayload> eventBuffer = new LinkedHashMap<>();


    @Override
    public boolean addEvent(TrackerPayload trackerPayload) {
        // Using a new UUID instead of the event UUID for the key
        // in case of problems with non-unique event IDs.
        // Event IDs can be set by the user.
        eventBuffer.put(Utils.getEventId(), trackerPayload);

        // returning true just because this was doing a queue offer before
        // LinkedHashMap will get full when out of memory
        // could artificially set the size
        // probably better to just stop this returning a boolean
        return true;
    }

    @Override
    public List<EmitterPayload> getEvents(int numberToGet) {
        List<EmitterPayload> eventsToSend = new ArrayList<>();

        // Hopefully this makes a copy?
        // Shouldn't modify the Map while iterating over entrySet
        LinkedHashMap<String, TrackerPayload> bufferSnapshot = new LinkedHashMap<>(eventBuffer);

        for (Map.Entry<String, TrackerPayload> event : bufferSnapshot.entrySet()) {
            eventsToSend.add(new EmitterPayload(event.getKey(), event.getValue()));
            if (eventsToSend.size() == numberToGet) {
                break;
            }
        }
        return eventsToSend;
    }

    @Override
    public void removeEvents(List<String> eventIds) {

        // should it log something if the eventId is invalid?
        // is it possible for eventId to be invalid?
        for (String eventId : eventIds) {
            eventBuffer.remove(eventId);
        }
    }

    @Override
    public int getSize() {
        return eventBuffer.size();
    }
}
