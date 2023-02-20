package ai.lava.demoapp.android.auth

import ai.lava.demoapp.android.MainActivity
import ai.lava.demoapp.android.R
import ai.lava.demoapp.android.utils.GenericUtils
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.lava.lavasdk.Lava
import com.lava.lavasdk.Track

class AccountSettingsFragment : Fragment(), View.OnClickListener {
  private var tvEditProfile: TextView? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Lava.instance.track(Track(Track.ACTION_VIEW_SCREEN, "AccountSettingsScreen", null, null, null, null, null))
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_account_settings, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initUI(view)
    try {
      setUpUI()
    } catch (e: Throwable) {
      e.printStackTrace()
    }
  }

  private fun initUI(view: View) {
    tvEditProfile = view.findViewById(R.id.tv_edit_profile)
  }

  private fun setUpUI() {
    tvEditProfile!!.setOnClickListener(this)
  }

  override fun onClick(v: View) {
    val id = v.id
    activity?.let { GenericUtils.hideKeyboard(it)}
    when (id) {
      R.id.tv_edit_profile -> (activity as MainActivity?)!!.addEditProfileFragment()
      else -> {}
    }
  }
}