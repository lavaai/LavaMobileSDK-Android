package ai.lava.demoapp.android.notification

import ai.lava.demoapp.android.MainActivity
import ai.lava.demoapp.android.utils.CLog
import android.os.Bundle
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
      if (Lava.instance.canHandlePushNotification(remoteMessage.data)) {
        val b = Bundle()
        b.putInt("prevClickedId", MainActivity.selectedTab)
        if (!Lava.instance.handleNotification(applicationContext, MainActivity::class.java, data, null, b)) {
          CLog.e("Lava did not handle notification")
        }
        //Notification handled by the SDK, App can ignore the //notification
      } else {
        CLog.i("Notification to be handled by app")
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