#!/usr/bin/env bash

./gradlew \
  googleAtdPixelXLApi33DebugAndroidTest \
  testDebugUnitTest \
  --continue \
  --stacktrace \
  -Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect
