# Snowplow Java Tracker - Architecture & Development Guide

## Project Overview

The Snowplow Java Tracker is a library for tracking analytics events and sending them to Snowplow collectors. It provides a robust, configurable event tracking system for Java applications (JDK 8+) with support for batch processing, retry logic, and multiple HTTP client implementations.

**Key Technologies:**
- Java 8+ (minimum requirement)
- Gradle 6.5.0 build system
- Jackson for JSON processing
- OkHttp/Apache HTTP clients (optional features)
- JUnit 4/5 for testing
- SLF4J for logging

## Development Commands

```bash
# Build and run tests
./gradlew build

# Run tests only
./gradlew test

# Publish to local Maven repository
./gradlew publishToMavenLocal

# Generate version info
./gradlew generateSources

# Clean build artifacts
./gradlew clean

# Run example application
cd examples/simple-console
./gradlew jar
java -jar ./build/libs/simple-console-all-0.0.1.jar "http://collector-url"
```

## Architecture

### Core Components

1. **Tracker**: Central component that processes and sends events
2. **Emitter**: Handles HTTP communication and batch processing
3. **Events**: Type-safe event models (PageView, Structured, SelfDescribing, etc.)
4. **Subject**: User/device information attached to events
5. **Payload**: Event data structures and serialization
6. **Configuration**: Builder-pattern configuration objects

### Layer Organization

```
com.snowplowanalytics.snowplow.tracker/
├── configuration/     # Configuration builders
├── emitter/          # Event transmission layer
├── events/           # Event type definitions
├── payload/          # Data structures & serialization
├── http/             # HTTP client adapters
└── constants/        # Constants and parameters
```

## Core Architectural Principles

### 1. Configuration-First Design
All components use configuration objects with fluent builders:
```java
// ✅ Use configuration objects
TrackerConfiguration config = new TrackerConfiguration(namespace, appId)
    .platform(DevicePlatform.ServerSideApp)
    .base64Encoded(true);

// ❌ Don't use complex constructors
new Tracker(namespace, appId, platform, base64, emitter, subject);
```

### 2. Builder Pattern for Events
All events use the builder pattern for construction:
```java
// ✅ Use builders for events
PageView event = PageView.builder()
    .pageUrl("https://example.com")
    .customContext(contexts)
    .build();

// ❌ Don't use constructors or setters
PageView event = new PageView();
event.setPageUrl("https://example.com");
```

### 3. Immutable Event Data
Events are immutable after creation:
```java
// ✅ Create complete events
Structured event = Structured.builder()
    .category("category")
    .action("action")
    .build();

// ❌ Don't modify after creation
event.setCategory("new-category"); // No such method
```

### 4. Nullable Pattern with Validation
Required fields are validated, optional fields can be null:
```java
// ✅ Validate required fields
Objects.requireNonNull(namespace);
if (namespace.isEmpty()) {
    throw new IllegalArgumentException("namespace cannot be empty");
}

// ✅ Optional fields can be null
private Subject subject; // Can be null
```

## Critical Import Patterns

### Standard Package Organization
```java
// ✅ Correct import order
// 1. Java standard library
import java.util.*;
import java.io.Closeable;

// 2. Third-party libraries
import org.slf4j.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;

// 3. Snowplow tracker packages
import com.snowplowanalytics.snowplow.tracker.*;
import com.snowplowanalytics.snowplow.tracker.events.*;
```

## Essential Library Patterns

### 1. Tracker Creation Pattern
```java
// ✅ Use Snowplow factory with configurations
Tracker tracker = Snowplow.createTracker(
    trackerConfig,
    networkConfig,
    emitterConfig,
    subjectConfig
);

// ❌ Don't create tracker directly
Tracker tracker = new Tracker(config, emitter);
```

### 2. Event Tracking Pattern
```java
// ✅ Track returns event IDs
List<String> eventIds = tracker.track(event);

// ✅ Handle batch events (EcommerceTransaction)
List<String> ids = tracker.track(transaction); // May return multiple IDs
```

### 3. SelfDescribingJson Pattern
```java
// ✅ Use schema + data constructor
SelfDescribingJson context = new SelfDescribingJson(
    "iglu:com.example/context/jsonschema/1-0-0",
    Collections.singletonMap("key", "value")
);

// ❌ Don't use TrackerPayload as data
new SelfDescribingJson(schema, new TrackerPayload()); // Contains unwanted eid/dtm
```

### 4. HTTP Client Adapter Pattern
```java
// ✅ Let configuration choose adapter
BatchEmitter emitter = new BatchEmitter(networkConfig, emitterConfig);

// ✅ Or provide custom adapter
HttpClientAdapter adapter = new OkHttpClientAdapter(url);
networkConfig.httpClientAdapter(adapter);
```

## Model Organization Pattern

### Event Hierarchy
```
Event (interface)
└── AbstractEvent (base class)
    ├── PageView
    ├── Structured
    ├── SelfDescribing
    ├── ScreenView
    ├── Timing
    ├── EcommerceTransaction
    └── EcommerceTransactionItem
```

### Payload Types
```
Payload (interface)
├── TrackerPayload (main event payload)
├── SelfDescribingJson (schema-based data)
└── BatchPayload (POST request wrapper)
```

## Common Pitfalls & Solutions

### 1. TrackerPayload in SelfDescribingJson
```java
// ❌ Wrong: TrackerPayload adds unwanted eid/dtm
SelfDescribingJson data = new SelfDescribingJson(
    schema,
    new TrackerPayload()
);

// ✅ Correct: Use Map or Object
SelfDescribingJson data = new SelfDescribingJson(
    schema,
    new HashMap<String, Object>()
);
```

### 2. Synchronous Event Sending
```java
// ❌ Wrong: Expecting immediate send
tracker.track(event);
// Event not sent yet!

// ✅ Correct: Events are batched
tracker.track(event);
tracker.getEmitter().flushBuffer(); // Force send
tracker.close(); // Or close to flush
```

### 3. Missing Required Configuration
```java
// ❌ Wrong: Missing collector URL
NetworkConfiguration network = new NetworkConfiguration();

// ✅ Correct: Provide URL or adapter
NetworkConfiguration network = new NetworkConfiguration("https://collector.example.com");
```

### 4. Thread Safety
```java
// ❌ Wrong: Sharing Subject across threads
Subject shared = new Subject();
// Multiple threads modifying shared

// ✅ Correct: Event-specific subjects
PageView.builder()
    .subject(new Subject()) // Thread-local
    .build();
```

## File Structure Template

```
snowplow-java-tracker/
├── build.gradle                 # Main build configuration
├── src/
│   ├── main/java/com/snowplowanalytics/snowplow/tracker/
│   │   ├── Tracker.java        # Core tracker
│   │   ├── Snowplow.java       # Factory & registry
│   │   ├── Subject.java        # User/device info
│   │   ├── configuration/      # Config objects
│   │   ├── emitter/           # Event transmission
│   │   ├── events/            # Event types
│   │   ├── payload/           # Data structures
│   │   └── http/              # HTTP adapters
│   └── test/java/             # Unit tests
└── examples/
    └── simple-console/        # Usage example
```

## Testing Patterns

### 1. Mock Emitter Pattern
```java
// ✅ Use MockEmitter for testing
class MockEmitter implements Emitter {
    public List<TrackerPayload> eventList = new ArrayList<>();
    
    @Override
    public boolean add(TrackerPayload payload) {
        eventList.add(payload);
        return true;
    }
}
```

### 2. MockWebServer for HTTP Tests
```java
// ✅ Use OkHttp MockWebServer
MockWebServer server = new MockWebServer();
server.enqueue(new MockResponse().setResponseCode(200));
String url = server.url("/").toString();
```

### 3. Test Event Creation
```java
// ✅ Test with all optional fields
PageView event = PageView.builder()
    .pageUrl("https://example.com")
    .customContext(contexts)
    .trueTimestamp(timestamp)
    .subject(subject)
    .build();
```

## Quick Reference

### Event Types Checklist
- [ ] **PageView**: Web page views
- [ ] **Structured**: Category/action events
- [ ] **SelfDescribing**: Custom schema-based events
- [ ] **ScreenView**: Mobile screen views
- [ ] **Timing**: Performance timing
- [ ] **EcommerceTransaction**: Purchase events (deprecated)

### Configuration Components
- [ ] **TrackerConfiguration**: namespace, appId, platform
- [ ] **NetworkConfiguration**: collector URL, HTTP client
- [ ] **EmitterConfiguration**: batch size, thread count, callbacks
- [ ] **SubjectConfiguration**: user ID, session, device info

### Snowplow Concepts
- **Collector**: Endpoint receiving events
- **Schema**: Iglu schema for self-describing data
- **Context**: Additional data attached to events
- **Subject**: User/device information
- **Namespace**: Unique tracker identifier

## Contributing to CLAUDE.md

When adding or updating content in this document, please follow these guidelines:

### File Size Limit
- **CLAUDE.md must not exceed 40KB** (currently ~19KB)
- Check file size after updates: `wc -c CLAUDE.md`
- Remove outdated content if approaching the limit

### Code Examples
- Keep all code examples **4 lines or fewer**
- Focus on the essential pattern, not complete implementations
- Use `// ❌` and `// ✅` to clearly show wrong vs right approaches

### Content Organization
- Add new patterns to existing sections when possible
- Create new sections sparingly to maintain structure
- Update the architectural principles section for major changes
- Ensure examples follow current codebase conventions

### Quality Standards
- Test any new patterns in actual code before documenting
- Verify imports and syntax are correct for the codebase
- Keep language concise and actionable
- Focus on "what" and "how", minimize "why" explanations

### Multiple CLAUDE.md Files
- **Directory-specific CLAUDE.md files** can be created for specialized modules
- Follow the same structure and guidelines as this root CLAUDE.md
- Keep them focused on directory-specific patterns and conventions
- Maximum 20KB per directory-specific CLAUDE.md file

### Instructions for LLMs
When editing files in this repository, **always check for CLAUDE.md guidance**:

1. **Look for CLAUDE.md in the same directory** as the file being edited
2. **If not found, check parent directories** recursively up to project root
3. **Follow the patterns and conventions** described in the applicable CLAUDE.md
4. **Prioritize directory-specific guidance** over root-level guidance when conflicts exist