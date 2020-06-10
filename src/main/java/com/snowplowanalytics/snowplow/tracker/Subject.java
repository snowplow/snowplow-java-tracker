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
     * Creates a Subject which will add extra data to each event.
     *
     * @param builder The builder that constructs a subject
     */
    private Subject(SubjectBuilder builder) {
        this.setUserId(builder.userId);
        this.setScreenResolution(builder.screenResWidth, builder.screenResHeight);
        this.setViewPort(builder.viewPortWidth, builder.viewPortHeight);
        this.setColorDepth(builder.colorDepth);
        this.setTimezone(builder.timezone);
        this.setLanguage(builder.language);
        this.setIpAddress(builder.ipAddress);
        this.setUseragent(builder.useragent);
        this.setNetworkUserId(builder.networkUserId);
        this.setDomainUserId(builder.domainUserId);
    }

    /**
     * Creates a new {@link Subject} object based on the map of another {@link Subject} object.
     * @param subject The subject from which the map is copied.
     */
    public Subject(Subject subject){
      this.standardPairs.putAll(subject.getSubject());
    }

    /**
     * Builder for the Subject
     */
    public static class SubjectBuilder {

        private String userId; // Optional
        private int screenResWidth = 0; // Optional
        private int screenResHeight = 0; // Optional
        private int viewPortWidth = 0; // Optional
        private int viewPortHeight = 0; // Optional
        private int colorDepth = 0; // Optional
        private String timezone = Utils.getTimezone(); // Optional
        private String language; // Optional
        private String ipAddress; // Optional
        private String useragent; // Optional
        private String networkUserId; // Optional
        private String domainUserId; // Optional

        /**
         * @param userId a user id string
         * @return itself
         */
        public SubjectBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        /**
         * @param width a width integer
         * @param height a height integer
         * @return itself
         */
        public SubjectBuilder screenResolution(int width, int height) {
            this.screenResWidth = width;
            this.screenResHeight = height;
            return this;
        }

        /**
         * @param width a width integer
         * @param height a height integer
         * @return itself
         */
        public SubjectBuilder viewPort(int width, int height) {
            this.viewPortWidth = width;
            this.viewPortHeight = height;
            return this;
        }

        /**
         * @param depth a color depth integer
         * @return itself
         */
        public SubjectBuilder colorDepth(int depth) {
            this.colorDepth = depth;
            return this;
        }

        /**
         * @param timezone a timezone string
         * @return itself
         */
        public SubjectBuilder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        /**
         * @param language a language string
         * @return itself
         */
        public SubjectBuilder language(String language) {
            this.language = language;
            return this;
        }

        /**
         * @param ipAddress a ipAddress string
         * @return itself
         */
        public SubjectBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        /**
         * @param useragent a useragent string
         * @return itself
         */
        public SubjectBuilder useragent(String useragent) {
            this.useragent = useragent;
            return this;
        }

        /**
         * @param networkUserId a networkUserId string
         * @return itself
         */
        public SubjectBuilder networkUserId(String networkUserId) {
            this.networkUserId = networkUserId;
            return this;
        }

        /**
         * @param domainUserId a domainUserId string
         * @return itself
         */
        public SubjectBuilder domainUserId(String domainUserId) {
            this.domainUserId = domainUserId;
            return this;
        }

        /**
         * Creates a new Subject
         *
         * @return a new Subject object
         */
        public Subject build() {
            return new Subject(this);
        }
    }

    /**
     * Sets the User ID
     *
     * @param userId a user id string
     */
    public void setUserId(String userId) {
        if (userId != null) {
            this.standardPairs.put(Parameter.UID, userId);
        }
    }

    /**
     * Sets the screen res parameter
     *
     * @param width a width integer
     * @param height a height integer
     */
    public void setScreenResolution(int width, int height) {
        if (width > 0 && height > 0) {
            String res = Integer.toString(width) + "x" + Integer.toString(height);
            this.standardPairs.put(Parameter.RESOLUTION, res);
        }
    }

    /**
     * Sets the view port parameter
     *
     * @param width a width integer
     * @param height a height integer
     */
    public void setViewPort(int width, int height) {
        if (width > 0 && height > 0) {
            String res = Integer.toString(width) + "x" + Integer.toString(height);
            this.standardPairs.put(Parameter.VIEWPORT, res);
        }
    }

    /**
     * Sets the color depth parameter
     *
     * @param depth a color depth integer
     */
    public void setColorDepth(int depth) {
        if (depth > 0) {
            this.standardPairs.put(Parameter.COLOR_DEPTH, Integer.toString(depth));
        }
    }

    /**
     * Sets the timezone parameter
     *
     * @param timezone a timezone string
     */
    public void setTimezone(String timezone) {
        if (timezone != null) {
            this.standardPairs.put(Parameter.TIMEZONE, timezone);
        }
    }

    /**
     * Sets the language parameter
     *
     * @param language a language string
     */
    public void setLanguage(String language) {
        if (language != null) {
            this.standardPairs.put(Parameter.LANGUAGE, language);
        }
    }

    /**
     * User inputted ip address for the
     * subject.
     *
     * @param ipAddress an ip address
     */
    public void setIpAddress(String ipAddress) {
        if (ipAddress != null) {
            this.standardPairs.put(Parameter.IP_ADDRESS, ipAddress);
        }
    }

    /**
     * User inputted useragent for the
     * subject.
     *
     * @param useragent a useragent
     */
    public void setUseragent(String useragent) {
        if (useragent != null) {
            this.standardPairs.put(Parameter.USERAGENT, useragent);
        }
    }

    /**
     * User inputted Domain User Id for the
     * subject.
     *
     * @param domainUserId a domain user id
     */
    public void setDomainUserId(String domainUserId) {
        if (domainUserId != null) {
            this.standardPairs.put(Parameter.DOMAIN_UID, domainUserId);
        }
    }

    /**
     * User inputted Network User Id for the
     * subject.
     *
     * @param networkUserId a network user id
     */
    public void setNetworkUserId(String networkUserId) {
        if (networkUserId != null) {
            this.standardPairs.put(Parameter.NETWORK_UID, networkUserId);
        }
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
