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

// JUnit
import com.snowplowanalytics.snowplow.tracker.configuration.SubjectConfiguration;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SubjectTest {

    @Test
    public void testSetUserId() {
        Subject subject = new Subject();
        subject.setUserId("user1");
        assertEquals("user1", subject.getSubject().get("uid"));
    }

    @Test
    public void testSetScreenResolution() {
        Subject subject = new Subject();
        subject.setScreenResolution(100, 150);
        assertEquals("100x150", subject.getSubject().get("res"));
    }

    @Test
    public void testSetViewPort() {
        Subject subject = new Subject();
        subject.setViewPort(150, 100);
        assertEquals("150x100", subject.getSubject().get("vp"));
    }

    @Test
    public void testSetColorDepth() {
        Subject subject = new Subject();
        subject.setColorDepth(10);
        assertEquals("10", subject.getSubject().get("cd"));
    }

    @Test
    public void testSetTimezone2() {
        Subject subject = new Subject();
        subject.setTimezone("America/Toronto");
        assertEquals("America/Toronto", subject.getSubject().get("tz"));
    }

    @Test
    public void testSetLanguage() {
        Subject subject = new Subject();
        subject.setLanguage("EN");
        assertEquals("EN", subject.getSubject().get("lang"));
    }

    @Test
    public void testSetIpAddress() {
        Subject subject = new Subject();
        subject.setIpAddress("127.0.0.1");
        assertEquals("127.0.0.1", subject.getSubject().get("ip"));
    }

    @Test
    public void testSetUseragent() {
        Subject subject = new Subject();
        subject.setUseragent("useragent");
        assertEquals("useragent", subject.getSubject().get("ua"));
    }

    @Test
    public void testSetDomainUserId() {
        Subject subject = new Subject();
        subject.setDomainUserId("duid");
        assertEquals("duid", subject.getSubject().get("duid"));
    }

    @Test
    public void testSetNetworkUserId() {
        Subject subject = new Subject();
        subject.setNetworkUserId("nuid");
        assertEquals("nuid", subject.getSubject().get("tnuid"));
    }

    @Test
    public void testSetDomainSessionId() {
        Subject subject = new Subject();
        subject.setDomainSessionId("sessionid");
        assertEquals("sessionid", subject.getSubject().get("sid"));
    }

    @Test
    public void testGetSubject() {
        Subject subject = new Subject();
        Map<String, String> expected = new HashMap<>();
        subject.setTimezone("America/Toronto");
        subject.setUserId("user1");

        expected.put("tz", "America/Toronto");
        expected.put("uid", "user1");

        assertEquals(expected, subject.getSubject());
    }

    @Test
    public void testBuilderMethods() {
        Subject subject = new Subject();
        subject
            .userId("user1")
            .screenResolution(100, 150)
            .viewPort(150, 100)
            .colorDepth(10)
            .timezone("America/Toronto")
            .language("EN")
            .ipAddress("127.0.0.1")
            .useragent("useragent")
            .domainUserId("duid")
            .domainSessionId("sessionid")
            .networkUserId("nuid");
        assertEquals("user1", subject.getSubject().get("uid"));
        assertEquals("100x150", subject.getSubject().get("res"));
        assertEquals("150x100", subject.getSubject().get("vp"));
        assertEquals("10", subject.getSubject().get("cd"));
        assertEquals("America/Toronto", subject.getSubject().get("tz"));
        assertEquals("EN", subject.getSubject().get("lang"));
        assertEquals("127.0.0.1", subject.getSubject().get("ip"));
        assertEquals("useragent", subject.getSubject().get("ua"));
        assertEquals("duid", subject.getSubject().get("duid"));
        assertEquals("sessionid", subject.getSubject().get("sid"));
        assertEquals("nuid", subject.getSubject().get("tnuid"));
    }

    @Test
    public void testCreateFromConfig() {
        SubjectConfiguration subjectConfig = new SubjectConfiguration()
                .ipAddress("xxx.000.xxx.111")
                .viewPort(123, 456)
                .useragent("Mac OS");
        Subject subject = new Subject(subjectConfig);

        assertEquals("xxx.000.xxx.111", subject.getSubject().get("ip"));
        assertEquals("123x456", subject.getSubject().get("vp"));
        assertEquals("Mac OS", subject.getSubject().get("ua"));
    }
}
