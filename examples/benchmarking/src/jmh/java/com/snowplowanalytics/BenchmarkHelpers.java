package com.snowplowanalytics;

import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.emitter.BatchEmitter;
import com.snowplowanalytics.snowplow.tracker.events.PageView;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

public class BenchmarkHelpers {
    static PageView event = PageView.builder()
            .pageUrl("https://www.snowplowanalytics.com/")
            .pageTitle("Snowplow")
            .build();

    static TrackerPayload payload = event.getPayload();

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

    static MockHttpClientAdapter mockHttpClientAdapter = new MockHttpClientAdapter();

    static BatchEmitter emitter = BatchEmitter.builder()
            .httpClientAdapter(mockHttpClientAdapter)
            .bufferSize(5)
            .build();

    static Tracker tracker = new Tracker.TrackerBuilder(emitter, "namespace", "appId").build();
}
