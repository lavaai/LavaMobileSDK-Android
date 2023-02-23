package ai.lava.demoapp.android.notification

import ai.lava.demoapp.android.MainActivity
import ai.lava.demoapp.android.utils.CLog
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.lava.lavasdk.Lava

/**
 * Created by Mohammed Azar on 11/6/2017.
 */
class MyFcmMessageService : FirebaseMessagingService() {
  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)

    val data = remoteMessage.data
    if (data.isNotEmpty()) {
      if (Lava.instance.handleNotification(applicationContext, MainActivity::class.java, data)) {
//Notification handled by the SDK, App can ignore the //notification
      } else {
        //App should handle the notification
      }
    }
  }

  override fun onNewToken(s: String) {
    super.onNewToken(s)
    CLog.d("FCM Token refreshed: $s")
    if (s.isNotEmpty()) {
      try {
        Lava.instance.setNotificationToken(s)
      } catch (e: Throwable) {
        e.printStackTrace()
      }
    }
  }
}