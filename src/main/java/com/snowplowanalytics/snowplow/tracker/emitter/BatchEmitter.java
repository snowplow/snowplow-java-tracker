package com.snowplowanalytics.snowplow.tracker.emitter;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.snowplowanalytics.snowplow.tracker.Constants;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An emitter that emit a batch of events in a single call
 * It uses the post method of under-laying http adapter
 */
public class BatchEmitter extends AbstractEmitter implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchEmitter.class);

    private int bufferSize = 10;

    private List<Map<String,Object>> buffer = new ArrayList<Map<String,Object>>();

    public BatchEmitter(HttpClientAdapter httpClientAdapter) {
        super(httpClientAdapter);
    }

    @Override
    public synchronized void emit(Map<String, Object> payload) {
        buffer.add(payload);
        if (buffer.size() >= bufferSize) {
            flushBuffer();
        }
    }

    /**
     * Flush buffer when buffer
     * reaches com.snowplowanalytics.snowplow.tracker.emitter.BatchEmitter#bufferSize
     */
    void flushBuffer() {
        if (buffer.isEmpty()) {
            LOGGER.debug("Buffer is empty, exiting flush operation..");
            return;
        }

        final List<Map<String, Object>> toSendPayloads = Lists.newArrayList(buffer);
        buffer.clear();

        final SchemaPayload selfDescribedJson = new SchemaPayload();
        selfDescribedJson.setSchema(Constants.SCHEMA_PAYLOAD_DATA);
        selfDescribedJson.setData(toSendPayloads);

        try {
            httpClientAdapter.post(selfDescribedJson);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to emit %d events", toSendPayloads.size()));
        }
    }

    @Override
    public void close() {
        flushBuffer();
    }

    /**
     * Customize the bxzzuffer sizxe
     *
     * @param bufferSize number of events to collect
     */
    public void setBufferSize(int bufferSize) {
        Preconditions.checkArgument(bufferSize > 0);
        this.bufferSize = bufferSize;
    }

    @VisibleForTesting
    public List<Map<String, Object>> getBuffer() {
        return buffer;
    }
}

