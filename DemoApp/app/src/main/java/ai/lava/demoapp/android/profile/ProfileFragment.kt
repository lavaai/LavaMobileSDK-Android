package ai.lava.demoapp.android.profile

import ai.lava.demoapp.android.R
import ai.lava.demoapp.android.utils.CLog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.lava.lavasdk.*

class ProfileFragment : Fragment(), View.OnClickListener {
  private var tvFirstName: TextView? = null
  private var tvLastName: TextView? = null
  private var tvPhoneNumber: TextView? = null
  private var tvEmailId: TextView? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Lava.instance.track(
      Track(
        Track.ACTION_VIEW_SCREEN,
        "ProfileScreen",
        null, null, null, null, null
      )
    )
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_profile, container, false)
    initUI(view)
    return view
  }

  private fun initUI(view: View) {
    tvFirstName = view.findViewById(R.id.tv_first_name)
    tvLastName = view.findViewById(R.id.tv_last_name)
    tvPhoneNumber = view.findViewById(R.id.tv_phone_number)
    tvEmailId = view.findViewById(R.id.tv_email_id)
    view.findViewById<View>(R.id.tvInAppPass).setOnClickListener(this)
  }

  private fun setUpUI() {
    Lava.instance.getProfile(object : ProfileListener {
      override fun onProfile(success: Boolean, message: String, profile: UserProfile?) {
        if (success) {
          setUIData(profile)
        }
      }
    })
  }

  override fun onResume() {
    super.onResume()

    setUpUI()
  }

  private fun setUIData(profile: UserProfile?) {
    if (profile == null) {
      CLog.e("Profile is null")
      return
    }

    profile.firstName?.let {
      tvFirstName?.text = it
    }

    profile.lastName?.let {
      tvLastName?.text = it
    }

    profile.phoneNumber?.let {
      tvPhoneNumber?.text = it
    }

    Lava.instance.getUser()?.email?.let {
      tvEmailId?.text = it
    }
  }

  override fun onClick(view: View) {
    when (view.id) {
      R.id.tvInAppPass -> {
        activity?.let{ Lava.instance.showInAppPass(it, "test-app-token") }
      }
    }
  }
}