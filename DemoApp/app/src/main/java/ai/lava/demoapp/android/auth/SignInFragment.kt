package ai.lava.demoapp.android.auth

import ai.lava.demoapp.android.BuildConfig
import ai.lava.demoapp.android.LavaApplication
import ai.lava.demoapp.android.R
import ai.lava.demoapp.android.api.ApiResultListener
import ai.lava.demoapp.android.api.AuthResponse
import ai.lava.demoapp.android.api.LoginRequest
import ai.lava.demoapp.android.api.RestClient
import ai.lava.demoapp.android.common.AppSession
import ai.lava.demoapp.android.debug.DebugActivity
import ai.lava.demoapp.android.utils.CLog
import ai.lava.demoapp.android.utils.GenericUtils
import ai.lava.demoapp.android.utils.ProgressUtils
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.lava.lavasdk.Lava
import com.lava.lavasdk.ResultListener
import com.lava.lavasdk.Track

class SignInFragment : Fragment(), View.OnClickListener {
  private var btLogin: Button? = null
  private var etUserName: EditText? = null
  private var etPassword: EditText? = null
  private var btShowDebugInfo: Button? = null
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
    Lava.instance.track(Track(Track.ACTION_VIEW_SCREEN, "SignInScreen", null, null, null, null, null))
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
    etPassword = view.findViewById(R.id.et_password)
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
    if (BuildConfig.enableSecureMemberToken.toBoolean()) {
      loginWithAppBackend()
    } else {
      loginWithLavaSdk()
    }
  }

  fun loginWithAppBackend() {
    val username = etUserName?.text?.toString()
    if (!isValidEmail(username)) {
      etUserName!!.error = "Please enter valid email"
      return
    }

    val password = etPassword?.text?.toString()
    if (password.isNullOrBlank()) {
      etPassword?.error = "Please enter valid password"
      return
    }

    ProgressUtils.showProgress(activity, false)

    val body = LoginRequest(email = username!!, password = password!!)

    RestClient.login(body, object: ApiResultListener {
      override fun onResult(success: Boolean, authResponse: AuthResponse?, errorMessage: String?) {
        ProgressUtils.cancel()

        if (!success) {
          // TODO: Report issue
          CLog.d("Error login: ${errorMessage ?: ""}")
          GenericUtils.displayToast(activity, "Error login: ${errorMessage ?: ""}")
          return
        }

        AppSession.instance.setEmail(body.email)

        Lava.instance.setEmail(username, commonLoginListener)

        authResponse?.memberToken?.let {
          Lava.instance.setSecureMemberToken(it)
        }
      }
    })
  }

  fun loginWithLavaSdk() {
    val username = etUserName?.text?.toString()
    ProgressUtils.showProgress(activity, false)
    Lava.instance.setEmail(username, commonLoginListener)
  }

  override fun onResume() {
    super.onResume()
    remaining = 3
  }

  companion object {
    fun isValidEmail(target: String?): Boolean {
      return target != null && target.isNotEmpty()
    }
  }
}