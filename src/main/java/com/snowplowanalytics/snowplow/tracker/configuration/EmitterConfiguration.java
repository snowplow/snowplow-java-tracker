/*
 * Copyright (c) 2014-2022 Snowplow Analytics Ltd. All rights reserved.
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
package com.snowplowanalytics.snowplow.tracker.configuration;

import com.snowplowanalytics.snowplow.tracker.emitter.EventStore;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class EmitterConfiguration {

    private int batchSize; // Optional
    private int bufferCapacity; // Optional
    private EventStore eventStore;  // Optional
    private List<Integer> fatalResponseCodes;  // Optional
    private int threadCount; // Optional
    private ScheduledExecutorService requestExecutorService; // Optional

    // Getters and Setters

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getBufferCapacity() {
        return bufferCapacity;
    }

    public void setBufferCapacity(int bufferCapacity) {
        this.bufferCapacity = bufferCapacity;
    }

    public EventStore getEventStore() {
        return eventStore;
    }

    public void setEventStore(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public List<Integer> getFatalResponseCodes() {
        return fatalResponseCodes;
    }

    public void setFatalResponseCodes(List<Integer> fatalResponseCodes) {
        this.fatalResponseCodes = fatalResponseCodes;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public ScheduledExecutorService getRequestExecutorService() {
        return requestExecutorService;
    }

    public void setRequestExecutorService(ScheduledExecutorService requestExecutorService) {
        this.requestExecutorService = requestExecutorService;
    }

    // Constructor

    public EmitterConfiguration() {
        batchSize = 50;
        bufferCapacity = Integer.MAX_VALUE;
        eventStore = null;
        fatalResponseCodes = null;
        threadCount = 50;
        requestExecutorService = null;
    }

    // Builder methods

    public EmitterConfiguration batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public EmitterConfiguration bufferCapacity(int bufferCapacity) {
        this.bufferCapacity = bufferCapacity;
        return this;
    }

    public EmitterConfiguration eventStore(EventStore eventStore) {
        this.eventStore = eventStore;
        return this;
    }

    public EmitterConfiguration fatalResponseCodes(List<Integer> fatalResponseCodes) {
        this.fatalResponseCodes = fatalResponseCodes;
        return this;
    }

    public EmitterConfiguration threadCount(int threadCount) {
        this.threadCount = threadCount;
        return this;
    }

    public EmitterConfiguration requestExecutorService(ScheduledExecutorService requestExecutorService) {
        this.requestExecutorService = requestExecutorService;
        return this;
    }
}
