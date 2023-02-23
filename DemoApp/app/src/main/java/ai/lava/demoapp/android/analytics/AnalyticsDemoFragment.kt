package ai.lava.demoapp.android.analytics

import ai.lava.demoapp.android.R
import ai.lava.demoapp.android.utils.CLog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.lava.lavasdk.Lava
import com.lava.lavasdk.Track
import java.util.*

class AnalyticsDemoFragment : Fragment(), View.OnClickListener {
  private var btVideo1: Button? = null
  private var btVideo2: Button? = null
  private var btImage1: Button? = null
  private var btImage2: Button? = null
  private var btArticle1: Button? = null
  private var btArticle2: Button? = null
  private var mToast: Toast? = null
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_demo, container, false)
    initUI(view)
    setUpUI()
    return view
  }

  private fun initUI(view: View) {
    btVideo1 = view.findViewById(R.id.bt_video_1)
    btVideo2 = view.findViewById(R.id.bt_video_2)
    btImage1 = view.findViewById(R.id.bt_image_1)
    btImage2 = view.findViewById(R.id.bt_image_2)
    btArticle1 = view.findViewById(R.id.bt_article_1)
    btArticle2 = view.findViewById(R.id.bt_article_2)
  }

  private fun setUpUI() {
    btVideo1!!.setOnClickListener(this)
    btVideo2!!.setOnClickListener(this)
    btImage1!!.setOnClickListener(this)
    btImage2!!.setOnClickListener(this)
    btArticle1!!.setOnClickListener(this)
    btArticle2!!.setOnClickListener(this)
  }

  override fun onClick(v: View) {
    val id = v.id
    val tag = v.tag as? String
    if (tag == null || tag.isEmpty()) {
      CLog.e("tag empty")
      return
    }
    when (id) {
      R.id.bt_video_1, R.id.bt_video_2, R.id.bt_image_1, R.id.bt_image_2, R.id.bt_article_1, R.id.bt_article_2 -> trackEvent(tag)
    }
  }

  private fun trackEvent(category: String) {
    try {
      mToast?.cancel()
      val toastMessage = "Analytics data sent for $category"
      mToast = Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT)
      mToast?.show()


      //   CLog.displayToast(getActivity(), "Analytics data sent for " + action);
      val tags: MutableList<String> = ArrayList()
      tags.add("tag1")
      tags.add("tag2")
      tags.add("tag3")
      tags.add("tag4")
      Lava.instance.track(Track(action = "Interact", category = category))
    } catch (e: Throwable) {
      e.printStackTrace()
    }
  }
}