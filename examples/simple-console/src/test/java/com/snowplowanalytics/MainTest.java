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

package com.snowplowanalytics;

import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MainTest {

    @Test
    public void testGetUrl() {
        String[] sample = {"com.acme", "world"};
        assertEquals("com.acme", Main.getUrlFromArgs(sample));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUrlNull() {
        Main.getUrlFromArgs(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUrlEmpty() {
        Main.getUrlFromArgs(new String[]{});
    }

}
