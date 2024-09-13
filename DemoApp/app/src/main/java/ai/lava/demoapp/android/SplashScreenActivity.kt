package ai.lava.demoapp.android

import ai.lava.demoapp.android.auth.LoginActivity
import ai.lava.demoapp.android.utils.CLog
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.lava.lavasdk.Lava

//import io.fabric.sdk.android.Fabric;
class SplashScreenActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    //Fabric.with(this, new Crashlytics());
    setContentView(R.layout.content_splash_screen)

    WindowInsetsControllerCompat(window, window.decorView.rootView).apply {
      hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
    }

    launchSuitableActivity()
  }

  private fun launchSuitableActivity() {
    var classToLaunch: Class<*> = LoginActivity::class.java
    val user = Lava.instance.getUser()
    if (user != null && user.isNormalUser()) {
      classToLaunch = MainActivity::class.java
    }
    val intent = Intent(this, classToLaunch)
    finish()
    startActivity(intent)

    Handler(Looper.getMainLooper()).postDelayed({
      launchLavaPushNotificationIfNeeded()
    }, 500)
  }

  private fun launchLavaPushNotificationIfNeeded() {
    try {
      val intentExtras = getIntent()?.getExtras()

      if (intentExtras != null) {
        val intentExtrasMap = mutableMapOf<String, String>()

        intentExtras?.keySet()?.forEach { key ->
          val value = intentExtras.get(key)
          intentExtrasMap[key] = value?.toString() ?: "null"
        }

        val canHandle = Lava.instance.canHandlePushNotification(intentExtrasMap)

        if (canHandle) {
          val handleNotificationResult = Lava.instance.handleNotification(applicationContext,
            MainActivity::class.java,
            intentExtrasMap,
            null, intentExtras)
          if (!handleNotificationResult) {
            CLog.e("Lava handleNotification failed")
          }

          return
        }
      }

      val data = this.intent.data
      if (data != null && data.isHierarchical) {
        val uri = this.intent.dataString
        CLog.i("Deep link clicked $uri")
        Lava.instance.handleDeepLink(this, uri!!)
      }
    } catch (e: Exception) {
      CLog.e("Failed in launchLavaPushNotificationIfNeeded")
      e.printStackTrace()
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    launchSuitableActivity()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
  }
}
