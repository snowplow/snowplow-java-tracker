package com.snowplowanalytics.snowplow.tracker.emitter;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.snowplowanalytics.snowplow.tracker.Constants;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.SchemaPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BatchEmitter extends AbstractEmitter  {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchEmitter.class);

    private Integer bufferSize = 10;

    private List<Map<String,Object>> buffer = new ArrayList<Map<String,Object>>();

    public BatchEmitter(HttpClientAdapter httpClientAdapter) {
        super(httpClientAdapter);
    }

    @Override
    public void emit(Map<String, Object> payload) {
        buffer.add(payload);
        if (buffer.size() >= bufferSize) {
            flushBuffer();
        }
    }

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
        final int statusCode = httpClientAdapter.post(selfDescribedJson);
        if (statusCode != 200) {
            throw new RuntimeException(String.format("Failed to emit event, got status %d", statusCode));
        }
    }

    public void setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
    }

    @VisibleForTesting
    public List<Map<String, Object>> getBuffer() {
        return buffer;
    }
}

