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

import com.snowplowanalytics.snowplow.tracker.Utils;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;

public class SubjectConfiguration {

    private String userId; // Optional
    private int screenResWidth; // Optional
    private int screenResHeight; // Optional
    private int viewPortWidth; // Optional
    private int viewPortHeight; // Optional
    private int colorDepth; // Optional
    private String timezone; // Optional
    private String language; // Optional
    private String ipAddress; // Optional
    private String useragent; // Optional
    private String networkUserId; // Optional
    private String domainUserId; // Optional
    private String domainSessionId; // Optional

    // Getters and Setters

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getScreenResWidth() {
        return screenResWidth;
    }

    public void setScreenResWidth(int screenResWidth) {
        this.screenResWidth = screenResWidth;
    }

    public int getScreenResHeight() {
        return screenResHeight;
    }

    public void setScreenResHeight(int screenResHeight) {
        this.screenResHeight = screenResHeight;
    }

    public int getViewPortWidth() {
        return viewPortWidth;
    }

    public void setViewPortWidth(int viewPortWidth) {
        this.viewPortWidth = viewPortWidth;
    }

    public int getViewPortHeight() {
        return viewPortHeight;
    }

    public void setViewPortHeight(int viewPortHeight) {
        this.viewPortHeight = viewPortHeight;
    }

    public int getColorDepth() {
        return colorDepth;
    }

    public void setColorDepth(int colorDepth) {
        this.colorDepth = colorDepth;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUseragent() {
        return useragent;
    }

    public void setUseragent(String useragent) {
        this.useragent = useragent;
    }

    public String getNetworkUserId() {
        return networkUserId;
    }

    public void setNetworkUserId(String networkUserId) {
        this.networkUserId = networkUserId;
    }

    public String getDomainUserId() {
        return domainUserId;
    }

    public void setDomainUserId(String domainUserId) {
        this.domainUserId = domainUserId;
    }

    public String getDomainSessionId() {
        return domainSessionId;
    }

    public void setDomainSessionId(String domainSessionId) {
        this.domainSessionId = domainSessionId;
    }


    // Constructor

    public SubjectConfiguration() {
        userId = null; // Optional
        screenResWidth = 0; // Optional
        screenResHeight = 0; // Optional
        viewPortWidth = 0; // Optional
        viewPortHeight = 0; // Optional
        colorDepth = 0; // Optional
        timezone = Utils.getTimezone(); // Optional
        language = null; // Optional
        ipAddress = null; // Optional
        useragent = null; // Optional
        networkUserId = null; // Optional
        domainUserId = null; // Optional
        domainSessionId = null; // Optional
    }

    // Builder methods

    public SubjectConfiguration userId(String userId) {
        this.userId = userId;
        return this;
    }

    public SubjectConfiguration screenResolution(int width, int height) {
        screenResWidth = width;
        screenResHeight = height;
        return this;
    }

    public SubjectConfiguration viewPort(int width, int height) {
        viewPortWidth = width;
        viewPortHeight = height;
        return this;
    }

    /**
     * @param depth a color depth integer
     * @return itself
     */
    public SubjectConfiguration colorDepth(int depth) {
        colorDepth = depth;
        return this;
    }

    /**
     * Note that timezone is set by default to the server's timezone
     * (`TimeZone tz = Calendar.getInstance().getTimeZone().getID()`)
     * @param timezone a timezone string
     * @return itself
     */
    public SubjectConfiguration timezone(String timezone) {
        this.timezone = timezone;
        return this;
    }

    /**
     * @param language a language string
     * @return itself
     */
    public SubjectConfiguration language(String language) {
        this.language = language;
        return this;
    }

    /**
     * @param ipAddress a ipAddress string
     * @return itself
     */
    public SubjectConfiguration ipAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    /**
     * @param useragent a useragent string
     * @return itself
     */
    public SubjectConfiguration useragent(String useragent) {
        this.useragent = useragent;
        return this;
    }

    /**
     * @param networkUserId a networkUserId string
     * @return itself
     */
    public SubjectConfiguration networkUserId(String networkUserId) {
        this.networkUserId = networkUserId;
        return this;
    }

    /**
     * @param domainUserId a domainUserId string
     * @return itself
     */
    public SubjectConfiguration domainUserId(String domainUserId) {
        this.domainUserId = domainUserId;
        return this;
    }

    /**
     * @param domainSessionId a domainSessionId string
     * @return itself
     */
    public SubjectConfiguration domainSessionId(String domainSessionId) {
        this.domainSessionId = domainSessionId;
        return this;
    }
}
