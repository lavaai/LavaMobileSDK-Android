# SDK message listener

[Integration guide](README.md)

Since version 2.0.30, the SDK can send messages to the host app. Initialize with a listener, or set one later. The SDK keeps only the last listener you set.

`MessageType` identifies the message:

```kotlin
enum class MessageType(val messageType: String, val message: String) {
    PassClosed("pass_closed", "In-app pass is closed"),
    PassContainerClosed("pass_container_closed", "In-app pass container is closed")
}
```

| Message type | Use case |
| --- | --- |
| `PassClosed` | After the in-app pass content is rendered, a tap on the close button is delivered through `onSdkMessage`. |
| `PassContainerClosed` | After the in-app pass container is closed by the user, or because of an error, the message is delivered through `onSdkMessage`. |

## Initialize the SDK with the listener

Pass the listener to `init`. The full parameter list is in [Initialization](initialization.md). The listener-focused form is:

```kotlin
fun init(
    application: Application,
    appKey: String,
    clientId: String,
    smallIcon: String,
    logLevel: LavaLogLevel = LavaLogLevel.WARN,
    serverLogLevel: LavaLogLevel?,
    sdkMessageListener: SdkMessageListener? = null,
)
```

```kotlin
interface SdkMessageListener {
    fun onSdkMessage(messageType: String, message: String)
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
    sdkMessageListener = object : SdkMessageListener {
        override fun onSdkMessage(messageType: String, message: String) {
            // Perform your own logic based on the message.
            // For example, send a track event to LAVA.
            if (messageType == MessageType.PassClosed.messageType) {
                Lava.instance.track(
                    Track(
                        messageType,
                        "DEBUG",
                        trackerType = "log",
                        path = message
                    )
                )
            }
        }
    },
)
```

## Set the SDK message listener

You can also set the listener later, in the place that needs it. For example, put it on an activity or fragment if the message should trigger navigation.

```kotlin
fun setSdkMessageListener(sdkMessageListener: SdkMessageListener)
```

**Usage**

```kotlin
Lava.instance.setSdkMessageListener(object : SdkMessageListener {
    override fun onSdkMessage(messageType: String, message: String) {
        if (messageType == MessageType.PassClosed.messageType) {
            Lava.instance.track(
                Track(
                    messageType,
                    "DEBUG",
                    trackerType = "log",
                    path = message
                )
            )
        }
    }
})
```

---

[Integration guide](README.md) · [Previous: Pass](pass.md)
