# Authentication and profile

[Integration guide](README.md)

By default, when you call `Lava.instance.start()` in your `AppDelegate`, the SDK gathers enough information to authenticate the app. That only updates the device identity on the LAVA backend.

To use features such as LAVA push notifications or the in-app pass, authenticate by setting the user email with `setEmail()`, or by setting an external id with `setUserId()`.

## Set email

| Personal information consent |
| --- |
| Strictly Necessary, Functional |

> **Deprecated.** `setEmail()` will be removed in a future release. Use [`setUserId()`](#set-user-id) with the email as `id` and `type` set to `"email"`.

Provide a valid email to authenticate as a normal user. After that succeeds, the app can use the rest of the SDK.

To switch users, set the new email. To stop working as the current user (for example on logout), pass `null`.

```kotlin
fun setEmail(email: String?, listener: ResultListener?)
```

`ResultListener` is the general API callback:

```kotlin
interface ResultListener {
    fun onResult(success: Boolean, message: String)
}
```

**Usage**

```kotlin
Lava.instance.setEmail(email, object : ResultListener {
    override fun onResult(success: Boolean, message: String) {
        // Perform redirect
    }
})
```

### Error handling

If `setEmail` fails, `onResult` is called with `success` set to `false` and `message` set to the error.

## Set user ID

| Personal information consent |
| --- |
| Strictly Necessary, Functional |

If the app does not use email as the user identity, authenticate with `setUserId`. This is the usual path when users are identified on an external system. Contact LAVA to configure it.

```kotlin
fun setUserId(id: String?, type: String?, listener: ResultListener?)
```

| Parameter | Required | Data type | Description |
| --- | --- | --- | --- |
| `id` | No | `String` | External identifier. Can be an email or a UUID. Passing `null` logs the user out. |
| `type` | No | `String` | External system used to authenticate the user. Can be `email` or `nba_id_encrypted`. `null` defaults to `email`. |
| `listener` | Yes | `ResultListener` | Listener for success and failure. |

**Usage**

```kotlin
Lava.instance.setUserId(id, type, object : ResultListener {
    override fun onResult(success: Boolean, message: String) {
        // Perform redirect
    }
})
```

### Error handling

As with `setEmail`, failure calls `onResult` with `success` set to `false` and the error in `message`. This method also supports NBA ID authentication. In those cases `message` is an NBA ID error code.

| Message | Meaning |
| --- | --- |
| `NBA_ID_00` | NBA ID service not configured for this environment |
| `NBA_ID_01` | Failed to acquire an NBA service token |
| `NBA_ID_02` | NBA account not found |
| `NBA_ID_03` | Ticketmaster account not linked |

```kotlin
Lava.instance.setUserId(
    userId,
    "nba_id_encrypted",
    object : ResultListener {
        override fun onResult(success: Boolean, message: String) {
            if (success) {
                // Successful
            } else {
                val displayErrorMessage = when (message) {
                    NBAIDError.NBA_ID_00 -> "NBA ID service not configured for this environment"
                    NBAIDError.NBA_ID_01 -> "Failed to acquire an NBA service token"
                    NBAIDError.NBA_ID_02 -> "NBA account not found"
                    NBAIDError.NBA_ID_03 -> "Ticketmaster account not linked"
                    else -> {
                        // Handle non NBA ID errors
                    }
                }
                // Perform corresponding action
            }
        }
    }
)
```

`NBAIDError` holds those constants so the app can compare them directly.

## LAVA user

| Personal information consent | Collected data |
| --- | --- |
| Strictly Necessary, Functional | Access token |

Check whether the app is authenticated for LAVA APIs by reading `LavaUser`:

```kotlin
fun getUser(): LavaUser?
```

**Usage**

```kotlin
val user = Lava.instance.getUser()
if (user != null && user.isNormalUser()) {
    // Navigate to main screen
}
```

`LavaUser` is the identity of the current user:

```kotlin
data class LavaUser(
    val email: String = ""
) {
    fun isAnonymous() = email.isEmpty()
    fun isNormalUser() = email.isNotEmpty()
}
```

## Profile

A regular login (with email) enables editable access to a user's LAVA profile.

### Get profile

| Personal information consent | Collected data |
| --- | --- |
| Strictly Necessary, Functional | Access token, secure member token |

```kotlin
fun getProfile(listener: ProfileListener)
```

**Usage**

```kotlin
Lava.instance.getProfile(object : ProfileListener {
    override fun onProfile(success: Boolean, message: String, profile: UserProfile?) {
        if (success) {
            // Do something with the profile
        }
    }
})
```

```kotlin
interface ProfileListener {
    fun onProfile(success: Boolean, message: String, profile: UserProfile?)
}
```

```kotlin
data class UserProfile(
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
)
```

### Update profile

| Personal information consent | Collected data |
| --- | --- |
| Strictly Necessary, Functional | Access token, secure member token, first name, last name, phone number |

Updates merge the fields you send with what the server already has. Updating only the first name leaves the rest of the profile unchanged. Deleting a field is not supported. You can remove the content of a field by providing a non-null value for it.

```kotlin
fun updateProfile(profile: UserProfile, listener: ProfileListener?)
```

**Usage**

```kotlin
Lava.instance.updateProfile(profile, object : ProfileListener {
    override fun onProfile(success: Boolean, message: String, profile: UserProfile?) {
        // Do something with the profile
    }
})
```

---

[Integration guide](README.md) · [Previous: Deep linking](deep-linking.md) · [Next: Push notifications](push-notifications.md)
