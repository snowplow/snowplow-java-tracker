package com.snowplowanalytics.snowplow.tracker;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
    public void testGetSubject() throws Exception {
        Subject subject = new Subject();
        Map<String, String> expected = new HashMap<String, String>();
        subject.setTimezone("America/Toronto");
        subject.setUserId("user1");

        expected.put("tz", "America/Toronto");
        expected.put("uid", "user1");

        assertEquals(expected, subject.getSubject());
    }
}