package ai.lava.demoapp.android

import ai.lava.demoapp.android.auth.LoginActivity
import ai.lava.demoapp.android.utils.CLog
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.lava.lavasdk.Lava

//import io.fabric.sdk.android.Fabric;
class SplashScreenActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    //Fabric.with(this, new Crashlytics());
    setContentView(R.layout.content_splash_screen)

    WindowInsetsControllerCompat(window, window.decorView.rootView).apply {
      hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
    }

    askNotificationPermission()
//    launchSuitableActivity()
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
      val intentExtras = this.intent.extras

      if (intentExtras != null) {
        val intentExtrasMap = mutableMapOf<String, String>()

        intentExtras.keySet()?.forEach { key ->
          val value = intentExtras.getString(key)
          intentExtrasMap[key] = value ?: "null"
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

  private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission(),
  ) { isGranted: Boolean ->
    if (isGranted) {
      CLog.d("Permission granted")
    } else {
      CLog.d("Permission is not granted")
    }
  }

  private fun askNotificationPermission() {
    // This is only necessary for API level >= 33 (TIRAMISU)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
      ) {
        // FCM SDK (and your app) can post notifications.
        launchSuitableActivity()
      } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
        // TODO: display an educational UI explaining to the user the features that will be enabled
        //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
        //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
        //       If the user selects "No thanks," allow the user to continue without notifications.
      } else {
        // Directly ask for the permission
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }
}
