package ai.lava.demoapp.android.profile

import ai.lava.demoapp.android.MainActivity
import ai.lava.demoapp.android.R
import ai.lava.demoapp.android.utils.ProgressUtils
import ai.lava.demoapp.android.utils.CLog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.lava.lavasdk.*
import java.util.concurrent.atomic.AtomicInteger

class EditProfileFragment : Fragment(), View.OnClickListener {
  private var etFirstName: EditText? = null
  private var etLastName: EditText? = null
  private var etPhoneNumber: EditText? = null
  private var uploadApiCallCount = AtomicInteger(0)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Lava.instance.track(Track(Track.ACTION_VIEW_SCREEN, "EditProfileScreen", null, null, null, null, null))
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)
    initUI(view)
    setUpUI()
    return view
  }

  private fun initUI(view: View) {
    etFirstName = view.findViewById(R.id.et_first_name)
    etLastName = view.findViewById(R.id.et_last_name)
    etPhoneNumber = view.findViewById(R.id.et_phone_number)
  }

  private fun setUpUI() {
    (activity as MainActivity?)!!.setEditProfileNotifier(object : NotifierListener {
      override fun onSaveClicked() {
        ProgressUtils.showProgress(activity)
        uploadApiCallCount = AtomicInteger(0)
        uploadProfileData()
      }
    })

    Lava.instance.getProfile(object : ProfileListener {
      override fun onProfile(success: Boolean, message: String, profile: UserProfile?) {
        if (success) {
          profile?.firstName?.let{ etFirstName?.setText(it) }
          profile?.lastName?.let{ etLastName?.setText(it) }
          profile?.phoneNumber?.let { etPhoneNumber?.setText(it) }
        }
      }
    })
  }

  private fun uploadProfileData() {
    val profile = UserProfile(
      etFirstName?.text?.toString(),
      etLastName?.text?.toString(),
      etPhoneNumber?.text.toString()
    )

    uploadApiCallCount.incrementAndGet()

    Lava.instance.updateProfile(profile, object : ProfileListener {
      override fun onProfile(success: Boolean, message: String, profile: UserProfile?) {
        CLog.d("uploadApiCallCount$uploadApiCallCount")
        uploadApiCallCount.decrementAndGet()
        if (uploadApiCallCount.toInt() <= 0) {
          ProgressUtils.cancel()
          (activity as MainActivity?)!!.removeAllFragmentAndReplaceWithProfileFragment()
        }
      }
    })
  }

  override fun onClick(v: View) {}

  override fun onPause() {
    super.onPause()
    (activity as? MainActivity?)?.makeToolbarSaveButtonInvisible()
  }

  override fun onResume() {
    super.onResume()
    (activity as? MainActivity?)?.makeToolbarSaveButtonVisible()
  }

  interface NotifierListener {
    fun onSaveClicked()
  }
}