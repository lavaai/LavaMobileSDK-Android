package ai.lava.demoapp.android.deepLink

import ai.lava.demoapp.android.utils.CLog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.lava.lavasdk.Lava
import com.lava.lavasdk.LavaConstants

class DeepLinkReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    CLog.e("onReceive() DeepLinkReceiver called with: context ")
    val url = intent.getStringExtra(LavaConstants.DeepLink.URL)

    CLog.e("onReceive: $url")
    Toast.makeText(context, "Deeplink url : $url", Toast.LENGTH_SHORT).show()

    try {
      if (Lava.instance.canHandleDeepLink(url!!)) {
        if (Lava.instance.handlePassLink(context, url!!)) {
          CLog.i("deep link handled by sdk")
        } else {
          CLog.e("deep link not handled by sdk")
        }
      } else {
        var intent1 = context.packageManager.getLaunchIntentForPackage(url)
        if (intent1 == null) {
          intent1 = Intent(Intent.ACTION_VIEW)
          intent1.data = Uri.parse(url)
        }
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // opens a new "page" instead of overlapping the same app
        context.startActivity(intent1)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}