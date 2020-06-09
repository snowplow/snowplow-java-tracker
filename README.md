# Java Analytics for Snowplow

[![Build][github-image]][github] [![Release][release-image]][releases] [![License][license-image]][license]

## Overview

Add analytics to your Java software with the **[Snowplow][snowplow]** event tracker for **[Java][java]**. See also: **[Snowplow Android Tracker][snowplow-android-tracker]**.

With this tracker you can collect event data from your Java-based desktop and server apps, servlets and games. Supports JDK7+.

## Quickstart

Assuming git, **[Vagrant][vagrant-install]** and **[VirtualBox][virtualbox-install]** installed:

```bash
 host$ git clone https://github.com/snowplow/snowplow-java-tracker.git
 host$ cd snowplow-java-tracker
 host$ vagrant up && vagrant ssh
guest$ cd /vagrant
guest$ ./gradlew clean build
guest$ ./gradlew test
guest$ ./gradlew publishToMavenLocal
guest$ cd /examples/simple-console
guest$ ./gradlew jar
guest$ java -jar ./build/libs/simple-console-all-0.0.1.jar "http://<your-collector-domain>"
```

## Find out more

| Technical Docs                  | Setup Guide               | Roadmap                 | Contributing                      |
|---------------------------------|---------------------------|-------------------------|-----------------------------------|
| ![i1][techdocs-image]           | ![i2][setup-image]        | ![i3][roadmap-image]    | ![i4][contributing-image]         |
| **[Technical Docs][techdocs]**  | **[Setup Guide][setup]**  | **[Roadmap][roadmap]**  | **[Contributing][contributing]**  |

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
[snowplow-android-tracker]: https://github.com/snowplow/snowplow-android-tracker/

[vagrant-install]: http://docs.vagrantup.com/v2/installation/index.html
[virtualbox-install]: https://www.virtualbox.org/wiki/Downloads

[techdocs-image]: https://d3i6fms1cm1j0i.cloudfront.net/github/images/techdocs.png
[setup-image]: https://d3i6fms1cm1j0i.cloudfront.net/github/images/setup.png
[roadmap-image]: https://d3i6fms1cm1j0i.cloudfront.net/github/images/roadmap.png
[contributing-image]: https://d3i6fms1cm1j0i.cloudfront.net/github/images/contributing.png

[techdocs]: https://github.com/snowplow/snowplow/wiki/Java-Tracker
[setup]: https://docs.snowplowanalytics.com/docs/collecting-data/collecting-from-own-applications/java-tracker/setup/
[roadmap]: https://github.com/snowplow/snowplow/wiki/Java-Tracker-Roadmap
[contributing]: https://github.com/snowplow/snowplow/wiki/Java-Tracker-Contributing
