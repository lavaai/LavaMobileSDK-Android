package ai.lava.demoapp.android.deepLink

import ai.lava.demoapp.android.R
import ai.lava.demoapp.android.utils.CLog
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder

class DeepLinkActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_deep_link)
    val data = this.intent.data
    if (data != null && data.isHierarchical) {
      val uri = this.intent.dataString
      CLog.i("Deep link clicked $uri")
      val textView = findViewById<TextView>(R.id.textView)
      textView.text = "Deep Link Activity"
    }
  }

  @Throws(MalformedURLException::class, UnsupportedEncodingException::class, JSONException::class)
  private fun getParamsFromDeepLink(link: String): JSONObject {
    val url = URL(link)
    val data = url.query
    val params = data.split("&".toRegex()).toTypedArray()
    val paramsJson = JSONObject()
    for (param in params) {
      val idx = param.indexOf("=")
      paramsJson.put(
        URLDecoder.decode(param.substring(0, idx), "UTF-8"),
        URLDecoder.decode(param.substring(idx + 1), "UTF-8")
      )
    }
    return paramsJson
  }
}