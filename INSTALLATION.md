# Installation & Build Guide

## Prerequisites
- JDK 17+ and Android SDK (Compile SDK 36, Min SDK 26 / Android 8.0+)
- Gradle 9+

## Build Commands
```bash
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
```

## Installing on Device
Install the generated debug APK from `app/build/outputs/apk/debug/` and walk through the 10-step onboarding flow to grant only the optional permissions you want to use.
