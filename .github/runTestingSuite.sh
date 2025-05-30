#!/usr/bin/env bash

./gradlew \
  googleAtdPixelXLApi33DebugAndroidTest \
  --continue \
  --stacktrace \
  -Pandroid.experimental.androidTest.numManagedDeviceShards="3" \
  -Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect

./gradlew \
  testDebugUnitTest \
  --continue \
  --stacktrace \
