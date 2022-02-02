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
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.events.PageView;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 15, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 20, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(5)
public class TrackerBenchmark {
    public static class MockHttpClientAdapter implements HttpClientAdapter {
        @Override
        public int post(SelfDescribingJson payload) {
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

    public static BatchEmitter getEmitter() {
        MockHttpClientAdapter mockHttpClientAdapter = new MockHttpClientAdapter();
        return BatchEmitter.builder()
                .httpClientAdapter(mockHttpClientAdapter)
                .build();
    }

    public static Tracker getTracker(Emitter emitter) {
        return new Tracker.TrackerBuilder(emitter, "namespace", "appId").build();
    }

    public static void closeThreads(Tracker tracker) {
        // Use this line for versions 0.12.0 onwards
//        tracker.close();
        // Use these lines for previous versions
        BatchEmitter emitter = (BatchEmitter) tracker.getEmitter();
        emitter.close();
    }

    // This State class exists only to print out the tracker version
    @State(Scope.Benchmark)
    public static class TrackerVersion {
        BatchEmitter emitter = getEmitter();
        Tracker tracker = getTracker(emitter);

        @Setup(Level.Trial)
        public void printTrackerVersion() {
            System.out.println("Using tracker version: " + tracker.getTrackerVersion());
        }

        @TearDown(Level.Trial)
        public void doTearDown() {
            System.out.println("Do TearDown for trackerVersion state");
            closeThreads(tracker);
        }
    }

    // This class creates the tracker components.
    // They are recreated for every iteration of the benchmark test.
    @State(Scope.Benchmark)
    public static class TrackerComponents {
        Tracker tracker;
        BatchEmitter emitter;

        PageView pageViewEvent = PageView.builder()
                .pageUrl("url")
                .pageTitle("title")
                .referrer("referrer")
                .build();

        @Setup(Level.Iteration)
        public void doSetUp() {
            emitter = getEmitter();
            tracker = getTracker(emitter);
        }

        @TearDown(Level.Iteration)
        public void doTearDown() {
            closeThreads(tracker);
        }
    }

    // The Blackhole forces JMH to measure the method.
    @Benchmark
    public void testTrackEvent(Blackhole blackhole, TrackerComponents trackerComponents, TrackerVersion trackerVersion) {
        trackerComponents.tracker.track(trackerComponents.pageViewEvent);
        blackhole.consume(trackerComponents);
    }
}
