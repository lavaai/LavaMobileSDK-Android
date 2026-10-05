# Message inbox

[Integration guide](README.md)

## Get messages

| Personal information consent | Collected data |
| --- | --- |
| Strictly Necessary, Functional | Access token |

```kotlin
fun getInboxMessages(listener: InboxListener)
```

**Usage**

```kotlin
Lava.instance.getInboxMessages(object : InboxListener {
    override fun onInboxMessage(
        success: Boolean,
        message: String,
        messages: List<InboxMessage>
    ) {
        // Do something with the message list
    }
})
```

## Batch delete messages

| Personal information consent | Collected data |
| --- | --- |
| Strictly Necessary, Functional | Access token, message IDs |

```kotlin
fun deleteInboxMessages(
    messageIds: List<String>,
    listener: ResultListener?
)
```

**Usage**

```kotlin
Lava.instance.deleteInboxMessages(messageIds, object : ResultListener {
    override fun onResult(success: Boolean, message: String) {
        // Do something with the result
    }
})
```

## Mark messages read or unread

| Personal information consent | Collected data |
| --- | --- |
| Strictly Necessary, Functional | Access token, message IDs |

```kotlin
fun markInboxMessages(
    messageIds: List<String>,
    read: Boolean,
    listener: ResultListener?
)
```

**Usage**

```kotlin
Lava.instance.markInboxMessages(
    messageIds,
    true,
    object : ResultListener {
        override fun onResult(success: Boolean, message: String) {
            // Do something with the result
        }
    }
)
```

## Display a single message

| Personal information consent | Collected data |
| --- | --- |
| Strictly Necessary, Functional | Access token, message ID |

The SDK no longer displays a single inbox message on its own, so the host app controls the message UI. Show it with `handleNotification`, then mark it read:

```kotlin
// This is a method in your app
fun displayMessage(ctx: Context, message: InboxMessage) {
    // Show the notification overlay for this message.
    // MainActivity is an example of the host activity that displays the notification UI.
    Lava.instance.handleNotification(
        ctx.applicationContext,
        MainActivity::class.java,
        message.toNotificationData()
    )

    if (!message.read) {
        Lava.instance.markInboxMessages(
            listOf(message.messageId),
            true,
            null
        )
    }
}
```

---

[Integration guide](README.md) · [Previous: Push notifications](push-notifications.md) · [Next: Track](track.md)
