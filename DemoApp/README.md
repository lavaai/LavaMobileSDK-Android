# Android Lava SDK Demo App - Developer guide

## 1. Requirements:

Gradle: can be installed with Homebrew. Tested with Gradle 7.3.1.

## 2. Build steps

### 2.1 Automated build

At this moment this repo does not contain own automated build action, but the `SDK-Android` repo is building
this demo app as part of the lavasdk build process by injecting the lavasdk AAR file in the app/libs folder
of the demo app.

### 2.2 Manual build

1. Clone the source code.
2. Navigate to `app` folder.
3. Get the Lava Android SDK AAR file and put it in the app/libs folder (make sure you do not push the AAR file to the repo)
4. Execute:
  - `gradle assembleDebug  -PdebugLavaSdkBuild='false'` if you want to build with Debug variant.
  - `gradle assembleRelease -PdebugLavaSdkBuild='false'` if you want to build with Release variant.
5. Navigate to `./app/build/outputs/apk/demo/debug` or `./app/build/outputs/apk/demo/release`, the .apk file(s) will be available for installation on a device.

The `-PdebugLavaSdkBuild='false'` instructs build.gradle to link the SDK AAR file from the app/libs folder.
If you do not provide this parameter, it will default to `-PdebugLavaSdkBuild='false'` which will try to link the lavasdk as a module from ../SDK-Android.

### 2.3 Android Studio development / test build

`gradle.properties` sets debugLavaSdkBuild='true' this instructs build.gradle to link the LavaSDK as a local project module from ../SDK-Android.
This means that you have to clone SDK-Android on the same level as SDK-Android-DemoApp in order for the build to work.
In this case on each rebuild of the demo app the sdk will be rebuilt and you will be able to debug it in the emulator or on a connected device.

## 3. Configuration

Please refer to the Configuration section of SDK-Android for details on needed configuration.

https://github.com/lavaai/SDK-Android#3-configuration

The demo app needs google-services.json and lava-services.json file to be present in the app folder for the build to succeed.

google-services.json is obtained from Firebase Console.

lava-services.json must contain the clientId and appKey needed for the Lava SDK init method, for example:

`{
"clientId": "***-backend",
"appKey": "****05b2****"
}`

The lava-services.json file setting values are being automatically added to the BuildConfig class by build.gradle.

## 4. Development / debug / test cycle configuration

gradle.properties sets debugLavaSdkBuild=true by default set to link the Lava SDK as a local project module.

This means that you can develop / debug the demo app together with the sdk as if they were in one project.

## 5. Development / debugging with the SDK-Android-DemoApp

Please refer to the Development / debugging section in the SDK-Android repo.

https://github.com/lavaai/SDK-Android#5-development--debugging-with-the-sdk-android-demoapp
