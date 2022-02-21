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

package com.snowplowanalytics;

import com.snowplowanalytics.snowplow.tracker.DevicePlatform;
import com.snowplowanalytics.snowplow.tracker.Subject;
import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.emitter.BatchEmitter;
import com.snowplowanalytics.snowplow.tracker.events.*;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;

import java.util.List;
import static java.util.Collections.singletonList;

import com.google.common.collect.ImmutableMap;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

public class Main {

    public static String getUrlFromArgs(String[] args) {
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("Collector URL is required");
        }
        return args[0];
    }

    public static PageView getPageView() {
        return PageView.builder()
                .pageTitle("Snowplow Analytics")
                .pageUrl("https://www.snowplowanalytics.com")
                .referrer("https://www.google.com")
                .build();
    }

    public static void main(String[] args) throws InterruptedException {
        String collectorEndpoint = getUrlFromArgs(args);

        // the application id to attach to events
        String appId = "java-tracker-sample-console-app";
        // the namespace to attach to events
        String namespace = "demo";

        // build an emitter, this is used by the tracker to batch and schedule transmission of events
        BatchEmitter emitter = BatchEmitter.builder()
                .url(collectorEndpoint)
                .bufferSize(4) // send batches of 4 events. In production this number should be higher, depending on the size/event volume
                .build();

        // now we have the emitter, we need a tracker to turn our events into something a Snowplow collector can understand
        final Tracker tracker = new Tracker.TrackerBuilder(emitter, namespace, appId)
            .base64(true)
            .platform(DevicePlatform.ServerSideApp)
            .build();

        System.out.println("Sending events to " + collectorEndpoint);
        System.out.println("Using tracker version " + tracker.getTrackerVersion());

        // This is an example of a custom context entity
        List<SelfDescribingJson> context = singletonList(
            new SelfDescribingJson(
                "iglu:com.snowplowanalytics.iglu/anything-c/jsonschema/1-0-0",
                ImmutableMap.of("foo", "bar")));

        // This is an example of a eventSubject for adding user data
        Subject eventSubject = new Subject.SubjectBuilder().build();
        eventSubject.setUserId("example@snowplowanalytics.com");
        eventSubject.setLanguage("EN");

        // This is a sample page view event
        // the eventSubject has been included in this event
        PageView pageViewEvent = PageView.builder()
            .pageTitle("Snowplow Analytics")
            .pageUrl("https://www.snowplowanalytics.com")
            .referrer("https://www.google.com")
            .customContext(context)
            .subject(eventSubject)
            .build();
        
        // EcommerceTransactionItems are tracked as part of an EcommerceTransaction event
        // They are processed into separate events during the `track()` call
        EcommerceTransactionItem item = EcommerceTransactionItem.builder()
            .itemId("order_id")
            .sku("sku")
            .price(1.0)
            .quantity(2)
            .name("name")
            .category("category")
            .currency("currency")
            .customContext(context)
            .build();

        // EcommerceTransaction event
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
            .items(item) // EcommerceTransactionItem events are added to a parent EcommerceTransaction here
            .customContext(context)
            .build();


        // This is an example of a custom "Unstructured" event based on a schema
        // Unstructured events are also called "self-describing" events
        // because of their SelfDescribingJson base
        Unstructured unstructured = Unstructured.builder()
            .eventData(new SelfDescribingJson(
                    "iglu:com.snowplowanalytics.iglu/anything-a/jsonschema/1-0-0",
                    ImmutableMap.of("foo", "bar")
            ))
            .customContext(context)
            .build();


        // This is an example of a ScreenView event which will be translated into an Unstructured event
        ScreenView screenView = ScreenView.builder()
            .name("name")
            .id("id")
            .customContext(context)
            .build();


        // This is an example of a Timing event which will be translated into an Unstructured event
        Timing timing = Timing.builder()
            .category("category")
            .label("label")
            .variable("variable")
            .timing(10)
            .customContext(context)
            .build();

        // This is an example of a Structured event
        Structured structured = Structured.builder()
                .category("category")
                .action("action")
                .label("label")
                .property("property")
                .value(12.34)
                .customContext(context)
                .build();

//        Thread.sleep(30000);

        System.out.println("About to track events");


        for (int i = 0; i < 150; i++) {
//            tracker.track(pageViewEvent); // the .track method schedules the event for delivery to Snowplow
//            tracker.track(ecommerceTransaction); // This will track two events
//            tracker.track(unstructured);
//            tracker.track(screenView);
//            tracker.track(timing);
//            tracker.track(structured);

            tracker.track(getPageView());
            tracker.track(getPageView());
            tracker.track(getPageView());
            tracker.track(getPageView());
            tracker.track(getPageView());
            tracker.track(getPageView());
            tracker.track(getPageView());
            Thread.sleep(10);
        }

//        Thread.sleep(5000);


        // Will close all threads and force send remaining events
        emitter.close();

        Thread.sleep(5000);

        System.out.println("Tracked 7 events");
    }

}
