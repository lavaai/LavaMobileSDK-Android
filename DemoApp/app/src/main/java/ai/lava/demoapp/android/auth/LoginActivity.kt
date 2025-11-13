package ai.lava.demoapp.android.auth

import ai.lava.demoapp.android.BaseActivity
import ai.lava.demoapp.android.MainActivity
import ai.lava.demoapp.android.R
import ai.lava.demoapp.android.inbox.NotificationInboxFragment
import ai.lava.demoapp.android.utils.CLog
import ai.lava.demoapp.android.utils.GenericUtils
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class LoginActivity : BaseActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge(
    )
    super.onCreate(savedInstanceState)
    isAlive = true
    setContentView(R.layout.layout_login_activity)
    changeStatusBarColor()
    addSignInFragment()
  }

  private fun addSignInFragment() {
    addFragments(SignInFragment(), false, false)
  }

  fun addFragments(fragment: Fragment, isAdd: Boolean, isBackStack: Boolean) {
    try {
      val fragmentTransaction = supportFragmentManager.beginTransaction()
      if (isAdd) fragmentTransaction.add(R.id.fragment_container, fragment, fragment.javaClass.simpleName)
      else fragmentTransaction.replace(R.id.fragment_container, fragment, fragment.javaClass.simpleName)

      if (isBackStack) fragmentTransaction.addToBackStack(fragment.javaClass.simpleName)
      try {
        fragmentTransaction.commit()
      } catch (e: Throwable) {
        fragmentTransaction.commitAllowingStateLoss()
      }
    } catch (e: Throwable) {
      e.printStackTrace()
    }
  }

  fun addInboxMessageScreen() {
    val fragment = supportFragmentManager.findFragmentByTag(NotificationInboxFragment::class.java.simpleName) ?: NotificationInboxFragment()
    if (!fragment.isAdded) {
      addFragments(fragment, false, true)
    }
  }

  //    public void addBaseUrlFragment() {
  //        Fragment fragment = getSupportFragmentManager().findFragmentByTag(LVBaseUrlFragment.class.getSimpleName());
  //        if (fragment == null)
  //            fragment = new LVBaseUrlFragment();
  //        if (!fragment.isAdded()) {
  //            addFragments(fragment, false, true);
  //        }
  //    }

  fun launchMainActivity() {
    val intent = Intent(this, MainActivity::class.java)
    finish()
    startActivity(intent)
  }

  override fun onDestroy() {
    super.onDestroy()
    isAlive = false
    CLog.d("Login Activity destroyed")
    GenericUtils.hideKeyboard(this)
  }

  private fun changeStatusBarColor() {
    val window = window
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = ContextCompat.getColor(this, R.color.signinTextBackground)
  }

  companion object {
    var isAlive = false
  }
}