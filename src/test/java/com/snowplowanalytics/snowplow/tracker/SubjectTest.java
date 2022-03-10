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
package com.snowplowanalytics.snowplow.tracker;

// Java
import java.util.HashMap;
import java.util.Map;

// JUnit
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SubjectTest {

    @Test
    public void testSetUserId() {
        Subject subject = new Subject.SubjectBuilder().build();
        subject.setUserId("user1");
        assertEquals("user1", subject.getSubject().get("uid"));
    }

    @Test
    public void testSetScreenResolution() {
        Subject subject = new Subject.SubjectBuilder().build();
        subject.setScreenResolution(100, 150);
        assertEquals("100x150", subject.getSubject().get("res"));
    }

    @Test
    public void testSetViewPort() {
        Subject subject = new Subject.SubjectBuilder().build();
        subject.setViewPort(150, 100);
        assertEquals("150x100", subject.getSubject().get("vp"));
    }

    @Test
    public void testSetColorDepth() {
        Subject subject = new Subject.SubjectBuilder().build();
        subject.setColorDepth(10);
        assertEquals("10", subject.getSubject().get("cd"));
    }

    @Test
    public void testSetTimezone2() {
        Subject subject = new Subject.SubjectBuilder().build();
        subject.setTimezone("America/Toronto");
        assertEquals("America/Toronto", subject.getSubject().get("tz"));
    }

    @Test
    public void testSetLanguage() {
        Subject subject = new Subject.SubjectBuilder().build();
        subject.setLanguage("EN");
        assertEquals("EN", subject.getSubject().get("lang"));
    }

    @Test
    public void testSetIpAddress() {
        Subject subject = new Subject.SubjectBuilder().build();
        subject.setIpAddress("127.0.0.1");
        assertEquals("127.0.0.1", subject.getSubject().get("ip"));
    }

    @Test
    public void testSetUseragent() {
        Subject subject = new Subject.SubjectBuilder().build();
        subject.setUseragent("useragent");
        assertEquals("useragent", subject.getSubject().get("ua"));
    }

    @Test
    public void testSetDomainUserId() {
        Subject subject = new Subject.SubjectBuilder().build();
        subject.setDomainUserId("duid");
        assertEquals("duid", subject.getSubject().get("duid"));
    }

    @Test
    public void testSetNetworkUserId() {
        Subject subject = new Subject.SubjectBuilder().build();
        subject.setNetworkUserId("nuid");
        assertEquals("nuid", subject.getSubject().get("tnuid"));
    }

    @Test
    public void testSetDomainSessionId() {
        Subject subject = new Subject.SubjectBuilder().build();
        subject.setDomainSessionId("sessionid");
        assertEquals("sessionid", subject.getSubject().get("sid"));
    }

    @Test
    public void testGetSubject() {
        Subject subject = new Subject.SubjectBuilder().build();
        Map<String, String> expected = new HashMap<>();
        subject.setTimezone("America/Toronto");
        subject.setUserId("user1");

        expected.put("tz", "America/Toronto");
        expected.put("uid", "user1");

        assertEquals(expected, subject.getSubject());
    }
}
