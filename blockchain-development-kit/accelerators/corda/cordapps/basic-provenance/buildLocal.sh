#!/usr/bin/env bash

#
# Build and copy cordapp to the pre-installed examples
#
./gradlew clean test assemble
cp cordapp/build/libs/cordapp-example-0.1.jar lib/basic-provenance.jar
