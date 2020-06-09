/*
 * Copyright (c) 2014-2020 Snowplow Analytics Ltd. All rights reserved.
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

package com.snowplowanalytics;

import com.snowplowanalytics.snowplow.tracker.DevicePlatform;
import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.emitter.BatchEmitter;
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.emitter.RequestCallback;
import com.snowplowanalytics.snowplow.tracker.events.*;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import static java.util.Collections.singletonList;

import com.google.common.collect.ImmutableMap;

public class Main {

    public static String getUrlFromArgs(String[] args) {
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("Collector URL is required");
        }
        return args[0];
    }

    public static void main(String[] args) {
        Set<String> failedEventIds = new HashSet<String>();
        String collectorEndpoint = getUrlFromArgs(args);

        System.out.println("Sending events to " + collectorEndpoint);

        // the application id to attach to events
        String appId = "java-tracker-sample-console-app";
        // the namespace to attach to events
        String namespace = "demo";

        // build an emitter, this is used by the tracker to batch and schedule transmission of events
        BatchEmitter emitter = BatchEmitter.builder()
                .url(collectorEndpoint)
                .requestCallback(new RequestCallback() {
                    // let us know on successes (may be called multiple times)
                    @Override
                    public synchronized void onSuccess(int successCount) {
                        System.out.println("Successfully sent " + successCount + " events");
                    }

                    // let us know if something has gone wrong (may be called multiple times)
                    @Override
                    public synchronized void onFailure(int successCount, List<Event> failedEvents) {
                        System.err.println("Successfully sent " + successCount + " events; failed to send " + failedEvents.size() + " events");
                    }
                })
                .bufferSize(4) // send an event every time one is given (no batching). In production this number should be higher, depending on the size/event volume
                .build();

        // now we have the emitter, we need a tracker to turn our events into something a Snowplow collector can understand
        final Tracker tracker = new Tracker.TrackerBuilder(emitter, namespace, appId)
            .base64(true)
            .platform(DevicePlatform.ServerSideApp)
            .build();

        // This is an example of a custom context
        List<SelfDescribingJson> contexts = singletonList(
            new SelfDescribingJson(
                "iglu:com.snowplowanalytics.iglu/anything-c/jsonschema/1-0-0",
                ImmutableMap.of("foo", "bar")));

        // This is a sample page view event, many other event types (such as self-describing events) are available
        PageView pageViewEvent = PageView.builder()
            .pageTitle("Snowplow Analytics")
            .pageUrl("https://www.snowplowanalytics.com")
            .referrer("https://www.google.com")
            .customContext(contexts)
            .build();
        
        tracker.track(pageViewEvent); // the .track method schedules the event for delivery to Snowplow

        EcommerceTransactionItem item = EcommerceTransactionItem.builder()
            .itemId("order_id")
            .sku("sku")
            .price(1.0)
            .quantity(2)
            .name("name")
            .category("category")
            .currency("currency")
            .customContext(contexts)
            .build();

        EcommerceTransaction ecommerceTransaction = EcommerceTransaction.builder()
            .orderId("order_id")
            .totalValue(1.0)
            .affiliation("affiliation")
            .taxValue(2.0)
            .shipping(3.0)
            .city("city")
            .state("state")
            .country("country")
            .currency("currency")
            .items(item) // EcommerceTransactionItem events are added to a parent EcommerceTransaction
            .customContext(contexts)
            .build();

        tracker.track(ecommerceTransaction); // This will track two events

        // This is an example of a custom "Unsutrcutred" event based on a schema
        Unstructured unstructured = Unstructured.builder()
            .eventData(new SelfDescribingJson(
                    "iglu:com.snowplowanalytics.iglu/anything-a/jsonschema/1-0-0",
                    ImmutableMap.of("foo", "bar")
            ))
            .customContext(contexts)
            .build();

        tracker.track(unstructured);

        // This is an example of a ScreenView event which will be translated into an Unstructured event
        ScreenView screenView = ScreenView.builder()
            .name("name")
            .id("id")
            .customContext(contexts)
            .build();

        tracker.track(screenView);

        // This is an example of a Timing event which will be translated into an Unstructured event
        Timing timing = Timing.builder()
            .category("category")
            .label("label")
            .variable("variable")
            .timing(10)
            .customContext(contexts)
            .build();

        tracker.track(timing);

        // Will close all threads and force send remaining events
        // should be 1 left to flush, as we send 5 events with a bufferSize of 4
        emitter.close();
    }

}
