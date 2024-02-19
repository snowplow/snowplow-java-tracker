package com.snowplowanalytics.snowplow.tracker.events;

import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;

// JUnit
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class SelfDescribingTest {

    @Test
    public void testEqualityOfTwoInstances() {
        SelfDescribing.Builder<?> builder = SelfDescribing.builder()
                .eventData(new SelfDescribingJson("schema-name"));

        SelfDescribing a = new SelfDescribing( builder );
        SelfDescribing b = new SelfDescribing( builder );

        assertEquals(a, b);
    }

    @Test
    public void testNegativeEqualityOfTwoInstances() {
        SelfDescribing.Builder<?> builderOne = SelfDescribing.builder()
                .eventData(new SelfDescribingJson("schema-name-one"));

        SelfDescribing.Builder<?> builderTwo = SelfDescribing.builder()
                .eventData(new SelfDescribingJson("schema-name-two"));

        SelfDescribing a = new SelfDescribing( builderOne );
        SelfDescribing b = new SelfDescribing( builderTwo );

        assertNotEquals(a, b);
    }

}