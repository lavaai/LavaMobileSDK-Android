# Pass

[Integration guide](README.md)

## Show the membership pass

| Personal information consent | Collected data |
| --- | --- |
| Strictly Necessary, Functional | Access token |

Show the membership pass on its own screen:

```kotlin
fun showInAppPass(ctx: Context, token: String? = null)
fun showInAppPass(ctx: Context, token: String? = null, listener: ResultListener? = null)
```

- `ctx`: context of the activity or fragment that shows the in-app pass.
- `token`: the secure member token, if the app uses one.
- `listener`: notified when there is a consent error.

**Usage**

Show the pass with a consent listener when you need to ask for Functional consent:

```kotlin
// Show the in-app pass when the user clicks a button
override fun onClick(view: View) {
    Lava.instance.showInAppPass(context, null, object : ResultListener {
        override fun onResult(success: Boolean, message: String) {
            if (!success) {
                showConsentDialog(message)
            }
        }
    })
}

// Show the consent dialog in your app
fun showConsentDialog(message: String) {
    // Show the consent dialog with or without the error message
}
```

If the app does not use a secure member token and does not need to handle content errors:

```kotlin
Lava.instance.showInAppPass(context)
```

Starting in version 2.0.33, you can also show the pass inset (the SDK attaches it inside the current activity and reserves space for your chrome) or embed it (the SDK returns a `Fragment` that you place in your own layout).

| Mode | Who presents | Typical use |
| --- | --- | --- |
| Full screen | SDK activity | Existing `showInAppPass(ctx)` |
| Inset | SDK, inside the host activity | Keep a bottom tab bar visible |
| Embed | Host app | Pass is a real tab or a custom frame |

> **Note**
>
> To handle `pass_closed` or `pass_container_closed`, see [SDK message listener](sdk-messages.md).

## Choose a presentation

`PassPresentation` describes how the pass is shown. Insets are density-independent pixels (dp) reserved around the pass so your chrome stays tappable. Zero insets are full screen.

```kotlin
data class PassPresentation(
    val insets: PassInsets = PassInsets.NONE,
    val showsCloseButton: Boolean? = null
)

data class PassInsets(
    val top: Int = 0,
    val left: Int = 0,
    val bottom: Int = 0,
    val right: Int = 0
)
```

`showsCloseButton` is forwarded to the pass HTML. `null` means automatic: show the close button only for full-screen presentation.

**Usage**

```kotlin
// Full screen (same as today)
PassPresentation.fullScreen

// Inset: reserve 56 dp at the bottom for a tab bar
PassPresentation.inset(bottom = 56)

// Inset: keep the top app bar and the bottom tabs
PassPresentation.inset(top = 64, bottom = 80)
```

## Named pages

Open a specific page inside the pass. Page IDs are open strings so the portal can add pages without an SDK release. Well-known constants:

```kotlin
PassPage.PASS        // "pass"
PassPage.HISTORY     // "history"
PassPage.BENEFITS    // "benefits"
PassPage("rewards")  // any portal page ID
```

Deep links such as `…/lava/inapp/pass?page=history` and `…/lava/inapp/pass/benefits` are parsed the same way.

## Show the pass inset above your chrome

| Personal information consent | Collected data |
| --- | --- |
| Strictly Necessary, Functional | Access token |

When `presentation` has non-zero insets, the SDK attaches the pass inside the host activity and leaves the reserved edges empty so a tab bar (or other chrome) stays visible and tappable. Inset mode requires a `FragmentActivity`. Do not pass an application context.

```kotlin
fun showInAppPass(
    activity: Activity,
    presentation: PassPresentation,
    page: PassPage = PassPage.PASS
)
```

- `activity`: the host `FragmentActivity` used to attach the pass.
- `presentation`: full screen or inset (reserved space in dp).
- `page`: named portal page to open. Defaults to `pass`.

**Usage**

```kotlin
// Measure the chrome you want to keep (tab bar + system navigation, and the
// top bar if you want that visible too). Values are in dp.
Lava.instance.showInAppPass(
    activity,
    PassPresentation.inset(top = topBarDp, bottom = tabBarDp),
    PassPage.PASS
)
```

> **Notes**
>
> - The overlay does not consume window insets. The activity still owns status and navigation padding.
> - Close and system Back go through the leave guard. See [Leave the pass](#leave-the-pass).
> - Opening another pass (full screen, inset, or embed) dismisses the pass that is already showing.

## Embed the pass in your own layout

| Personal information consent | Collected data |
| --- | --- |
| Strictly Necessary, Functional | Access token |

Use embed when the pass should be a real tab, or sit in a frame the SDK should not know about. The SDK does not present or attach the fragment.

```kotlin
fun createInAppPassFragment(page: PassPage = PassPage.PASS): Fragment
```

- `page`: named portal page to open. Defaults to `pass`.

Returns a pass `Fragment` for your container. The portal close button is hidden.

**Usage**

```kotlin
val fragment = Lava.instance.createInAppPassFragment(PassPage.PASS)
supportFragmentManager.beginTransaction()
    .replace(R.id.pass_container, fragment)
    .commit()
```

Keep-alive contract when the user switches tabs:

1. Create the fragment once and add it to your container.
2. When the user leaves the Pass tab, hide the fragment or its container (`hide()` / `isVisible = false`). Do not `remove()` it.
3. When the user returns to Pass, show the same instance. The portal does not reload.
4. Call `hideInAppPass(force = true)` on logout, or when you destroy the screen.

You do not need to call `requestHideInAppPass()` on every tab change. That API is only for when you are about to destroy the pass.

## Navigate to another page

| Personal information consent | Collected data |
| --- | --- |
| Strictly Necessary, Functional | Access token |

```kotlin
fun navigateInAppPass(
    page: PassPage,
    listener: PassNavigationListener? = null
)
```

- `page`: named portal page to open.
- `listener`: optional callback for whether navigation was allowed.

Navigation is a request. The portal can block it, for example when a form is unfinished.

**Usage**

```kotlin
Lava.instance.navigateInAppPass(PassPage.HISTORY, object : PassNavigationListener {
    override fun onNavigationResult(result: PassNavigationResult) {
        when (result) {
            is PassNavigationResult.Allowed -> { /* page changed */ }
            is PassNavigationResult.Blocked -> {
                // result.reason may explain why the user must stay
            }
        }
    }
})
```

## Leave the pass

| Personal information consent | Collected data |
| --- | --- |
| Strictly Necessary, Functional | Access token |

Hide is no longer fire-and-forget when the portal can block leaving.

```kotlin
fun requestHideInAppPass(listener: PassNavigationListener)
fun hideInAppPass(force: Boolean = false)
```

- `listener`: callback for whether hiding was allowed.
- `force`: when `true`, skips the leave guard. Use this for logout or session end only.

`PassNavigationResult` is either `Allowed` or `Blocked(reason)`.

**Usage**

```kotlin
// Ask before leaving (inset close, or destroying an embed)
Lava.instance.requestHideInAppPass(object : PassNavigationListener {
    override fun onNavigationResult(result: PassNavigationResult) {
        if (result is PassNavigationResult.Allowed) {
            // Safe to leave
        }
    }
})

// Logout / session end
Lava.instance.hideInAppPass(true)
```

## Default presentation for pass deep links

When a pass deep link does not specify a presentation, the SDK uses the default (full screen). Change that so links such as `/lava/inapp/pass` open inset above your tabs.

```kotlin
fun setDefaultPassPresentation(presentation: PassPresentation)
```

**Usage**

```kotlin
Lava.instance.setDefaultPassPresentation(
    PassPresentation.inset(bottom = 56)
)
```

## Pass lifecycle and page listeners

```kotlin
fun setPassLifecycleListener(listener: PassLifecycleListener?)
fun setPassPageListener(listener: PassPageListener?)
```

`PassCloseReason` is `CLOSE_BUTTON`, `HOST_DISMISSED`, or `CONTAINER_REMOVED`.

**Usage**

```kotlin
Lava.instance.setPassLifecycleListener { reason ->
    // Update your UI after the pass is closed
}
Lava.instance.setPassPageListener { page ->
    // Sync your own sub-navigation with page.rawValue
}
```

`pass_closed` and `pass_container_closed` via the [SDK message listener](sdk-messages.md) continue to work as documented.

## Handle a pass deep link as a Fragment

| Personal information consent | Collected data |
| --- | --- |
| Strictly Necessary, Functional | Access token |

When the host app owns navigation (a push, a tab replace, or a custom container), do not call `handleDeepLink`. That API still presents the pass. Use the factory instead. The SDK does not attach the fragment.

```kotlin
fun handleDeepLinkAsFragment(deepLinkData: String): Fragment?
```

- `deepLinkData`: the same input as `handleDeepLink`: a full URL, or any string containing `lava/inapp/pass`.

Returns a pass `Fragment` configured with the parsed page and the raw string for `linkPass`, or `null` when this is not a pass UI link.

- Trigger links (`lava/trigger/…`) return `null`. Keep using `handleDeepLink` for those.
- Unrelated URLs return `null`.
- If the user is not logged in yet, the factory still returns a configured fragment. The WebView waits on auth the same way embed already does.
- Path-only strings such as `lava/inapp/pass?page=history` are accepted by the factory. `canHandleDeepLink` still requires a URL scheme (`https://` or your app scheme).

Inspect the page without presenting:

```kotlin
PassPage.parseFromDeepLink(deepLinkData)
```

The `?page=` query wins over a path segment (`lava/inapp/pass/history`). If no page is specified, the result is `PassPage.PASS`.

**Usage**

```kotlin
if (Lava.instance.canHandleDeepLink(url)) {
    val fragment = Lava.instance.handleDeepLinkAsFragment(url)
    if (fragment != null) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.pass_container, fragment)
            .commit()
    } else {
        // Trigger or other Lava link. The SDK presents and tracks it as today.
        Lava.instance.handleDeepLink(this, url)
    }
}
```

Keep the fragment mounted when switching tabs (`hide` / `show`; do not remove it). Call `hideInAppPass(force = true)` on logout.

## Custom user-agent for the in-app pass

When the in-app pass needs a custom user-agent, for example to integrate with an external system, pass it to `Lava.init()`. See [Initialization](initialization.md) (`customUserAgent`).

---

[Integration guide](README.md) · [Previous: Secure token](secure-token.md) · [Next: SDK message listener](sdk-messages.md)
