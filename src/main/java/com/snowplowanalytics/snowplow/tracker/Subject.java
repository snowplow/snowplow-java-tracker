/*
 * Copyright (c) 2014-present Snowplow Analytics Ltd. All rights reserved.
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
import com.snowplowanalytics.snowplow.tracker.configuration.SubjectConfiguration;
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;

/**
 * An object for managing extra event decoration.
 * All the properties are optional. However, the timezone is set by default,
 * to that of the server.
 */
public class Subject {

    private HashMap<String, String> standardPairs = new HashMap<>();

    /**
     * Creates a Subject instance from a SubjectConfiguration.
     *
     * @param subjectConfig a SubjectConfiguration
     */
    public Subject(SubjectConfiguration subjectConfig) {
        setUserId(subjectConfig.getUserId());
        setScreenResolution(subjectConfig.getScreenResWidth(), subjectConfig.getScreenResHeight());
        setViewPort(subjectConfig.getViewPortWidth(), subjectConfig.getViewPortHeight());
        setColorDepth(subjectConfig.getColorDepth());
        setTimezone(subjectConfig.getTimezone());
        setLanguage(subjectConfig.getLanguage());
        setIpAddress(subjectConfig.getIpAddress());
        setUseragent(subjectConfig.getUseragent());
        setNetworkUserId(subjectConfig.getNetworkUserId());
        setDomainUserId(subjectConfig.getDomainUserId());
        setDomainSessionId(subjectConfig.getDomainSessionId());
    }

    /**
     * Creates a Subject instance with default configuration (only the timezone is set).
     */
    public Subject() {
        this(new SubjectConfiguration());
    }

    /**
     * Creates a new {@link Subject} object based on the map of another {@link Subject} object.
     * @param subject The subject from which the map is copied.
     */
    public Subject(Subject subject){
        standardPairs.putAll(subject.getSubject());
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
     * Sets the User ID and returns itself
     *
     * @param userId a user id string
     * @return itself
     */
    public Subject userId(String userId) {
        this.setUserId(userId);
        return this;
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
     * Sets the screen res parameter and returns itself
     *
     * @param width a width integer
     * @param height a height integer
     * @return itself
     */
    public Subject screenResolution(int width, int height) {
        this.setScreenResolution(width, height);
        return this;
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
     * Sets the view port parameter and returns itself
     *
     * @param width a width integer
     * @param height a height integer
     * @return itself
     */
    public Subject viewPort(int width, int height) {
        this.setViewPort(width, height);
        return this;
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
     * Sets the color depth parameter and returns itself
     *
     * @param depth a color depth integer
     * @return itself
     */
    public Subject colorDepth(int depth) {
        this.setColorDepth(depth);
        return this;
    }

    /**
     * Sets the timezone parameter. Note that timezone is set by default to the server's timezone
     * (`TimeZone tz = Calendar.getInstance().getTimeZone().getID()`);
     *
     * @param timezone a timezone string
     */
    public void setTimezone(String timezone) {
        if (timezone != null) {
            this.standardPairs.put(Parameter.TIMEZONE, timezone);
        }
    }

    /**
     * Sets the timezone parameter and returns itself.
     * Note that timezone is set by default to the server's timezone
     * (`TimeZone tz = Calendar.getInstance().getTimeZone().getID()`)
     * 
     * @param timezone a timezone string
     * @return itself
     */
    public Subject timezone(String timezone) {
        this.setTimezone(timezone);
        return this;
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
     * Sets the language parameter and returns itself
     *
     * @param language a language string
     * @return itself
     */
    public Subject language(String language) {
        this.setLanguage(language);
        return this;
    }

    /**
     * User inputted ip address for the subject.
     *
     * @param ipAddress an ip address
     */
    public void setIpAddress(String ipAddress) {
        if (ipAddress != null) {
            this.standardPairs.put(Parameter.IP_ADDRESS, ipAddress);
        }
    }

    /**
     * Sets the user inputted ip address for the subject and returns itself
     *
     * @param ipAddress a ipAddress string
     * @return itself
     */
    public Subject ipAddress(String ipAddress) {
        this.setIpAddress(ipAddress);
        return this;
    }

    /**
     * User inputted useragent for the subject.
     *
     * @param useragent a useragent
     */
    public void setUseragent(String useragent) {
        if (useragent != null) {
            this.standardPairs.put(Parameter.USERAGENT, useragent);
        }
    }

    /**
     * Sets the user inputted useragent for the subject and returns itself
     *
     * @param useragent a useragent string
     * @return itself
     */
    public Subject useragent(String useragent) {
        this.setUseragent(useragent);
        return this;
    }

    /**
     * User inputted Domain User Id for the subject.
     *
     * @param domainUserId a domain user id
     */
    public void setDomainUserId(String domainUserId) {
        if (domainUserId != null) {
            this.standardPairs.put(Parameter.DOMAIN_UID, domainUserId);
        }
    }

    /**
     * Sets the user inputted Domain User Id for the subject and returns itself
     *
     * @param domainUserId a domainUserId string
     * @return itself
     */
    public Subject domainUserId(String domainUserId) {
        this.setDomainUserId(domainUserId);
        return this;
    }

    /**
     * User inputted Domain Session ID for the subject.
     *
     * @param domainSessionId a domain session id
     */
    public void setDomainSessionId(String domainSessionId) {
        if (domainSessionId != null) {
            this.standardPairs.put(Parameter.SESSION_UID, domainSessionId);
        }
    }

    /**
     * Sets the user inputted Domain Session ID for the subject and returns itself
     *
     * @param domainSessionId a domainSessionId string
     * @return itself
     */
    public Subject domainSessionId(String domainSessionId) {
        this.setDomainSessionId(domainSessionId);
        return this;
    }

    /**
     * User inputted Network User ID for the subject.
     * This overrides the network user ID set by the Collector in response Cookies.
     *
     * @param networkUserId a network user id
     */
    public void setNetworkUserId(String networkUserId) {
        if (networkUserId != null) {
            this.standardPairs.put(Parameter.NETWORK_UID, networkUserId);
        }
    }

    /**
     * Sets the user inputted Network User ID for the subject and returns itself.
     * This overrides the network user ID set by the Collector in response Cookies.
     *
     * @param networkUserId a networkUserId string
     * @return itself
     */
    public Subject networkUserId(String networkUserId) {
        this.setNetworkUserId(networkUserId);
        return this;
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
