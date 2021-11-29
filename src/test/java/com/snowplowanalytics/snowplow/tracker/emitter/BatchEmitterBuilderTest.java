/*
 * Copyright (c) 2014-2020 Snowplow Analytics Ltd. All rights reserved.
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
package com.snowplowanalytics.snowplow.tracker.emitter;

import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import org.junit.Assert;
import org.junit.Test;

public class BatchEmitterBuilderTest {

    @Test
    public void setNeitherHttpClientAdapterOrCollectorUrl_shouldThrowException() {
        Exception exception = Assert.assertThrows(Exception.class, () -> BatchEmitter.builder().build());
        Assert.assertEquals("Collector url must be specified if not using a httpClientAdapter", exception.getMessage());
    }

    @Test
    public void setCollectorUrlAndNoHttpClientAdapter_shouldInitialiseCorrectly() {
        BatchEmitter emitter = BatchEmitter.builder().url("https://mycollector.com").build();
        Assert.assertNotNull(emitter);
    }

    @Test
    public void setHttpClientAdapterAndNoCollectorUrl_shouldInitialiseCorrectly() {
        HttpClientAdapter mockHttpClientAdapter = new HttpClientAdapter() {
            @Override
            public int post(SelfDescribingJson payload) {
                return 0;
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
        };

        BatchEmitter emitter = BatchEmitter.builder().httpClientAdapter(mockHttpClientAdapter).build();
        Assert.assertNotNull(emitter);
    }
}
