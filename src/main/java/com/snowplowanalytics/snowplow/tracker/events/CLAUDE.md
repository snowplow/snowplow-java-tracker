# Events Module - Snowplow Java Tracker

## Module Overview

The events module defines all trackable event types in the Snowplow Java Tracker. Each event type implements the Event interface and extends AbstractEvent, providing a consistent builder-based API for event creation and immutable event objects.

## Event Type Hierarchy

```
Event (interface)
└── AbstractEvent (abstract base)
    ├── PageView         # Web page views
    ├── Structured       # Generic structured events
    ├── SelfDescribing   # Schema-based custom events
    ├── ScreenView       # Mobile/app screen views
    ├── Timing          # Performance timing events
    ├── EcommerceTransaction     # Purchase events (deprecated)
    └── EcommerceTransactionItem # Line items (deprecated)
```

## Core Event Patterns

### 1. Builder Pattern Mandatory
Every event MUST use the builder pattern:
```java
// ✅ Correct: Builder pattern
PageView event = PageView.builder()
    .pageUrl("https://example.com")
    .build();

// ❌ Wrong: Direct instantiation
new PageView("https://example.com");
```

### 2. AbstractEvent Base Class
All events extend AbstractEvent for common fields:
```java
// ✅ Inherit common behavior
public class PageView extends AbstractEvent {
    public static class Builder extends AbstractEvent.Builder<Builder> {
        // Event-specific fields
    }
}
```

### 3. Self-Returning Builder Pattern
Builders must return self() for chaining:
```java
// ✅ Correct self() implementation
public static class Builder extends AbstractEvent.Builder<Builder> {
    @Override
    protected Builder self() {
        return this;
    }
}
```

### 4. Payload Generation Pattern
Each event implements getPayload():
```java
// ✅ Standard payload creation
@Override
public TrackerPayload getPayload() {
    TrackerPayload payload = new TrackerPayload();
    payload.add(Parameter.EVENT, Constants.EVENT_PAGE_VIEW);
    payload.add(Parameter.PAGE_URL, pageUrl);
    return putTrueTimestamp(payload);
}
```

## Event-Specific Patterns

### PageView Events
```java
// ✅ Complete PageView example
PageView pageView = PageView.builder()
    .pageUrl("https://example.com")      // Required
    .pageTitle("Example Page")           // Optional
    .referrer("https://google.com")      // Optional
    .customContext(contexts)             // From AbstractEvent
    .trueTimestamp(timestamp)            // From AbstractEvent
    .subject(eventSubject)               // From AbstractEvent
    .build();
```

### Structured Events
```java
// ✅ Structured event with all fields
Structured event = Structured.builder()
    .category("video")        // Required
    .action("play")           // Required
    .label("tutorial")        // Optional
    .property("intro")        // Optional
    .value(1.5)              // Optional
    .build();
```

### SelfDescribing Events
```java
// ✅ Schema-based custom event
SelfDescribing event = SelfDescribing.builder()
    .eventData(new SelfDescribingJson(
        "iglu:com.example/event/jsonschema/1-0-0",
        eventDataMap
    ))
    .build();
```

### EcommerceTransaction Pattern
```java
// ✅ Transaction with items
EcommerceTransaction transaction = EcommerceTransaction.builder()
    .orderId("ORDER-123")
    .totalValue(99.99)
    .items(item1, item2)  // Variadic items
    .build();
// Note: Generates multiple events when tracked
```

## Common Implementation Requirements

### 1. Field Validation
```java
// ✅ Validate required fields in build()
public PageView build() {
    Objects.requireNonNull(pageUrl, "pageUrl cannot be null");
    if (pageUrl.isEmpty()) {
        throw new IllegalArgumentException("pageUrl cannot be empty");
    }
    return new PageView(this);
}
```

### 2. Immutability Enforcement
```java
// ✅ Final fields, no setters
public class PageView extends AbstractEvent {
    private final String pageUrl;
    private final String pageTitle;
    // No setters, only getters
}
```

### 3. Context Handling
```java
// ✅ Contexts are copied, not referenced
@Override
public List<SelfDescribingJson> getContext() {
    return new ArrayList<>(this.context);
}
```

### 4. Timestamp Management
```java
// ✅ True timestamp is optional
Long trueTimestamp = getTrueTimestamp();
if (trueTimestamp != null) {
    payload.add(Parameter.TRUE_TIMESTAMP, Long.toString(trueTimestamp));
}
```

## Event Parameters Reference

### Common Parameters (AbstractEvent)
- `customContext`: List of SelfDescribingJson contexts
- `trueTimestamp`: User-defined timestamp (milliseconds)
- `subject`: Event-specific Subject override

### PageView Parameters
- `pageUrl` (required): URL of the page
- `pageTitle`: Title of the page
- `referrer`: Referring URL

### Structured Parameters
- `category` (required): Event category
- `action` (required): Event action
- `label`: Event label
- `property`: Event property
- `value`: Numeric value

### SelfDescribing Parameters
- `eventData` (required): SelfDescribingJson with schema and data

## Testing Event Creation

### 1. Test Required Fields
```java
// ✅ Test validation
@Test(expected = NullPointerException.class)
public void testMissingRequiredField() {
    PageView.builder().build(); // Should throw
}
```

### 2. Test Optional Fields
```java
// ✅ Test with nulls
PageView event = PageView.builder()
    .pageUrl("https://example.com")
    .pageTitle(null)  // Should work
    .build();
```

### 3. Test Event Payload
```java
// ✅ Verify payload contents
TrackerPayload payload = event.getPayload();
assertEquals("pv", payload.getMap().get("e"));
assertEquals(url, payload.getMap().get("url"));
```

## Anti-Patterns to Avoid

### 1. Mutable Events
```java
// ❌ Never add setters
public void setPageUrl(String url) {
    this.pageUrl = url;
}
```

### 2. Public Constructors
```java
// ❌ Don't expose constructors
public PageView(String url) {
    this.pageUrl = url;
}
```

### 3. Direct Field Access
```java
// ❌ Don't expose mutable fields
public List<SelfDescribingJson> context;
```

### 4. Missing Validation
```java
// ❌ Don't skip validation
public Event build() {
    return new PageView(this); // No checks
}
```

## Adding New Event Types

### Template for New Event
```java
public class NewEvent extends AbstractEvent {
    private final String requiredField;
    private final String optionalField;
    
    public static class Builder extends AbstractEvent.Builder<Builder> {
        private String requiredField;
        private String optionalField;
        
        @Override
        protected Builder self() { return this; }
        
        public Builder requiredField(String value) {
            this.requiredField = value;
            return self();
        }
        
        public NewEvent build() {
            Objects.requireNonNull(requiredField);
            return new NewEvent(this);
        }
    }
    
    private NewEvent(Builder builder) {
        super(builder);
        this.requiredField = builder.requiredField;
        this.optionalField = builder.optionalField;
    }
    
    @Override
    public TrackerPayload getPayload() {
        TrackerPayload payload = new TrackerPayload();
        payload.add(Parameter.EVENT, "new");
        return putTrueTimestamp(payload);
    }
}
```

## Contributing to Events Module

### Guidelines
1. All new events MUST extend AbstractEvent
2. All new events MUST use the builder pattern
3. Required fields MUST be validated in build()
4. Events MUST be immutable after creation
5. Test both required and optional field scenarios
6. Document schema requirements for SelfDescribing events