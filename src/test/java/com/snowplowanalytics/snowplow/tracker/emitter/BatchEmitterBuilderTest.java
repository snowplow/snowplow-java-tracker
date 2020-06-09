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

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.mockito.Mockito.*;

import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;

public class BatchEmitterBuilderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void setNeitherHttpClientAdapterOrCollectorUrl_shouldThrowException() throws Exception {
        expectedException.expect(Exception.class);
        BatchEmitter.builder().build();
    }

    @Test
    public void setCollectorUrlAndNoHttpClientAdapter_shouldInitialiseCorrectly() throws Exception {
        BatchEmitter emitter = BatchEmitter.builder().url("https://mycollector.com").build();
        Assert.assertNotNull(emitter);
    }

    @Test
    public void setHttpClientAdapterAndNoCollectorUrl_shouldInitialiseCorrectly() throws Exception {
        HttpClientAdapter httpClientAdapter = mock(HttpClientAdapter.class);
        BatchEmitter emitter = BatchEmitter.builder().httpClientAdapter(httpClientAdapter).build();
        Assert.assertNotNull(emitter);
    }
}
