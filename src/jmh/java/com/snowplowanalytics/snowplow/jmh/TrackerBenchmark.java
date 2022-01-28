package com.snowplowanalytics.snowplow.jmh;

import com.snowplowanalytics.snowplow.tracker.Subject;
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
public class TrackerBenchmark {

    @State(Scope.Benchmark)
    public static class trackerComponents {
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
            tracker.close();
            System.out.println("Do TearDown");
            System.out.println("This many POST requests made: " + mockHttpClientAdapter.getPostCount());
        }
    }

    @Benchmark
    public void testTrackEvent(Blackhole blackhole, trackerComponents trackerComponents) {
        trackerComponents.tracker.track(trackerComponents.pageViewEvent);
        blackhole.consume(trackerComponents);
    }
}
