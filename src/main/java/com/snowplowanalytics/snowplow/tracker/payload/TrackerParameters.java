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
package com.snowplowanalytics.snowplow.tracker.payload;

import com.snowplowanalytics.snowplow.tracker.DevicePlatform;

public class TrackerParameters {

    private final String trackerVersion;
    private final String appId;
    private final DevicePlatform platform;
    private final String namespace;
    private final boolean base64Encoded;

    public TrackerParameters(String appId, DevicePlatform platform, String namespace, String trackerVersion,
            boolean base64Encoded) {
        this.appId = appId;
        this.platform = platform;
        this.namespace = namespace;
        this.trackerVersion = trackerVersion;
        this.base64Encoded = base64Encoded;
    }

    public boolean getBase64Encoded() {
        return base64Encoded;
    }

    public String getTrackerVersion() {
        return trackerVersion;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getAppId() {
        return appId;
    }

    public DevicePlatform getPlatform() {
        return platform;
    }
}
