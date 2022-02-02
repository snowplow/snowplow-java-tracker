## Benchmarking results

This benchmarking module is provided for maintainers, allowing them to check that their changes have not degraded performance. It uses the Java microbenchmarking harness, JMH.

The benchmark test measures the time taken to track one event. Note that this does not include the time for the event to be processed and sent, which happens asynchronously.

To run the test, navigate to this folder and run:

```bash
$ ./gradlew build
$ ./gradlew jmh
```

The tracker version is set in the `build.gradle` file. Change the specified version to benchmark a different tracker version. 
```groovy
dependencies {
    jmh 'com.snowplowanalytics:snowplow-java-tracker:0.11.0'
}
```
Note that you may also need to edit the `TrackerBenchmark` `closeThreads()` code. Versions from 0.12.0 onwards must call a different method. This is explained in in-line comments.

### Results
See this PR for discussion of benchmarking results: https://github.com/snowplow/snowplow-java-tracker/pull/301

