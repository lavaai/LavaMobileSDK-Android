package ai.lava.demoapp.android.profile

import ai.lava.demoapp.android.MainActivity
import ai.lava.demoapp.android.R
import ai.lava.demoapp.android.consent.ConsentActivity
import ai.lava.demoapp.android.utils.CLog
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.lava.lavasdk.InboxMessage
import com.lava.lavasdk.InboxMessageListener
import com.lava.lavasdk.Lava
import com.lava.lavasdk.ProfileListener
import com.lava.lavasdk.Track
import com.lava.lavasdk.UserProfile
import com.lava.lavasdk.internal.inbox.InboxStyle

class ProfileFragment : Fragment(), View.OnClickListener {
  private var tvFirstName: TextView? = null
  private var tvLastName: TextView? = null
  private var tvPhoneNumber: TextView? = null
  private var tvEmailId: TextView? = null

  private var consentLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
      result.data?.apply {
        if (getBooleanExtra("SHOULD_LOGOUT", false)) {
          (activity as? MainActivity)?.forceLogout()
        }
      }
    }
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
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
    view.findViewById<View>(R.id.tvSDKInbox).setOnClickListener(this)
    view.findViewById<View>(R.id.tvSDKCustomInbox).setOnClickListener(this)
    view.findViewById<View>(R.id.tvConsentDialog).setOnClickListener(this)
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

      R.id.tvSDKInbox -> {
        activity?.let {
          Lava.instance.showInboxMessages(it)
        }
      }

      R.id.tvSDKCustomInbox -> {
        activity?.let {
          val style = InboxStyle()
            .setTitleTextColor(Color.BLUE)
            .setIndicatorColor(Color.MAGENTA)

          val listener = object: InboxMessageListener {
            override fun onViewMessage(message: InboxMessage) {
              val event = Track()
                .setAction(Track.ACTION_VIEW_SCREEN)
                .setCategory("Inbox")
                .setUserParams(message.toNotificationData())
              Lava.instance.track(event)
            }
          }

          Lava.instance.showInboxMessages(it, listener, style)
        }
      }

      R.id.tvConsentDialog -> {
        val intent = Intent(context, ConsentActivity::class.java)
        consentLauncher.launch(intent)
      }
    }

  }
}