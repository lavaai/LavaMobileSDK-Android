package ai.lava.demoapp.android

import ai.lava.demoapp.android.auth.LoginActivity
import ai.lava.demoapp.android.utils.CLog
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.lava.lavasdk.Lava

//import io.fabric.sdk.android.Fabric;
@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : Activity() {

  private val REQUEST_CODE = 1001

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    //Fabric.with(this, new Crashlytics());
    setContentView(R.layout.content_splash_screen)

    WindowInsetsControllerCompat(window, window.decorView.rootView).apply {
      hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
    }

    // Handle deep link for when opening from the notification
    if (intent?.action == Intent.ACTION_VIEW && !referrer.toString().isEmpty()) {
      handleDeepLink(intent = intent)
      finish()
      return
    }

    requestNotificationPermission()
  }

  private fun requestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(
          this, 
          Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        ActivityCompat.requestPermissions(
          this,
          arrayOf(Manifest.permission.POST_NOTIFICATIONS),
          REQUEST_CODE
        )
      } else {
        launchSuitableActivity()
      }
    } else {
      launchSuitableActivity()
    }
  }

  private fun launchSuitableActivity() {
    var classToLaunch: Class<*> = LoginActivity::class.java
    val user = Lava.instance.getUser()
    if (user != null && user.isNormalUser()) {
      classToLaunch = MainActivity::class.java
    }

    val intent = Intent(this, classToLaunch)
    startActivity(intent)
    finish()

    Handler(Looper.getMainLooper()).postDelayed({
      launchLavaPushNotificationIfNeeded()
    }, 500)
  }

  private fun launchLavaPushNotificationIfNeeded() {
    try {

      val intentExtras = intent?.extras

      if (intentExtras != null) {
        val intentExtrasMap = mutableMapOf<String, String>()

        intentExtras.keySet()?.forEach { key ->
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

      // Handle deep link for direct deep link opening
      handleDeepLink(this.intent)
    } catch (e: Exception) {
      CLog.e("Failed in launchLavaPushNotificationIfNeeded")
      e.printStackTrace()
    }
  }

  fun handleDeepLink(intent: Intent?) {
    intent?.apply {
      val data = this.data
      if (data != null && data.isHierarchical) {
        val uri = intent.dataString
        CLog.i("Deep link clicked $uri")
        Lava.instance.handleDeepLink(this@SplashScreenActivity, uri!!)
      }
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == REQUEST_CODE) {
      if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(this, "Push notification permission granted", Toast.LENGTH_SHORT).show()

      } else {
        // Permission denied
        Toast.makeText(this, "Push notification permission denied", Toast.LENGTH_SHORT).show()
      }
    }
    launchSuitableActivity()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
  }
}
