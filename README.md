# Java Analytics for Snowplow

[![maintained]][tracker-classification] [![Build][github-image]][github] [![Release][release-image]][releases] [![License][license-image]][license]

## Overview

Add analytics to your Java software with the **[Snowplow][snowplow]** event tracker for **[Java][java]**. See also: **[Snowplow Android Tracker][snowplow-android-tracker]**.

With this tracker you can collect event data from your Java-based desktop and server apps, servlets and games. Supports JDK8+.

## Find out more

| Snowplow Docs                 | API Docs | Contributing                      |
|-------------------------------|-----------|-----------------------------------|
| ![i1][techdocs-image]         | ![i1][techdocs-image] |  ![i4][contributing-image]         |
| **[Snowplow Docs][techdocs]** | **[Javadoc Docs][apidocs]** | **[Contributing](CONTRIBUTING.md)**  |

## Maintainer Quickstart

Feedback and contributions are very welcome. If you have identified a bug, please log an issue on this repo. For all other feedback, discussion or questions please open a thread on our [Discourse forum][forums]. Feel free to make Pull Requests for new features, if you can code them yourself!

Clone this repo and navigate into the cloned folder. To run the tests locally, you will need Docker or Java installed. Using either method, the build will fail if there are failing tests.  

To run the tests using Docker, run:

```bash
$ docker build . -t snowplow-java-tracker
```

To run the tests using your installed JDK, run:

```bash
$ ./gradlew build
```

We have also included a simple demo, found in the `examples/simple-console` folder. You will need a JDK installed to run it. When run, it sends several events to your event collector. For a simple event collector, we advise using the [Snowplow Micro][micro] testing pipeline.

To run simple-console using the current Maven Central version of the Java tracker:
```bash
$ cd examples/simple-console
$ ./gradlew jar
$ java -jar ./build/libs/simple-console-all-0.0.1.jar "http://<your-collector-domain>"
```

To run simple-console using a local version of the Java tracker:
```bash
$ ./gradlew publishToMavenLocal
$ cd examples/simple-console
$ ./gradlew jar
$ java -jar ./build/libs/simple-console-all-0.0.1.jar "http://<your-collector-domain>"
```

## Copyright and license

The Snowplow Java Tracker is copyright 2014-2020 Snowplow Analytics Ltd.

Licensed under the **[Apache License, Version 2.0][license]** (the "License");
you may not use this software except in compliance with the License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[github]: https://github.com/snowplow/snowplow-java-tracker/actions
[github-image]: https://github.com/snowplow/snowplow-java-tracker/workflows/Build/badge.svg

[release-image]: https://img.shields.io/github/release/snowplow/snowplow-java-tracker.svg?style=flat
[releases]: https://github.com/snowplow/snowplow-java-tracker/releases

[license-image]: https://img.shields.io/badge/license-Apache--2-blue.svg?style=flat
[license]: https://www.apache.org/licenses/LICENSE-2.0

[java]: http://www.java.com/en/

[snowplow]: http://snowplowanalytics.com
[forums]: https://discourse.snowplowanalytics.com/
[snowplow-android-tracker]: https://github.com/snowplow/snowplow-android-tracker/
[micro]: https://github.com/snowplow-incubator/snowplow-micro

[techdocs-image]: https://d3i6fms1cm1j0i.cloudfront.net/github/images/techdocs.png
[setup-image]: https://d3i6fms1cm1j0i.cloudfront.net/github/images/setup.png
[roadmap-image]: https://d3i6fms1cm1j0i.cloudfront.net/github/images/roadmap.png
[contributing-image]: https://d3i6fms1cm1j0i.cloudfront.net/github/images/contributing.png

[techdocs]: https://docs.snowplowanalytics.com/docs/collecting-data/collecting-from-own-applications/java-tracker/
[apidocs]: https://snowplow.github.io/snowplow-java-tracker/index.html?overview-summary.html

[tracker-classification]: https://docs.snowplowanalytics.com/docs/collecting-data/collecting-from-own-applications/tracker-maintenance-classification/
[maintained]: https://img.shields.io/static/v1?style=flat&label=Snowplow&message=Maintained&color=9e62dd&labelColor=9ba0aa&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAeFBMVEVMaXGXANeYANeXANZbAJmXANeUANSQAM+XANeMAMpaAJhZAJeZANiXANaXANaOAM2WANVnAKWXANZ9ALtmAKVaAJmXANZaAJlXAJZdAJxaAJlZAJdbAJlbAJmQAM+UANKZANhhAJ+EAL+BAL9oAKZnAKVjAKF1ALNBd8J1AAAAKHRSTlMAa1hWXyteBTQJIEwRgUh2JjJon21wcBgNfmc+JlOBQjwezWF2l5dXzkW3/wAAAHpJREFUeNokhQOCA1EAxTL85hi7dXv/E5YPCYBq5DeN4pcqV1XbtW/xTVMIMAZE0cBHEaZhBmIQwCFofeprPUHqjmD/+7peztd62dWQRkvrQayXkn01f/gWp2CrxfjY7rcZ5V7DEMDQgmEozFpZqLUYDsNwOqbnMLwPAJEwCopZxKttAAAAAElFTkSuQmCC
