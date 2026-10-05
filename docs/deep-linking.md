# Deep linking

[Integration guide](README.md)

There are two ways to set up a deep link: a custom URL scheme, or an Android App Link.

## Custom URL scheme (deprecated)

### Setup

Declare a custom URL scheme on an activity in `AndroidManifest.xml`:

```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="<YOUR SCHEME>" />
</intent-filter>
```

### Check if a link can be handled

| Personal information consent | Collected data |
| --- | --- |
| Strictly Necessary | Access token |

Call `canHandleDeepLink` before asking the SDK to open a link. Use this when the app, or another library, also handles deep links.

```kotlin
fun canHandleDeepLink(deepLinkUrl: String): Boolean
```

**Usage**

```kotlin
if (Lava.instance.canHandleDeepLink(url)) {
    Lava.instance.handleDeepLink(ctx, url)
} else {
    // Handle the deep link in the app
}
```

### Handle a deep link

| Personal information consent | Collected data |
| --- | --- |
| Strictly Necessary | Access token |

> **Deprecated**
>
> `handlePassLink` is deprecated.

```kotlin
fun handlePassLink(ctx: Context, deepLinkData: String): Boolean
```

To let the SDK process the deep link, call:

```kotlin
fun handleDeepLink(ctx: Context, deepLinkData: String): Boolean
fun handleDeepLink(ctx: Context, deepLinkData: String, listener: ResultListener? = null): Boolean
```

- `ctx`: context of the activity or fragment that handles the deep link.
- `deepLinkData`: deep link target.
- `listener`: notified when there is a consent error.

**Usage**

Handle LAVA deep links with a consent listener when you need to ask for Functional consent:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    this.intent.dataString?.let { uri ->
        Lava.instance.handleDeepLink(this, uri, object : ResultListener {
            override fun onResult(success: Boolean, message: String) {
                if (!success) {
                    showConsentDialog(message)
                }
            }
        })
    }
}

// Show the consent dialog in your app
fun showConsentDialog(message: String) {
    // Show the consent dialog with or without the error message
}
```

If the app does not need to handle consent:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    this.intent.dataString?.let { uri ->
        Lava.instance.handleDeepLink(this, uri)
    }
}
```

To present a pass deep link yourself, as a host-owned fragment, see [Handle a pass deep link as a Fragment](pass.md#handle-a-pass-deep-link-as-a-fragment).

## Android App Links

Android App Links are the preferred way to deep link into the app.

### 1. Declare website associations

Host a static file so Android can verify the association:

```text
https://<fully qualified domain>/.well-known/assetlinks.json
```

```json
[{
  "relation": ["delegate_permission/common.handle_all_urls"],
  "target": {
    "namespace": "android_app",
    "package_name": "com.example",
    "sha256_cert_fingerprints": [
      "14:6D:E9:83:C5:73:06:50:D8:EE:B9:95:2F:34:FC:64:16:A0:83:42:E6:1D:BE:A8:8A:04:96:B2:3F:CF:44:E5"
    ]
  }
}]
```

See the Android guide on [verifying App Links](https://developer.android.com/training/app-links/verify-android-applinks).

### 2. Prepare `AndroidManifest.xml`

Add an intent filter on the activity that should open the link. `host` looks like `www.example.com`. `pathPrefix` must include `/lava/inapp/pass`.

```xml
<activity
    android:name="com.example.android.YourActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="https"
              android:host="<YOUR DOMAIN NAME>"
              android:pathPrefix="/lava/inapp/pass" />
    </intent-filter>
</activity>
```

### 3. Handle app links in the activity

| Personal information consent | Collected data |
| --- | --- |
| Strictly Necessary | Access token |

```kotlin
fun handlePassLink(ctx: Context, deepLinkData: String): Boolean
```

**Usage**

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    this.intent.dataString?.let { uri ->
        if (Lava.instance.canHandleDeepLink(uri)) {
            Lava.instance.handlePassLink(this, uri)
        } else {
            // Link not handled by Lava
        }
    }
}
```

## Consistency of deep links between iOS and Android

When you ship both an iOS app and an Android app, pick a scheme and host so both apps open the same links the same way.

- Option 1: both Android and iOS use a custom URL scheme.
- Option 2: Android uses App Links, and iOS uses Universal Links.

---

[Integration guide](README.md) · [Previous: Debug information](debug.md) · [Next: Authentication and profile](authentication.md)
