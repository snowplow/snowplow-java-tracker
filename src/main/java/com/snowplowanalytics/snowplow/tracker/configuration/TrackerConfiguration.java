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

import com.snowplowanalytics.snowplow.tracker.DevicePlatform;


public class TrackerConfiguration {
    private final String namespace; // Required
    private final String appId; // Required
    private DevicePlatform platform; // Optional
    private boolean base64Encoded; // Optional

    // Getters and Setters

    public String getNamespace() {
        return namespace;
    }

    public String getAppId() {
        return appId;
    }

    public DevicePlatform getPlatform() {
        return platform;
    }

    public boolean isBase64Encoded() {
        return base64Encoded;
    }

    // Constructor

    /**
     * Create a TrackerConfiguration instance. The namespace is the unique identifier for the instance.
     * By default, the platform is ServerSideApp, and JSONs will be base64 encoded.
     *
     * @param namespace identifier for the Tracker instance
     * @param appId application ID
     */
    public TrackerConfiguration(String namespace, String appId) {
        this.namespace = namespace;
        this.appId = appId;
        this.platform = DevicePlatform.ServerSideApp;
        this.base64Encoded = true;
    }

    // Builder methods

    /**
     * The {@link DevicePlatform} the tracker is running on (default is "srv", ServerSideApp).
     *
     * @param platform The device platform the tracker is running on
     * @return itself
     */
    public TrackerConfiguration platform(DevicePlatform platform) {
        this.platform = platform;
        return this;
    }

    /**
     * Whether JSONs in the payload should be base-64 encoded (default is true)
     *
     * @param base64Encoded JSONs should be encoded or not
     * @return itself
     */
    public TrackerConfiguration base64Encoded(boolean base64Encoded) {
        this.base64Encoded = base64Encoded;
        return this;
    }
}
