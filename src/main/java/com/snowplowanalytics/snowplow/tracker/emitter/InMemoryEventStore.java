package com.snowplowanalytics.snowplow.tracker.emitter;

import com.snowplowanalytics.snowplow.tracker.payload.TrackerEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryEventStore implements EventStore {
    private static final AtomicInteger BUFFER_CONSUMER_THREAD_NUMBER = new AtomicInteger(1);
    private static final String BUFFER_CONSUMER_THREAD_NAME_PREFIX = "snowplow-eventStore-BufferConsumer-thread-";

    private final Thread bufferConsumer;
    // Queue for immediate buffering of events
    public final BlockingQueue<TrackerEvent> eventInitialBuffer = new LinkedBlockingQueue<>();

    // Queue for storing events until bufferSize is reached
    public final BlockingQueue<TrackerEvent> eventStagingBuffer = new LinkedBlockingQueue<>();

    protected InMemoryEventStore() {
        bufferConsumer = new Thread(
                getBufferConsumerRunnable(),
                BUFFER_CONSUMER_THREAD_NAME_PREFIX + BUFFER_CONSUMER_THREAD_NUMBER.getAndIncrement()
        );
        bufferConsumer.start();
    }

    public BlockingQueue<TrackerEvent> getInitialEventBuffer() {
        return eventInitialBuffer;
    }

    public BlockingQueue<TrackerEvent> getEventStagingBuffer() {
        return eventStagingBuffer;
    }



    @Override
    public boolean add(TrackerEvent trackerEvent) {
        return eventInitialBuffer.offer(trackerEvent);
    }

    @Override
    public void removeAllEvents(List<TrackerEvent> eventsList) {
        eventStagingBuffer.drainTo(eventsList);
    }

    @Override
    public long getSize() {
        return eventStagingBuffer.size();
    }

    /**
     * Returns a Consumer for the concurrent queue buffer
     * Consumes events onto another queue to be sent when bufferSize is reached
     *
     * @return the new Runnable object
     */
    private Runnable getBufferConsumerRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                System.out.println("hello from " + Thread.currentThread().getName());
                while (true) {
                    try {
                        eventStagingBuffer.put(eventInitialBuffer.take());
                    } catch (InterruptedException ex) {
//                        if (isClosing) {
//                            return;
//                        }
                        // right now it never stops! isClosing is part of BatchEmitter
                        System.out.println("there was an exception in BufferConsumer");
                    }
                }
            }
        };
    }
}
