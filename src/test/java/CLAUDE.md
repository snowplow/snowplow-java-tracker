# Testing Guide - Snowplow Java Tracker

## Testing Overview

The test suite uses JUnit 4 with JUnit 5 Vintage Engine for backward compatibility. Tests focus on unit testing individual components with extensive use of mocking for external dependencies like HTTP servers.

## Test Organization

```
src/test/java/com/snowplowanalytics/snowplow/tracker/
├── TrackerTest.java           # Core tracker functionality
├── SnowplowTest.java          # Factory and registry
├── SubjectTest.java           # Subject data management
├── UtilsTest.java            # Utility functions
├── emitter/                  # Emitter layer tests
├── events/                   # Event type tests
├── payload/                  # Payload serialization tests
└── http/                     # HTTP client tests
```

## Core Testing Patterns

### 1. Mock Emitter Pattern
Essential for testing tracker behavior without network calls:
```java
// ✅ Standard MockEmitter
public static class MockEmitter implements Emitter {
    public List<TrackerPayload> eventList = new ArrayList<>();
    
    @Override
    public boolean add(TrackerPayload payload) {
        eventList.add(payload);
        return true;
    }
}
```

### 2. Test Setup Pattern
Consistent test initialization:
```java
// ✅ Standard setUp
@Before
public void setUp() {
    mockEmitter = new MockEmitter();
    TrackerConfiguration config = new TrackerConfiguration("AF003", "cloudfront")
        .base64Encoded(false);
    tracker = new Tracker(config, mockEmitter);
}
```

### 3. MockWebServer Pattern
For testing HTTP interactions:
```java
// ✅ HTTP testing setup
MockWebServer server = new MockWebServer();
server.enqueue(new MockResponse()
    .setResponseCode(200)
    .setBody("ok"));
String url = server.url("/").toString();
```

### 4. Assertion Patterns
Verify event payloads correctly:
```java
// ✅ Payload verification
TrackerPayload payload = mockEmitter.eventList.get(0);
Map<String, String> map = payload.getMap();
assertEquals("pv", map.get("e"));        // Event type
assertEquals(url, map.get("url"));       // Page URL
assertNotNull(map.get("eid"));          // Event ID
```

## Event Testing Patterns

### 1. Builder Validation Tests
```java
// ✅ Test required fields
@Test(expected = NullPointerException.class)
public void testMissingRequiredField() {
    PageView.builder().build();  // Missing pageUrl
}
```

### 2. Optional Field Tests
```java
// ✅ Test optional fields
@Test
public void testOptionalFields() {
    PageView event = PageView.builder()
        .pageUrl("https://example.com")
        .pageTitle(null)  // Should work
        .build();
    assertNotNull(event);
}
```

### 3. Context Testing
```java
// ✅ Test custom contexts
@Test
public void testCustomContext() {
    List<SelfDescribingJson> contexts = singletonList(
        new SelfDescribingJson("schema", 
            Collections.singletonMap("key", "value"))
    );
    PageView event = PageView.builder()
        .pageUrl("https://example.com")
        .customContext(contexts)
        .build();
}
```

## Emitter Testing Patterns

### 1. Batch Processing Tests
```java
// ✅ Test batching behavior
@Test
public void testBatchSize() throws InterruptedException {
    BatchEmitter emitter = new BatchEmitter(
        networkConfig,
        new EmitterConfiguration().batchSize(2)
    );
    emitter.add(payload1);
    emitter.add(payload2);  // Should trigger send
    Thread.sleep(500);
    // Verify batch sent
}
```

### 2. Retry Logic Tests
```java
// ✅ Test retry on failure
@Test
public void testRetryLogic() {
    server.enqueue(new MockResponse().setResponseCode(500));
    server.enqueue(new MockResponse().setResponseCode(200));
    // Add event and verify retry
}
```

### 3. Callback Tests
```java
// ✅ Test callbacks
@Test
public void testSuccessCallback() {
    AtomicBoolean called = new AtomicBoolean(false);
    EmitterCallback callback = new EmitterCallback() {
        @Override
        public void onSuccess(List<TrackerPayload> payloads) {
            called.set(true);
        }
    };
    // Verify callback invoked
}
```

## Payload Testing Patterns

### 1. Serialization Tests
```java
// ✅ Test JSON serialization
@Test
public void testJsonSerialization() {
    SelfDescribingJson json = new SelfDescribingJson(
        "schema",
        Collections.singletonMap("key", "value")
    );
    String result = json.toString();
    assertTrue(result.contains("\"schema\":\"schema\""));
}
```

### 2. Base64 Encoding Tests
```java
// ✅ Test encoding
@Test
public void testBase64Encoding() {
    TrackerConfiguration config = new TrackerConfiguration("ns", "app")
        .base64Encoded(true);
    // Verify contexts are base64 encoded
}
```

### 3. Size Calculation Tests
```java
// ✅ Test payload size
@Test
public void testPayloadSize() {
    TrackerPayload payload = new TrackerPayload();
    payload.add("key", "value");
    assertTrue(payload.getByteSize() > 0);
}
```

## Subject Testing Patterns

### 1. Subject Merging Tests
```java
// ✅ Test subject override
@Test
public void testSubjectOverride() {
    Subject trackerSubject = new Subject();
    trackerSubject.setUserId("tracker-user");
    
    Subject eventSubject = new Subject();
    eventSubject.setUserId("event-user");
    
    // Event subject should override
}
```

### 2. Platform Detection Tests
```java
// ✅ Test platform setting
@Test
public void testPlatformDetection() {
    Subject subject = new Subject();
    subject.setPlatform(DevicePlatform.Mobile);
    assertEquals("mob", subject.getSubject().get("p"));
}
```

## Thread Safety Testing

### 1. Concurrent Access Tests
```java
// ✅ Test thread safety
@Test
public void testConcurrentAccess() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(10);
    for (int i = 0; i < 10; i++) {
        new Thread(() -> {
            tracker.track(event);
            latch.countDown();
        }).start();
    }
    latch.await();
    // Verify all events tracked
}
```

### 2. Buffer Overflow Tests
```java
// ✅ Test buffer limits
@Test
public void testBufferOverflow() {
    EmitterConfiguration config = new EmitterConfiguration()
        .bufferCapacity(2);
    // Add 3 events, verify one dropped
}
```

## Test Utilities

### 1. Event ID Validation
```java
// ✅ Validate UUID format
private boolean isValidUUID(String id) {
    try {
        UUID.fromString(id);
        return true;
    } catch (Exception e) {
        return false;
    }
}
```

### 2. Timestamp Validation
```java
// ✅ Validate timestamp
private boolean isValidTimestamp(String ts) {
    try {
        long timestamp = Long.parseLong(ts);
        return timestamp > 0;
    } catch (Exception e) {
        return false;
    }
}
```

### 3. JSON Validation
```java
// ✅ Validate JSON structure
private boolean isValidJson(String json) {
    try {
        new ObjectMapper().readTree(json);
        return true;
    } catch (Exception e) {
        return false;
    }
}
```

## Common Test Anti-Patterns

### 1. Real Network Calls
```java
// ❌ Don't use real endpoints
BatchEmitter emitter = new BatchEmitter(
    new NetworkConfiguration("https://real-collector.com"),
    config
);

// ✅ Use MockWebServer
MockWebServer server = new MockWebServer();
```

### 2. Sleep Without Reason
```java
// ❌ Arbitrary sleep
Thread.sleep(5000);  // Why?

// ✅ Sleep with purpose
Thread.sleep(100);  // Allow async operation
```

### 3. Missing Cleanup
```java
// ❌ Resources not cleaned
MockWebServer server = new MockWebServer();
// Never shutdown

// ✅ Proper cleanup
@After
public void tearDown() throws IOException {
    server.shutdown();
}
```

### 4. Overly Complex Mocks
```java
// ❌ Complex mock setup
when(mock.method1()).thenReturn(x);
when(mock.method2()).thenReturn(y);
// 20 more lines...

// ✅ Simple test double
class SimpleEmitter implements Emitter {
    // Minimal implementation
}
```

## Test Execution

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests TrackerTest

# Run with coverage
./gradlew test jacocoTestReport
```

### Test Categories
- **Unit Tests**: Individual component testing
- **Integration Tests**: Component interaction
- **Concurrency Tests**: Thread safety verification
- **Performance Tests**: Not in main suite

## Contributing Test Guidelines

1. **Test Naming**: Use descriptive names (testPageViewWithAllFields)
2. **One Assertion Per Test**: Keep tests focused
3. **Mock External Dependencies**: Never make real network calls
4. **Test Edge Cases**: Null values, empty strings, limits
5. **Document Complex Tests**: Add comments for non-obvious logic
6. **Clean Up Resources**: Always close/shutdown in @After