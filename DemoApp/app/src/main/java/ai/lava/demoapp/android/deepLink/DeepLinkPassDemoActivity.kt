package ai.lava.demoapp.android.deepLink

import ai.lava.demoapp.android.R
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.lava.lavasdk.Lava
import com.lava.lavasdk.PassPage

/**
 * Host-owned pass deep link tester: paste a URL, then
 * [Lava.canHandleDeepLink] + [Lava.handleDeepLinkAsFragment].
 */
class DeepLinkPassDemoActivity : AppCompatActivity() {

  private var passFragment: Fragment? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_deep_link_pass)

    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

    if (savedInstanceState != null) {
      passFragment = supportFragmentManager.findFragmentByTag(PASS_TAG)
    }

    val input = findViewById<EditText>(R.id.deepLinkInput)
    val status = findViewById<TextView>(R.id.statusLine)
    findViewById<TextView>(R.id.openButton).setOnClickListener {
      hideKeyboard(input)
      openDeepLink(input.text?.toString().orEmpty(), status)
    }
  }

  private fun openDeepLink(raw: String, status: TextView) {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) {
      status.text = "canHandle=false · empty link"
      Toast.makeText(this, "SDK cannot handle this link", Toast.LENGTH_SHORT).show()
      return
    }

    val forCanHandle = if (trimmed.contains("://")) trimmed else "lavademo://$trimmed"
    val canHandle = Lava.instance.canHandleDeepLink(forCanHandle)
    val page = PassPage.parseFromDeepLink(trimmed)
    status.text = "canHandle=$canHandle · page=${page.rawValue}"

    if (!canHandle) {
      Toast.makeText(this, "SDK cannot handle this link", Toast.LENGTH_SHORT).show()
      return
    }

    val fragment = Lava.instance.handleDeepLinkAsFragment(trimmed)
    if (fragment == null) {
      Toast.makeText(this, "This is not a pass UI link", Toast.LENGTH_SHORT).show()
      return
    }

    passFragment = fragment
    supportFragmentManager.beginTransaction()
      .replace(R.id.passContainer, fragment, PASS_TAG)
      .commit()
  }

  private fun hideKeyboard(input: EditText) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(input.windowToken, 0)
  }

  override fun onDestroy() {
    if (isFinishing) {
      Lava.instance.hideInAppPass(true)
    }
    super.onDestroy()
  }

  companion object {
    private const val PASS_TAG = "deep_link_pass"
  }
}
