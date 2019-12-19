#!/bin/bash

tag_version=$1

cd $TRAVIS_BUILD_DIR
pwd

project_version=`./gradlew -q printVersion`
if [ "${project_version}" == "${tag_version}" ]; then
    ./gradlew bintrayUpload
else
    echo "Tag version '${tag_version}' doesn't match version in project ('${project_version}'). Aborting!"
    exit 1
fi
