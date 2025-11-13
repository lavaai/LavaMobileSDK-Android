package ai.lava.demoapp.android.auth

import ai.lava.demoapp.android.BuildConfig
import ai.lava.demoapp.android.R
import ai.lava.demoapp.android.api.ApiResultListener
import ai.lava.demoapp.android.api.AuthResponse
import ai.lava.demoapp.android.api.LoginRequest
import ai.lava.demoapp.android.api.RestClient
import ai.lava.demoapp.android.common.AppSession
import ai.lava.demoapp.android.consent.ConsentActivity
import ai.lava.demoapp.android.debug.DebugActivity
import ai.lava.demoapp.android.initOptions.InitOptionsActivity
import ai.lava.demoapp.android.utils.CLog
import ai.lava.demoapp.android.utils.GenericUtils
import ai.lava.demoapp.android.utils.ProgressUtils
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.lava.lavasdk.Lava
import com.lava.lavasdk.LavaAuthType
import com.lava.lavasdk.ResultListener
import com.lava.lavasdk.Track
import com.lava.lavasdk.internal.nba_id.NBAIDError

class SignInFragment : Fragment(), View.OnClickListener {
  private var etUserName: EditText? = null
  private var etPassword: EditText? = null
  private var btShowDebugInfo: Button? = null

  private var rlViewContainer: RelativeLayout? = null
  private var pbAnonymousLogin: ProgressBar? = null
  private var tvAnonymous: TextView? = null
  private var tvFailedAnonymous: TextView? = null
  private var rgExternalSystem: RadioGroup? = null
  private var remaining = 0

  var commonLoginListener: ResultListener = object : ResultListener {
    override fun onResult(success: Boolean, message: String) {
      ProgressUtils.cancel()
      CLog.d("onLogin() called with: success = [$success], message = [$message]")

      if (success) {
        (activity as LoginActivity?)!!.launchMainActivity()
      } else {
        val displayErrorMessage = when (message) {
          NBAIDError.NBA_ID_00 -> "NBA ID service not configured for this environment"
          NBAIDError.NBA_ID_01 -> "Failed to acquire an NBA service token"
          NBAIDError.NBA_ID_02 -> "NBA account not found"
          NBAIDError.NBA_ID_03 -> "Ticketmaster account not linked"
          else -> message
        }
        GenericUtils.displayToast(activity, displayErrorMessage)
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

  override fun onStart() {
    super.onStart()
    loginAnonymous()
  }

  private fun initUI(view: View) {
    btShowDebugInfo = view.findViewById(R.id.btnShowDebug)
    btInboxMessage = view.findViewById(R.id.btnInboxMessage)
    etUserName = view.findViewById(R.id.et_email)
    rlViewContainer = view.findViewById(R.id.rl_login_container)
    pbAnonymousLogin = view.findViewById(R.id.pbAnonymousLogin)
    tvAnonymous = view.findViewById(R.id.tvAnonymous)
    tvFailedAnonymous = view.findViewById(R.id.tvFailedAnonymous)
    rgExternalSystem = view.findViewById(R.id.rgExternalSystem)
  }

  private fun setUpUI() {
    view?.findViewById<TextView>(R.id.bt_login)?.setOnClickListener(this)
    view?.findViewById<TextView>(R.id.btnShowPass)?.setOnClickListener(this)
    view?.findViewById<TextView>(R.id.btnConsentPref)?.setOnClickListener(this)
    view?.findViewById<TextView>(R.id.btnInitOptions)?.setOnClickListener(this)
    btShowDebugInfo!!.setOnClickListener(this)
    btInboxMessage!!.setOnClickListener(this)
    rlViewContainer!!.setOnClickListener(this)
    rgExternalSystem!!.setOnCheckedChangeListener { group, checkId ->
      when (checkId) {
        R.id.radioEmail -> {
          etUserName?.setText("app.001@lava.ai")
        }
        R.id.radioExternalID -> {
          etUserName?.setText("123123123")
        }
      }

    }
  }

  override fun onClick(v: View) {
    activity?.let { GenericUtils.hideKeyboard(it)}

    when (v.id) {
      R.id.bt_login -> doLogin()
      R.id.btnShowDebug -> showDebugInfo()
      R.id.btnInboxMessage -> (activity as LoginActivity?)!!.addInboxMessageScreen()
      R.id.rl_login_container -> {
        remaining--
        if (remaining <= 0) {
          remaining = 3
        }
      }
      R.id.btnShowPass -> Lava.instance.showInAppPass(requireContext())
      R.id.btnConsentPref -> {
        val intent = Intent(context, ConsentActivity::class.java)
        startActivity(intent)
      }

      R.id.btnInitOptions -> {
        val intent = Intent(context, InitOptionsActivity::class.java)
        startActivity(intent)
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

  private fun loginAnonymous() {
    Lava.instance.setEmail(null, object: ResultListener {
      override fun onResult(success: Boolean, message: String) {
        if (success) {
          tvAnonymous?.visibility = View.VISIBLE
          tvFailedAnonymous?.visibility = View.GONE
          pbAnonymousLogin?.visibility = View.GONE
        } else {
          tvAnonymous?.visibility = View.GONE
          tvFailedAnonymous?.visibility = View.VISIBLE
          pbAnonymousLogin?.visibility = View.GONE
        }
      }
    })
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

    val body = LoginRequest(email = username!!, password = password)

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
    when (rgExternalSystem?.checkedRadioButtonId) {
      R.id.radioEmail -> Lava.instance.setEmail(username, commonLoginListener)
      R.id.radioExternalID -> Lava.instance.setUserId(username, LavaAuthType.NBA_ID, commonLoginListener)
    }
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