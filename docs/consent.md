# Personal information consent

[Integration guide](README.md)

Some SDK functions send personal information (for example an email address) that is covered by privacy regulations. Each feature section lists the consent it needs.

Examples include `setEmail()` and tracking a button click. The SDK exposes two ways for the user to set consent:

1. `init()` — pass consent when the app opens and the user is not logged in.
2. `setPIConsentFlags()` — update the consent list later, when the user changes it in your app.

Keep the consent list in the app and pass it to the SDK:

- When the SDK is initialized
- When the user changes the consent level in the app UI
- Whenever the user context changes, for example logout or login

## When your app is opening

Pass the initial consent list to `init()`. That list applies to anonymous users and is stored by the SDK. After login, call `setPIConsentFlags()` to apply the list for that user.

```kotlin
Lava.init(
    this,
    BuildConfig.appKey,
    BuildConfig.clientId,
    R.drawable.app_icon_shil.toString(),
    LavaLogLevel.VERBOSE,
    LavaLogLevel.VERBOSE,
    ConsentUtils.getConsentFlags(BuildConfig.consentFlags.toSet()),
    object : ConsentListener {
        override fun onResult(error: Throwable?, shouldLogout: Boolean) {
            if (error != null) {
                // Handle consent error
                return
            }
            if (shouldLogout) {
                // Perform necessary navigation
            }
        }
    }
)
```

> **Notes**
>
> - If the user allows no consent, pass an empty list as `piConsentFlags`.
> - `piConsentFlags = null`, or omitting the argument, means consent for all flags.

`LavaPIConsentFlag` has these values:

```kotlin
enum class LavaPIConsentFlag {
    StrictlyNecessary,
    PerformanceAndLogging,
    Functional,
    Targeting,
}
```

| Flag | Meaning |
| --- | --- |
| Strictly Necessary | Lets the LAVA backend access and store user identity, including device ID and notification token. |
| Performance and Logging | Required for the SDK to report errors and debug information. |
| Functional | Required for most SDK APIs: `setEmail`, `getMessages`, `showNotification`, and similar calls. |
| Targeting | Required if the host app calls `track()`. |

`ConsentListener` reports the result of setting consent:

```kotlin
interface ConsentListener {
    fun onResult(
        error: Throwable?,
        shouldLogout: Boolean = false,
    )
}
```

During initialization, a consent error is passed to `onResult()` as the `error` argument. Otherwise `onResult()` is called with `null`. For example, disabling `StrictlyNecessary` while enabling another flag calls `onResult()` with an error.

`shouldLogout` is usually `false`. It is `true` only when the user disables every consent. Use that to drive navigation in the app.

## Required consent for specific features

| Feature | Method | Consent |
| --- | --- | --- |
| Debug information | `fetchDebugData()` | Strictly Necessary, Functional |
| Deep linking | `handleDeepLink()` | Strictly Necessary |
| Deep linking | `canHandleDeepLink()` | Strictly Necessary |
| Authentication | `setEmail()` | Strictly Necessary, Functional |
| Authentication | `getUser()` | Strictly Necessary, Functional |
| Profile | `getProfile()` | Strictly Necessary, Functional |
| Profile | `updateProfile()` | Strictly Necessary, Functional |
| Push notifications | `setNotificationToken()` | Strictly Necessary, Functional |
| Push notifications | `handleNotification()` | Strictly Necessary, Functional |
| Push notifications | `canHandlePushNotification()` | Strictly Necessary |
| Push notifications | `setCustomStyle()` | Strictly Necessary |
| Message inbox | `getInboxMessages()` | Strictly Necessary, Functional |
| Message inbox | `deleteInboxMessages()` | Strictly Necessary, Functional |
| Message inbox | `markInboxMessages()` | Strictly Necessary, Functional |
| Message inbox | `displayMessage()` | Strictly Necessary, Functional |
| Track | `track()` | Strictly Necessary, Targeting |
| Secure member token | `setSecureMemberToken()` | Strictly Necessary |
| Secure member token | `subscribeSecureMemberTokenExpiry()` | Strictly Necessary |
| Secure member token | `unsubscribeSecureMemberTokenExpiry()` | Strictly Necessary |
| Pass | `showInAppPass()` | Strictly Necessary, Functional |
| Pass | `createInAppPassFragment()` | Strictly Necessary, Functional |
| Pass | `navigateInAppPass()` | Strictly Necessary, Functional |
| Pass | `requestHideInAppPass()` | Strictly Necessary, Functional |
| Pass | `hideInAppPass()` | Strictly Necessary, Functional |

## Update the consent list

After the user changes consent in your app, send the new list to the SDK:

```kotlin
fun setPIConsentFlags(
    piConsentFlags: Set<LavaPIConsentFlag>,
    piConsentListener: ConsentListener?
)
```

`piConsentListener` works the same way as in `init()`.

**Usage**

```kotlin
Lava.instance.setPIConsentFlags(
    itemsToUpdate,
    object : ConsentListener {
        override fun onResult(error: Throwable?, shouldLogout: Boolean) {
            if (error != null) {
                // Handle consent error
                return
            }
            if (shouldLogout) {
                // Perform necessary navigation
            }
        }
    }
)
```

## Custom consent mapping

You can use your own consent names. Pass a `Map<String, Set<LavaPIConsentFlag>>` to `init()`, plus the set of consent strings that are currently granted:

```kotlin
fun init(
    application: Application,
    appKey: String,
    clientId: String,
    smallIcon: String,
    logLevel: LavaLogLevel = LavaLogLevel.WARN,
    serverLogLevel: LavaLogLevel?,
    customPIConsentFlagMapping: Map<String, Set<LavaPIConsentFlag>>? = null,
    customPIConsentFlags: Set<String>? = null,
    piConsentListener: ConsentListener? = null,
)
```

**Usage**

```kotlin
Lava.init(
    this,
    BuildConfig.appKey,
    BuildConfig.clientId,
    R.drawable.app_icon_shil.toString(),
    LavaLogLevel.VERBOSE,
    LavaLogLevel.VERBOSE,
    mapOf(
        "Consent01" to setOf(LavaPIConsentFlag.StrictlyNecessary),
        "Consent02" to setOf(LavaPIConsentFlag.PerformanceAndLogging),
        "Consent03" to setOf(LavaPIConsentFlag.Functional),
        "Consent04" to setOf(LavaPIConsentFlag.Targeting),
    ),
    setOf(
        "Consent01",
        "Consent02",
        "Consent03",
        "Consent04",
    ),
    object : ConsentListener {
        override fun onResult(error: Throwable?, shouldLogout: Boolean) {
            if (error != null) {
                // Handle consent error
                return
            }
        }
    }
)
```

Later, change the granted set with:

```kotlin
fun setCustomPIConsentFlags(
    customPIConsentFlags: Set<String>,
    piConsentListener: ConsentListener?
)
```

**Usage**

```kotlin
val newConsentList = setOf(
    "Consent01",
    "Consent02",
    "Consent03",
)

Lava.instance.setCustomPIConsentFlags(
    newConsentList,
    listener
)
```

## Built-in OneTrust consent mapping

The SDK includes a OneTrust mapping you can pass as the custom consent map:

```kotlin
object LavaConsent {
    @JvmField
    val OneTrustDefaultConsentMapping: Map<String, Set<LavaPIConsentFlag>> = mapOf(
        "C0001" to setOf(LavaPIConsentFlag.StrictlyNecessary),
        "C0002" to setOf(LavaPIConsentFlag.PerformanceAndLogging),
        "C0003" to setOf(LavaPIConsentFlag.Functional),
        "C0004" to setOf(LavaPIConsentFlag.Targeting),
        "C0005" to emptySet()
    )
}
```

**Usage**

```kotlin
Lava.init(
    this,
    BuildConfig.appKey,
    BuildConfig.clientId,
    R.drawable.app_icon_shil.toString(),
    LavaLogLevel.VERBOSE,
    LavaLogLevel.VERBOSE,
    LavaConsent.OneTrustDefaultConsentMapping,
    setOf(
        "C0001",
        "C0002",
        "C0003",
        "C0004",
    ),
    object : ConsentListener {
        override fun onResult(error: Throwable?, shouldLogout: Boolean) {
            if (error != null) {
                // Handle consent error
                return
            }
        }
    }
)
```

---

[Integration guide](README.md) · [Previous: Initialization](initialization.md) · [Next: Debug information](debug.md)
