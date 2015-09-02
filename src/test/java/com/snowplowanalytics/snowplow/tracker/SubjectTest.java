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

// JUnit
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SubjectTest {

    @Test
    public void testSetUserId() throws Exception {
        Subject subject = new Subject();
        subject.setUserId("user1");
        assertEquals("user1", subject.getSubject().get("uid"));
    }

    @Test
    public void testSetScreenResolution() throws Exception {
        Subject subject = new Subject();
        subject.setScreenResolution(100, 150);
        assertEquals("100x150", subject.getSubject().get("res"));
    }

    @Test
    public void testSetViewPort() throws Exception {
        Subject subject = new Subject();
        subject.setViewPort(150, 100);
        assertEquals("150x100", subject.getSubject().get("vp"));
    }

    @Test
    public void testSetColorDepth() throws Exception {
        Subject subject = new Subject();
        subject.setColorDepth(10);
        assertEquals("10", subject.getSubject().get("cd"));
    }

    @Test
    public void testSetTimezone2() throws Exception {
        Subject subject = new Subject();
        subject.setTimezone("America/Toronto");
        assertEquals("America/Toronto", subject.getSubject().get("tz"));
    }

    @Test
    public void testSetLanguage() throws Exception {
        Subject subject = new Subject();
        subject.setLanguage("EN");
        assertEquals("EN", subject.getSubject().get("lang"));
    }

    @Test
    public void testSetIpAddress() throws Exception {
        Subject subject = new Subject();
        subject.setIpAddress("127.0.0.1");
        assertEquals("127.0.0.1", subject.getSubject().get("ip"));
    }

    @Test
    public void testSetUseragent() throws Exception {
        Subject subject = new Subject();
        subject.setUseragent("useragent");
        assertEquals("useragent", subject.getSubject().get("ua"));
    }

    @Test
    public void testGetSubject() throws Exception {
        Subject subject = new Subject();
        Map<String, String> expected = new HashMap<>();
        subject.setTimezone("America/Toronto");
        subject.setUserId("user1");

        expected.put("tz", "America/Toronto");
        expected.put("uid", "user1");

        assertEquals(expected, subject.getSubject());
    }
}
