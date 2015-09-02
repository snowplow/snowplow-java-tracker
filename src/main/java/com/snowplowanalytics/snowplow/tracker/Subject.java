/*
 * Copyright (c) 2015 Snowplow Analytics Ltd. All rights reserved.
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

// Java
import java.util.HashMap;
import java.util.Map;

// This library
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;

/**
 * An object for managing extra event decoration.
 */
public class Subject {

    private HashMap<String, String> standardPairs = new HashMap<>();

    /**
     * Builds a new Subject object and sets:
     * - the default timezone
     */
    public Subject() {
        this.setTimezone(Utils.getTimezone());
    }

    /**
     * Sets the User ID
     *
     * @param userId a user id string
     */
    public void setUserId(String userId) {
        this.standardPairs.put(Parameter.UID, userId);
    }

    /**
     * Sets the screen res parameter
     *
     * @param width a width integer
     * @param height a height integer
     */
    public void setScreenResolution(int width, int height) {
        String res = Integer.toString(width) + "x" + Integer.toString(height);
        this.standardPairs.put(Parameter.RESOLUTION, res);
    }

    /**
     * Sets the view port parameter
     *
     * @param width a width integer
     * @param height a height integer
     */
    public void setViewPort(int width, int height) {
        String res = Integer.toString(width) + "x" + Integer.toString(height);
        this.standardPairs.put(Parameter.VIEWPORT, res);
    }

    /**
     * Sets the color depth parameter
     *
     * @param depth a color depth integer
     */
    public void setColorDepth(int depth) {
        this.standardPairs.put(Parameter.COLOR_DEPTH, Integer.toString(depth));
    }

    /**
     * Sets the timezone parameter
     *
     * @param timezone a timezone string
     */
    public void setTimezone(String timezone) {
        this.standardPairs.put(Parameter.TIMEZONE, timezone);
    }

    /**
     * Sets the language parameter
     *
     * @param language a language string
     */
    public void setLanguage(String language) {
        this.standardPairs.put(Parameter.LANGUAGE, language);
    }

    /**
     * Gets the Subject pairs.
     *
     * @return the stored k-v pairs
     */
    public Map<String, String> getSubject() {
        return this.standardPairs;
    }
}
