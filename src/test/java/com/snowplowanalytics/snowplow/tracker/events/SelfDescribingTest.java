package com.snowplowanalytics.snowplow.tracker.events;

// JUnit
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
public class SelfDescribingTest {

    @Test
    public void testEqualityOfTwoInstances() {
        SelfDescribing.Builder<?> builder = SelfDescribing.builder()
                .eventData(new SelfDescribingJson("schema-name"));

        SelfDescribing a = new SelfDescribing( builder );
        SelfDescribing b = new SelfDescribing( builder );

        assertEquals(a, b);
    }

}