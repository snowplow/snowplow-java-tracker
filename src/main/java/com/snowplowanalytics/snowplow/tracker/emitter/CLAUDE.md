# Emitter Module - Snowplow Java Tracker

## Module Overview

The emitter module handles event transmission to Snowplow collectors. It provides batching, buffering, retry logic, and asynchronous event sending with configurable HTTP clients. The BatchEmitter is the primary implementation with sophisticated error handling and exponential backoff.

## Core Components

```
Emitter (interface)
└── BatchEmitter          # Main implementation with batching
    ├── EventStore        # Event persistence interface
    │   └── InMemoryEventStore  # Default in-memory storage
    ├── BatchPayload      # POST request wrapper
    ├── EmitterCallback   # Success/failure callbacks
    └── FailureType       # Failure categorization
```

## Emitter Architecture Principles

### 1. Asynchronous Processing
All events are processed asynchronously:
```java
// ✅ Events queued, not sent immediately
emitter.add(payload);  // Returns quickly
// Event sent later by executor

// ❌ Don't expect synchronous sending
emitter.add(payload);
// Event may not be sent yet!
```

### 2. Batch Processing Pattern
Events are batched for efficiency:
```java
// ✅ Configure batch size
EmitterConfiguration config = new EmitterConfiguration()
    .batchSize(25)        // Send when 25 events buffered
    .bufferCapacity(1000); // Max buffer size
```

### 3. Retry with Exponential Backoff
Failed requests retry with increasing delays:
```java
// ✅ Automatic retry handling
// Initial: 0ms delay
// First failure: 100ms delay
// Second failure: 200ms delay
// Max delay: 600000ms (10 min)
```

### 4. Thread Pool Management
Configurable thread pool for sending:
```java
// ✅ Configure thread count
EmitterConfiguration config = new EmitterConfiguration()
    .threadCount(2);  // Number of sender threads
```

## BatchEmitter Implementation

### Constructor Pattern
```java
// ✅ Use configuration objects
BatchEmitter emitter = new BatchEmitter(
    networkConfig,   // URL and HTTP client
    emitterConfig    // Batching and threading
);

// ❌ Don't use deprecated builder
BatchEmitter.builder().url(url).build();
```

### Event Addition Flow
```java
// ✅ Standard flow
boolean success = emitter.add(payload);
if (!success) {
    // Buffer full, event dropped
}
```

### Buffer Management
```java
// ✅ Force flush buffer
emitter.flushBuffer();

// ✅ Close and flush
emitter.close();  // Flushes remaining events
```

## EventStore Pattern

### Interface Contract
```java
public interface EventStore {
    boolean add(TrackerPayload payload);
    boolean remove(TrackerPayload payload);
    boolean removeAll(List<TrackerPayload> payloads);
    List<TrackerPayload> getBuffer();
    long getSize();
}
```

### InMemoryEventStore Implementation
```java
// ✅ Thread-safe implementation
public class InMemoryEventStore implements EventStore {
    private final AtomicLong bufferSize = new AtomicLong(0);
    private final ConcurrentLinkedDeque<TrackerPayload> buffer;
    private final long bufferCapacity;
}
```

## HTTP Client Configuration

### Client Adapter Options
```java
// ✅ OkHttp (default)
HttpClientAdapter client = new OkHttpClientAdapter(url);

// ✅ Apache HTTP
HttpClientAdapter client = new ApacheHttpClientAdapter(url);

// ✅ Custom implementation
HttpClientAdapter custom = new CustomAdapter();
networkConfig.httpClientAdapter(custom);
```

### Cookie Management
```java
// ✅ Cookie jar for network_userid
OkHttpClientWithCookieJarAdapter adapter = 
    new OkHttpClientWithCookieJarAdapter(url);
```

## Callback Pattern

### EmitterCallback Interface
```java
// ✅ Implement callbacks
EmitterCallback callback = new EmitterCallback() {
    @Override
    public void onSuccess(List<TrackerPayload> payloads) {
        // Handle successful send
    }
    
    @Override
    public void onFailure(FailureType type, boolean willRetry, 
                          List<TrackerPayload> payloads) {
        // Handle failure
    }
};
```

### Failure Types
```java
public enum FailureType {
    REJECTED_BY_COLLECTOR,  // 4xx responses
    TRACKER_ISSUE,         // 5xx or network errors
    EMITTER_REQUEST_FAILURE // Client-side issues
}
```

## Custom Retry Logic

### Status Code Configuration
```java
// ✅ Custom retry for status codes
Map<Integer, Boolean> customRetry = new HashMap<>();
customRetry.put(403, false);  // Don't retry 403
customRetry.put(500, true);   // Retry 500

EmitterConfiguration config = new EmitterConfiguration()
    .customRetryForStatusCodes(customRetry);
```

## Request Building

### GET Request Pattern
```java
// ✅ Single event GET request
String url = collectorUrl + "/i?" + payload.toString();
```

### POST Request Pattern
```java
// ✅ Batch POST request
BatchPayload batch = new BatchPayload();
batch.add(payload1);
batch.add(payload2);
String json = batch.toString();
// POST to collectorUrl + "/com.snowplowanalytics.snowplow/tp2"
```

## Thread Safety Patterns

### 1. Concurrent Buffer Access
```java
// ✅ Thread-safe operations
private final ConcurrentLinkedDeque<TrackerPayload> buffer;
private final AtomicLong bufferSize;
```

### 2. Executor Management
```java
// ✅ Proper shutdown
@Override
public void close() {
    isClosing = true;
    flushBuffer();
    executor.shutdown();
    executor.awaitTermination(timeout, TimeUnit.SECONDS);
}
```

### 3. Atomic Retry Delay
```java
// ✅ Thread-safe retry counter
private final AtomicInteger retryDelay = new AtomicInteger(0);
```

## Testing Emitter Behavior

### 1. Mock EventStore
```java
// ✅ Test with mock store
EventStore mockStore = mock(EventStore.class);
when(mockStore.getBuffer()).thenReturn(payloads);
```

### 2. MockWebServer Testing
```java
// ✅ Test HTTP interactions
MockWebServer server = new MockWebServer();
server.enqueue(new MockResponse().setResponseCode(200));
BatchEmitter emitter = new BatchEmitter(
    new NetworkConfiguration(server.url("/").toString()),
    new EmitterConfiguration()
);
```

### 3. Callback Testing
```java
// ✅ Verify callbacks
AtomicBoolean success = new AtomicBoolean(false);
EmitterCallback callback = new EmitterCallback() {
    @Override
    public void onSuccess(List<TrackerPayload> payloads) {
        success.set(true);
    }
};
```

## Common Pitfalls

### 1. Synchronous Expectations
```java
// ❌ Wrong: Expecting immediate send
emitter.add(payload);
assert(eventSent);  // May fail

// ✅ Correct: Wait or flush
emitter.add(payload);
emitter.flushBuffer();
Thread.sleep(100);
```

### 2. Ignoring Buffer Limits
```java
// ❌ Wrong: Not checking return value
emitter.add(payload);  // Might be dropped

// ✅ Correct: Check success
if (!emitter.add(payload)) {
    // Handle dropped event
}
```

### 3. Resource Leaks
```java
// ❌ Wrong: Not closing emitter
BatchEmitter emitter = new BatchEmitter(...);
// Use emitter...
// Never closed!

// ✅ Correct: Always close
try (BatchEmitter emitter = new BatchEmitter(...)) {
    // Use emitter
}  // Auto-closed
```

### 4. Improper Thread Count
```java
// ❌ Wrong: Too many threads
.threadCount(100)  // Excessive

// ✅ Correct: Reasonable count
.threadCount(2)    // Good default
```

## Performance Considerations

### Batch Size Tuning
- Small batches (1-10): Lower latency, more requests
- Medium batches (25-50): Balanced
- Large batches (100+): Higher latency, fewer requests

### Buffer Capacity
- Set based on expected event volume
- Consider memory constraints
- Default: 10,000 events

### Thread Count
- 1-2 threads for most applications
- More threads for high-volume scenarios
- Consider collector capacity

## Adding Custom Emitters

### Template for Custom Emitter
```java
public class CustomEmitter implements Emitter {
    @Override
    public boolean add(TrackerPayload payload) {
        // Custom logic
        return true;
    }
    
    @Override
    public void flushBuffer() {
        // Send all buffered events
    }
    
    @Override
    public void close() {
        // Cleanup resources
    }
}
```

## Contributing to Emitter Module

### Guidelines
1. Maintain thread safety in all operations
2. Implement proper resource cleanup in close()
3. Honor the EmitterCallback contract
4. Test retry logic with various failure scenarios
5. Document any custom retry strategies
6. Ensure buffer limits are respected