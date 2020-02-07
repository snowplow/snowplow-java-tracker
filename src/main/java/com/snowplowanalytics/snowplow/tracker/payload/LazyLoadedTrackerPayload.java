package com.snowplowanalytics.snowplow.tracker.payload;

import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.events.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A TrackerPayload which will not be filled on creation. The payload will be
 * filled by the Emitter in the Emitter thread, using the fillPayload() method.
 */
public class LazyLoadedTrackerPayload extends TrackerPayload {

  private static final Logger LOGGER = LoggerFactory.getLogger(LazyLoadedTrackerPayload.class);

  private Event event;
  private Tracker tracker;
  private TrackerPayload trackerPayload;

  public LazyLoadedTrackerPayload(Tracker tracker, Event event) {
    this.event = event;
    this.tracker = tracker;
  }

  public void fillPayload() {
    this.trackerPayload = this.tracker.getTrackerPayload(event);
  }

  public TrackerPayload getTrackerPayload(){
    return this.trackerPayload;
  }
}
