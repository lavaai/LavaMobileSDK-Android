# Track

[Integration guide](README.md)

## Track events

| Personal information consent | Collected data |
| --- | --- |
| Strictly Necessary, Targeting | Access token, user activities |

```kotlin
fun track(t: Track)
```

**Usage**

```kotlin
Lava.instance.track(
    Track(
        action = Track.ACTION_VIEW_SCREEN,
        category = "ProfileScreen"
    )
)
```

`Track` contains:

```kotlin
data class Track(
    var action: String? = null,
    var category: String? = null,
    var metadata: Map<String, String>? = null,
    var path: String? = null,
    var tags: List<String>? = null,
    var trackerType: String? = TRACKER_TYPE_EVENT,
    var userParams: Map<String, String>? = null,
) {
    companion object {
        const val TRACKER_TYPE_EVENT = "event"
        const val ACTION_VIEW_SCREEN = "ViewScreen"
    }
}
```

---

[Integration guide](README.md) · [Previous: Message inbox](message-inbox.md) · [Next: Secure token](secure-token.md)
