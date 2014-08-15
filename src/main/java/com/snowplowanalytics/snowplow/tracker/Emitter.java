/*
 * Copyright (c) 2014 Snowplow Analytics Ltd. All rights reserved.
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

package com.snowplowanalytics.snowplow.tracker;

import com.snowplowanalytics.snowplow.tracker.core.emitter.HttpMethod;
import com.snowplowanalytics.snowplow.tracker.core.emitter.RequestCallback;

public class Emitter extends com.snowplowanalytics.snowplow.tracker.core.emitter.Emitter {
    public Emitter(String URI) {
        super(URI);
    }

    public Emitter(String URI, RequestCallback callback) {
        super(URI, callback);
    }

    public Emitter(String URI, HttpMethod httpMethod) {
        super(URI, httpMethod);
    }

    public Emitter(String URI, HttpMethod httpMethod, RequestCallback callback) {
        super(URI, httpMethod, callback);
    }
}
