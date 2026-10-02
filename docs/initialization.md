# Initialization

[Integration guide](README.md)

## Full `init` signature

```kotlin
fun init(
    application: Application,
    appKey: String,
    clientId: String,
    smallIcon: String,
    logLevel: LavaLogLevel = LavaLogLevel.WARN,
    serverLogLevel: LavaLogLevel?,
    piConsentFlags: Set<LavaPIConsentFlag>? = null,
    customPIConsentFlagMapping: Map<String, Set<LavaPIConsentFlag>>? = null,
    customPIConsentFlags: Set<String>? = null,
    piConsentListener: ConsentListener? = null,
    hostAppUIReady: Boolean = true,
    initListener: LavaInitListener? = null,
    customUserAgent: String? = null,
    sdkMessageListener: SdkMessageListener? = null,
)
```

| Parameter | Description | Required |
| --- | --- | --- |
| `application` | The Android application class | Yes |
| `appKey` | App key, provided by LAVA | Yes |
| `clientId` | Client ID, provided by LAVA | Yes |
| `smallIcon` | The icon for push notifications | Yes |
| `logLevel` | Development log level | Yes |
| `serverLogLevel` | Log level for SDK logs sent to LAVA, useful for debugging | |
| `piConsentFlags` | Default personal information flags. See [Personal information consent](consent.md). | |
| `customPIConsentFlagMapping` | A custom map of PI consent flag values. Overrides the default consent flags from LAVA. See [Personal information consent](consent.md). | |
| `customPIConsentFlags` | Custom PI consent flags. Override the default consent flags from LAVA. See [Personal information consent](consent.md). | |
| `piConsentListener` | Called when there is a personal information consent error. See [Personal information consent](consent.md). | |
| `hostAppUIReady` | When `false`, the host app says when it is ready so the SDK can run actions such as push notifications or deep links. | |
| `initListener` | Called when the SDK is ready, so the app can run follow-up work such as showing the in-app pass. | |
| `customUserAgent` | Custom user-agent string used by the in-app pass for web features such as SSO. | |
| `sdkMessageListener` | Lets the SDK tell the host app about certain events. See [SDK message listener](sdk-messages.md). | |

## Pending UI tasks and initialization callback

LAVA UI work, such as a push notification overlay, waits until your app has finished its own UI setup. By default the SDK assumes the app is ready when you call `Lava.init()`. If the app must do other work first, such as network requests, pass `hostAppUIReady = false`:

```kotlin
Lava.init(
    this,
    BuildConfig.appKey,
    BuildConfig.clientId,
    R.drawable.app_icon_shil.toString(),
    LavaLogLevel.VERBOSE,
    LavaLogLevel.VERBOSE,
    hostAppUIReady = false,
    initListener = this
)
```

Implement `LavaInitListener` on the application:

```kotlin
class MyApplication : MultiDexApplication(), LavaInitListener {
    // ...
    override fun onCompleted() {
    }
}
```

When the app finishes its initialization, tell the SDK to run pending UI tasks:

```kotlin
Lava.instance.finishAppInitialization()
```

- `hostAppUIReady` turns off the default, which assumes the app is ready as soon as the SDK is initialized.
- `LavaInitListener` is the listener the SDK calls when it finishes its own initialization.
- `onCompleted` is that callback. Use it for work that should run on your side.

`finishAppInitialization()` signals that the app is ready. The SDK then runs pending UI tasks, including a notification overlay.

## Pending auth tasks

Call SDK APIs such as `showInAppPass()` from the success callback of `setEmail()` when you can. That gives those calls an access token.

Starting in version 2.0.30, pending auth tasks delay API calls until authentication finishes. Use this when a small set of SDK calls should run from a quick action, such as a button click:

```kotlin
Lava.instance.setEmail(username, object : ResultListener {
    override fun onResult(success: Boolean, message: String) {}
})
Lava.instance.showInAppPass(requireContext())
```

In this snippet, showing the in-app pass is delayed until `setEmail()` succeeds.

---

[Integration guide](README.md) · [Previous: Getting started](getting-started.md) · [Next: Personal information consent](consent.md)
