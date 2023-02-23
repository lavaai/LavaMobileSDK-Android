package ai.lava.demoapp.android.debug

import ai.lava.demoapp.android.R
import ai.lava.demoapp.android.utils.CLog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.lava.lavasdk.*

class DebugFragment : Fragment() {
  private var mLlRootContainer: LinearLayout? = null
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Lava.instance.track(Track(action = Track.ACTION_VIEW_SCREEN, category = "DebugScreen"))
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_debug, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initUI(view)
  }

  override fun onResume() {
    super.onResume()
    CLog.e("onResume DebugFragment")
    setUpUI()
  }

  private fun initUI(view: View) {
    mLlRootContainer = view.findViewById(R.id.ll_root_container)
  }

  private fun setUpUI() {
    try {
/*

            HashMap<String, String> hashMap = LSDK.instance.instancegetAllParams();

            for (Map.Entry<String, String> v : hashMap.entrySet()) {
                try {
                    View inflatedView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_debug_info_row, null);
                    if (!TextUtils.isEmpty(v.getKey()) && !TextUtils.isEmpty(v.getValue())) {
                        ((TextView) inflatedView.findViewById(R.id.tv_key)).setText(v.getKey());
                        ((TextView) inflatedView.findViewById(R.id.tv_value)).setText(v.getValue());
                        ((TextView) inflatedView.findViewById(R.id.tv_value)).setTextIsSelectable(true);
                        mLlRootContainer.addView(inflatedView);
                        CLog.d("Params Key : " + v.getKey() + " Value : " + v.getValue());
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
*/
  Lava.instance.fetchDebugData(object : DebugDataListener {
    override fun onDebugData(data: DebugData?) {
      val debugMap = data?.let{toDebugDataMap(data)} ?: mapOf()
      for ((key, value) in debugMap) {
        try {
          val inflatedView = LayoutInflater.from(activity).inflate(R.layout.layout_debug_info_row, null)
          if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            (inflatedView.findViewById<View>(R.id.tv_key) as TextView).text = key
            (inflatedView.findViewById<View>(R.id.tv_value) as TextView).text = value
            (inflatedView.findViewById<View>(R.id.tv_value) as TextView).setTextIsSelectable(true)
            mLlRootContainer!!.addView(inflatedView)
            CLog.d("Params Key : $key Value : $value")
          }
        } catch (e: Throwable) {
          e.printStackTrace()
        }
      }
    }
  })
    } catch (e: Throwable) {
      e.printStackTrace()
    }
  }

  private fun toDebugDataMap(dd: DebugData): Map<String,String?> {
    val debugInfoMap: MutableMap<String, String?> = hashMapOf()
    debugInfoMap["App install id"] = dd.appInstallId
    debugInfoMap["Notification token"] = dd.notificationToken
    debugInfoMap["SDK version"] = dd.sdkVersion
    debugInfoMap["Server"] = dd.server
    debugInfoMap["User id"] = dd.userId
    debugInfoMap["Email"] = dd.user?.email
    debugInfoMap["User token"] = dd.authorizationToken
    debugInfoMap["User token expiration"] = dd.userTokenExpiresAt.toString()

    return debugInfoMap
  }
}