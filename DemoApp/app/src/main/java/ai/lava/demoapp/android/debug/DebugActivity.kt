package ai.lava.demoapp.android.debug

import ai.lava.demoapp.android.R
import ai.lava.demoapp.android.utils.CLog
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.TextUtils
import android.text.format.Formatter
import android.view.View
import android.widget.TextView
import com.lava.lavasdk.DebugData
import com.lava.lavasdk.DebugDataListener
import com.lava.lavasdk.Lava

class DebugActivity : Activity() {
  private var mTvDebugInfo: TextView? = null
  private var mDebugInfo: DebugData? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_debug)
    try {
      requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    } catch (e: Throwable) {
    }
    initUi()
    fetchDebugInfo()
  }

  private fun fetchDebugInfo() {
    mDebugInfo = DebugData()

    Lava.instance.fetchDebugData(object : DebugDataListener{
      override fun onDebugData(data: DebugData?) {
        mDebugInfo = data ?: DebugData()
        setDebugInfo()
      }
    })

    setDebugInfo()
  }

  private fun setDebugInfo() {
    mTvDebugInfo?.text = mDebugInfo?.let{toInfoString(it)} ?: ""
  }

  private fun initUi() {
    findViewById<View>(R.id.ibClose).setOnClickListener { finish() }
    mTvDebugInfo = findViewById(R.id.tvDebugInfo)
  }

  companion object {
    fun showDebugInfo(a: Activity?) {
      if (a == null) {
        CLog.e("ShowDebugInfo failed - Activity context is null")
        return
      }
      val intent = Intent(a, DebugActivity::class.java)
      a.startActivity(intent)
    }
  }

  fun toInfoString(dd: DebugData): String {
    val debugInfo = StringBuilder()
    try {
      if (!TextUtils.isEmpty(dd.appInstallId)) {
        debugInfo.append("App install id : ").append(dd.appInstallId).append("\n\n")
      }
      if (!TextUtils.isEmpty(dd.userId)) {
        debugInfo.append("LAVA user id : ").append(dd.userId).append("\n\n")
      }
      if (!TextUtils.isEmpty(dd.user?.email)) {
        debugInfo.append("Email : ").append(dd.user?.email).append("\n\n")
      }
      if (!TextUtils.isEmpty(dd.userType)) {
        debugInfo.append("User type : ").append(dd.userType).append("\n\n")
      }
      if (!TextUtils.isEmpty(dd.notificationToken)) {
        debugInfo.append("Notification token : ").append(dd.notificationToken).append("\n\n")
      }
      if (!TextUtils.isEmpty(dd.sdkVersion)) {
        debugInfo.append("SDK version : ").append(dd.sdkVersion).append("\n\n")
      }
      if (!TextUtils.isEmpty(dd.server)) {
        debugInfo.append("Server : ").append(dd.server).append("\n\n")
      }

      if (!TextUtils.isEmpty(dd.authorizationToken)) {
        debugInfo.append("Authorization token : ").append(dd.authorizationToken).append("\n\n")
      }
      if (dd.profileData != null) {
        debugInfo.append("User profile info :-\n")
        if (dd.profileData != null) {
          debugInfo.append("First name : ").append(dd.profileData!!.firstName).append("\n")
          debugInfo.append("Last name : ").append(dd.profileData!!.lastName).append("\n")
        } else {
          debugInfo.append("User profile info is empty")
        }
      } else {
        debugInfo.append("User profile info :- Not downloaded")
      }
      debugInfo.append("\n\n")

      val ipAddress = getIPAddress()
      if (!TextUtils.isEmpty(ipAddress)) {
        debugInfo.append("Ip address : ").append(ipAddress)
      }
      debugInfo.append("\n\n")

      val debugConsoleUrl = dd.debugConsoleUrl
      if (!TextUtils.isEmpty(debugConsoleUrl)) {
        debugInfo.append("Debug Console URL : ").append(debugConsoleUrl)
      }
      debugInfo.append("\n\n")
    } catch (e: Throwable) {
      e.printStackTrace()
    }
    return debugInfo.toString()
  }

  private fun getIPAddress(): String? {
    val wm = applicationContext.getSystemService(Activity.WIFI_SERVICE) as? WifiManager
    if (wm != null) {
      return Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
    }
    return null
  }

}
