package com.snowplowanalytics.snowplow.jmh;

import com.snowplowanalytics.snowplow.tracker.Subject;
import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.emitter.BatchEmitter;
import com.snowplowanalytics.snowplow.tracker.events.PageView;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
public class TrackerBenchmark {
    Subject subject = new Subject.SubjectBuilder().build();
    PageView pageViewEvent = PageView.builder()
            .pageUrl("url")
            .pageTitle("title")
            .referrer("referer")
            .build();

    BatchEmitter emitter = BatchEmitter.builder()
            .url("http://localhost:9090")
            .build();

    Tracker tracker = new Tracker.TrackerBuilder(emitter, "namespace", "appId").build();

//    @Benchmark
//    public void testSetUserId(Blackhole blackhole) {
//        subject.setUserId("hello");
//        blackhole.consume(subject);
//    }

    @Benchmark
    public void testTrackEvent(Blackhole blackhole) {
        tracker.track(pageViewEvent);
        tracker.close();
        blackhole.consume(tracker);
    }

}
