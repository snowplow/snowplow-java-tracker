# Simple console sample

This is a small Java console project that sends PageView events to a given collector.

## Run

Ensure `build.gradle` is using a version available on the Snowplow maven repository, or a version installed to a local maven repository.

```bash
./gradlew jar
 java -jar ./build/libs/simple-console-all-0.0.1.jar "http://<your-collector-domain>"
```
