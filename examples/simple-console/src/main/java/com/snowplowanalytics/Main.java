/*
 * Copyright (c) 2017 Snowplow Analytics Ltd. All rights reserved.
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
import com.snowplowanalytics.snowplow.tracker.events.PageView;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.http.OkHttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import com.squareup.okhttp.OkHttpClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final int PAGEVIEW_COUNT = 20;

    public static String getUrlFromArgs(String[] args) {
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("Collector URL is required");
        }
        return args[0];
    }

    public static HttpClientAdapter getClient(String url) {
        // use okhttp to send events
        OkHttpClient client = new OkHttpClient();

        client.setConnectTimeout(5, TimeUnit.SECONDS);
        client.setReadTimeout(5, TimeUnit.SECONDS);
        client.setWriteTimeout(5, TimeUnit.SECONDS);

        return OkHttpClientAdapter.builder()
                .url(url)
                .httpClient(client)
                .build();
    }

    public static void main(String[] args) {
        String collectorEndpoint = getUrlFromArgs(args);

        System.out.println("Sending " + PAGEVIEW_COUNT + " events to " + collectorEndpoint);

        // get the client adapter
        // this is used by the Java tracker to transmit events to the collector
        HttpClientAdapter okHttpClientAdapter = getClient(collectorEndpoint);

        // the application id to attach to events
        String appId = "java-tracker-sample-console-app";
        // the namespace to attach to events
        String namespace = "demo";

        // build an emitter, this is used by the tracker to batch and schedule transmission of events
        Emitter emitter = BatchEmitter.builder()
                .httpClientAdapter(okHttpClientAdapter)
                .requestCallback(new RequestCallback() {
                    // let us know on successes (may be called multiple times)
                    @Override
                    public void onSuccess(int successCount) {
                        System.out.println("Successfully sent " + successCount + " events");
                    }

                    // let us know if something has gone wrong (may be called multiple times)
                    @Override
                    public void onFailure(int successCount, List<TrackerPayload> failedEvents) {
                        System.err.println("Successfully sent " + successCount + " events; failed to send " + failedEvents.size() + " events");
                    }
                })
                .bufferSize(1) // send an event every time one is given (no batching). In production this number should be higher, depending on the size/event volume
                .build();

        // now we have the emitter, we need a tracker to turn our events into something a Snowplow collector can understand
        Tracker tracker = new Tracker.TrackerBuilder(emitter, namespace, appId)
                .base64(true)
                .platform(DevicePlatform.ServerSideApp)
                .build();

        // This is a sample page view event, many other event types (such as self-describing events) are available
        PageView pageViewEvent = PageView.builder()
                .pageTitle("Hello world")
                .pageUrl("http://helloworld.com")
                .build();

        for (int i = 0; i < PAGEVIEW_COUNT; i++) {
            tracker.track(pageViewEvent); // the .track method schedules the event for delivery to Snowplow
        }

    }

}
