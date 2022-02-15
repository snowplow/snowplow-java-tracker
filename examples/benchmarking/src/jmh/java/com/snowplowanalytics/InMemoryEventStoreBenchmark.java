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

import com.snowplowanalytics.snowplow.tracker.events.PageView;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@State(Scope.Thread)
@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 20, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
public class InMemoryEventStoreBenchmark {

    static TrackerPayload payload = BenchmarkHelpers.event.getPayload();

    static List<TrackerPayload> payloads = getTenPayloads();

    public static List<TrackerPayload> getTenPayloads() {
        ArrayList<TrackerPayload> payloads = new ArrayList<>();
        payloads.add(payload);
        return payloads;
    }


    @State(Scope.Benchmark)
    public static class BufferComponents {
        ConcurrentLinkedDeque<TrackerPayload> ConcurrentLinkedDeque;
        ConcurrentLinkedQueue<TrackerPayload> ConcurrentLinkedQueue;
        LinkedBlockingDeque<TrackerPayload> LinkedBlockingDeque;
        LinkedBlockingQueue<TrackerPayload> LinkedBlockingQueue;
        List<TrackerPayload> events;

        @Setup(Level.Iteration)
        public void doSetUp() {
            ConcurrentLinkedDeque = new ConcurrentLinkedDeque<>();
            ConcurrentLinkedQueue = new ConcurrentLinkedQueue<>();
            LinkedBlockingDeque = new LinkedBlockingDeque<>();
            LinkedBlockingQueue = new LinkedBlockingQueue<>();

            ConcurrentLinkedDeque.addAll(payloads);
            ConcurrentLinkedQueue.addAll(payloads);
            LinkedBlockingDeque.addAll(payloads);
            LinkedBlockingQueue.addAll(payloads);

            events = new ArrayList<>();
        }
    }

//    @Benchmark
//    public List<TrackerPayload> testGetFromConcurrentLinkedDeque(BufferComponents bufferComponents) {
//        for (int i = 0; i < 5; i++) {
//            TrackerPayload payload = bufferComponents.ConcurrentLinkedDeque.poll();
//            if (payload == null) {
//                break;
//            }
//            bufferComponents.events.add(payload);
//        }
//        return bufferComponents.events;
//    }
//
//    @Benchmark
//    public List<TrackerPayload> testGetFromConcurrentLinkedQueue(BufferComponents bufferComponents) {
//        for (int i = 0; i < 5; i++) {
//            TrackerPayload payload = bufferComponents.ConcurrentLinkedQueue.poll();
//            if (payload == null) {
//                break;
//            }
//            bufferComponents.events.add(payload);
//        }
//        return bufferComponents.events;
//    }
//
//    @Benchmark
//    public List<TrackerPayload> testGetFromLinkedBlockingQueue(BufferComponents bufferComponents) {
//        bufferComponents.LinkedBlockingQueue.drainTo(bufferComponents.events, 5);
//        return bufferComponents.events;
//    }
//
//    @Benchmark
//    public List<TrackerPayload> testGetFromLinkedBlockingDeque(BufferComponents bufferComponents) {
//        bufferComponents.LinkedBlockingDeque.drainTo(bufferComponents.events, 5);
//        return bufferComponents.events;
//    }

    @Benchmark
    public void testInsertLinkedBlockingDequeTail(BufferComponents bufferComponents, Blackhole blackhole) {
        bufferComponents.LinkedBlockingDeque.addAll(payloads);
        blackhole.consume(bufferComponents);
    }

    @Benchmark
    public void testInsertLinkedBlockingQueueTail(BufferComponents bufferComponents, Blackhole blackhole) {
        bufferComponents.LinkedBlockingQueue.addAll(payloads);
        blackhole.consume(bufferComponents);
    }

    @Benchmark
    public void testInsertConcurrentLinkedQueueTail(BufferComponents bufferComponents, Blackhole blackhole) {
        bufferComponents.ConcurrentLinkedQueue.addAll(payloads);
        blackhole.consume(bufferComponents);
    }

    @Benchmark
    public void testInsertConcurrentLinkedDequeTail(BufferComponents bufferComponents, Blackhole blackhole) {
        bufferComponents.ConcurrentLinkedDeque.addAll(payloads);
        blackhole.consume(bufferComponents);
    }

    @Benchmark
    public void testInsertConcurrentLinkedDequeHead(BufferComponents bufferComponents, Blackhole blackhole) {
        for (TrackerPayload payload : payloads) {
            bufferComponents.ConcurrentLinkedDeque.addFirst(payload);
        }
        blackhole.consume(bufferComponents);
    }

    @Benchmark
    public void testInsertLinkedBlockingDequeHead(BufferComponents bufferComponents, Blackhole blackhole) {
        for (TrackerPayload payload : payloads) {
            bufferComponents.LinkedBlockingDeque.addFirst(payload);
        }
        blackhole.consume(bufferComponents);
    }
}
