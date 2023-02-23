package ai.lava.demoapp.android.auth

import ai.lava.demoapp.android.R
import ai.lava.demoapp.android.debug.DebugActivity
import ai.lava.demoapp.android.utils.CLog
import ai.lava.demoapp.android.utils.GenericUtils
import ai.lava.demoapp.android.utils.ProgressUtils
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import com.lava.lavasdk.Lava
import com.lava.lavasdk.ResultListener
import com.lava.lavasdk.Track

class SignInFragment : Fragment(), View.OnClickListener {
  private var btLogin: Button? = null
  private var etUserName: EditText? = null
  private var btShowDebugInfo: Button? = null
  private var mUserName: String? = null
  private var rlViewContainer: RelativeLayout? = null
  private var remaining = 0

  var commonLoginListener: ResultListener = object : ResultListener {
    override fun onResult(success: Boolean, message: String) {
      ProgressUtils.cancel()
      CLog.d("onLogin() called with: success = [$success], message = [$message]")

      if (success) {
        (activity as LoginActivity?)!!.launchMainActivity()
      } else {
        GenericUtils.displayToast(activity, message)
      }
    }
  }
  private var btInboxMessage: Button? = null
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Lava.instance.track(Track(action = Track.ACTION_VIEW_SCREEN, category = "SignInScreen"))
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_sign_in, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initUI(view)
    setUpUI()
  }

  private fun initUI(view: View) {
    btLogin = view.findViewById(R.id.bt_login)
    btShowDebugInfo = view.findViewById(R.id.bt_show_debug)
    btInboxMessage = view.findViewById(R.id.bt_inbox_message)
    etUserName = view.findViewById(R.id.et_email)
    rlViewContainer = view.findViewById(R.id.rl_login_container)
  }

  private fun setUpUI() {
    btLogin!!.setOnClickListener(this)
    btShowDebugInfo!!.setOnClickListener(this)
    btInboxMessage!!.setOnClickListener(this)
    rlViewContainer!!.setOnClickListener(this)
  }

  override fun onClick(v: View) {
    activity?.let { GenericUtils.hideKeyboard(it)}

    when (v.id) {
      R.id.bt_login -> doLogin()
      R.id.bt_show_debug -> showDebugInfo()
      R.id.bt_inbox_message -> (activity as LoginActivity?)!!.addInboxMessageScreen()
      R.id.rl_login_container -> {
        remaining--
        if (remaining <= 0) {
          remaining = 3
        }
      }
      else -> {}
    }
  }

  private fun showDebugInfo() {
    DebugActivity.showDebugInfo(activity)
  }

  private fun doLogin() {
    mUserName = etUserName?.text?.toString()
    if (!isValidEmail(mUserName)) {
      etUserName!!.error = "Please enter valid email"
      return
    }
    ProgressUtils.showProgress(activity, false)
    Lava.instance.setEmail(mUserName, commonLoginListener)
  }

  override fun onResume() {
    super.onResume()
    remaining = 3
  }

  companion object {
    fun isValidEmail(target: String?): Boolean {
      return target != null && target.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }
  }
}