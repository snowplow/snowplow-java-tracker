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

package com.snowplowanalytics.snowplow.tracker.core;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class Subject {

    private HashMap<String, String> standardPairs;

    public Subject() {
        standardPairs = new HashMap<String, String>();

        // Default Platform
        this.setPlatform(DevicePlatform.Desktop);

        // Default Timezone
        TimeZone tz = Calendar.getInstance().getTimeZone();
        this.setTimezone(tz.getID());
    }

    public void setPlatform(DevicePlatform platform) {
        this.standardPairs.put(Parameter.PLATFORM, platform.toString());
    }

    public void setUserId(String userId) {
        this.standardPairs.put(Parameter.UID, userId);
    }

    public void setScreenResolution(int width, int height) {
        String res = Integer.toString(width) + "x" + Integer.toString(height);
        this.standardPairs.put(Parameter.RESOLUTION, res);
    }

    public void setViewPort(int width, int height) {
        String res = Integer.toString(width) + "x" + Integer.toString(height);
        this.standardPairs.put(Parameter.VIEWPORT, res);
    }

    public void setColorDepth(int depth) {
        this.standardPairs.put(Parameter.COLOR_DEPTH, Integer.toString(depth));
    }

    public void setTimezone(String timezone) {
        this.standardPairs.put(Parameter.TIMEZONE, timezone);
    }

    public void setLanguage(String language) {
        this.standardPairs.put(Parameter.LANGUAGE, language);
    }

    public Map<String, String> getSubject() {
        return this.standardPairs;
    }
}
