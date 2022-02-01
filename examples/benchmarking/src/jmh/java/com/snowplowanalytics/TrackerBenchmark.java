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

import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.emitter.BatchEmitter;
import com.snowplowanalytics.snowplow.tracker.events.PageView;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 30, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 30, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(4)
public class TrackerBenchmark {
    @State(Scope.Benchmark)
    public static class TrackerComponents {
        public static class MockHttpClientAdapter implements HttpClientAdapter {
            public int getPostCount() {
                return postCount;
            }

            private int postCount = 0;

            @Override
            public int post(SelfDescribingJson payload) {
                postCount++;
                return 200;
            }

            @Override
            public int get(TrackerPayload payload) {
                return 0;
            }

            @Override
            public String getUrl() {
                return null;
            }

            @Override
            public Object getHttpClient() {
                return null;
            }
        }

        MockHttpClientAdapter mockHttpClientAdapter = new MockHttpClientAdapter();

        BatchEmitter emitter = BatchEmitter.builder()
                .httpClientAdapter(mockHttpClientAdapter)
                .build();

        Tracker tracker = new Tracker.TrackerBuilder(emitter, "namespace", "appId").build();

        PageView pageViewEvent = PageView.builder()
                .pageUrl("url")
                .pageTitle("title")
                .referrer("referrer")
                .build();

        @Setup
        public void doSetUp() {
            System.out.println("Using tracker version: " + tracker.getTrackerVersion());
        }

        @TearDown(Level.Trial)
        public void doTearDown() {
            emitter.close();

            System.out.println("Do TearDown");
            System.out.println("This many POST requests made: " + mockHttpClientAdapter.getPostCount());
        }
    }

    @Benchmark
    public void testTrackEvent(Blackhole blackhole, TrackerComponents trackerComponents) {
        trackerComponents.tracker.track(trackerComponents.pageViewEvent);
        blackhole.consume(trackerComponents);
    }
}
