# Debug information

[Integration guide](README.md)

## Get debug information

| Personal information consent | Collected data |
| --- | --- |
| Strictly Necessary, Functional | User ID, email, device ID, device name, device model, IP address, language, notification token, OS version, SDK version, platform, screen size |

```kotlin
fun fetchDebugData(listener: DebugDataListener)
```

**Usage**

```kotlin
Lava.instance.fetchDebugData(object : DebugDataListener {
    override fun onDebugData(data: DebugData?) {
    }
})
```

`DebugData` is declared as follows:

```kotlin
data class DebugData(
    val appInstallId: String? = null,
    val authorizationToken: String? = null,
    val notificationToken: String? = null,
    val user: LavaUser? = null,
    val userId: String? = null,
    val userTokenExpiresAt: Instant? = null,
    val userType: String? = null,
    val profileData: UserProfile? = null,
    val sdkVersion: String? = null,
    val server: String? = null,
)
```

---

[Integration guide](README.md) · [Previous: Personal information consent](consent.md) · [Next: Deep linking](deep-linking.md)
