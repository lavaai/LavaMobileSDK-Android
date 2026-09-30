package ai.lava.demoapp.android.deepLink

import ai.lava.demoapp.android.R
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lava.lavasdk.Lava
import com.lava.lavasdk.PassPage

/**
 * Host-owned embed driven by a deep link: [Lava.canHandleDeepLink] +
 * [Lava.handleDeepLinkAsFragment], then the fragment is committed into
 * [R.id.passContainer] the same way as [ai.lava.demoapp.android.embed.EmbedPassDemoActivity].
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

    val clubBody = findViewById<TextView>(R.id.clubBody)
    val clubContent = findViewById<View>(R.id.clubContent)
    val deepLinkForm = findViewById<View>(R.id.deepLinkForm)
    val passContainer = findViewById<View>(R.id.passContainer)
    val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
    val input = findViewById<EditText>(R.id.deepLinkInput)
    val status = findViewById<TextView>(R.id.statusLine)

    showClubTab(R.id.nav_home, clubBody, clubContent, deepLinkForm, passContainer)
    bottomNav.selectedItemId = R.id.nav_home

    bottomNav.setOnItemSelectedListener { item ->
      if (item.itemId == R.id.nav_pass) {
        if (attachedPassFragment() == null) {
          Toast.makeText(this, "Open a pass URL from Home first", Toast.LENGTH_SHORT).show()
          return@setOnItemSelectedListener false
        }
        showPassTab(clubContent, passContainer)
      } else {
        showClubTab(item.itemId, clubBody, clubContent, deepLinkForm, passContainer)
      }
      true
    }

    findViewById<TextView>(R.id.openButton).setOnClickListener {
      hideKeyboard(input)
      openDeepLink(input.text?.toString().orEmpty(), status, bottomNav)
    }

    onBackPressedDispatcher.addCallback(
      this,
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          if (passContainer.visibility == View.VISIBLE) {
            bottomNav.selectedItemId = R.id.nav_home
          } else {
            finish()
          }
        }
      }
    )
  }

  private fun showClubTab(
    itemId: Int,
    clubBody: TextView,
    clubContent: View,
    deepLinkForm: View,
    passContainer: View
  ) {
    deepLinkForm.visibility = if (itemId == R.id.nav_home) View.VISIBLE else View.GONE
    clubBody.text = when (itemId) {
      R.id.nav_matches -> MATCHES_COPY
      R.id.nav_players -> PLAYERS_COPY
      else -> HOME_COPY
    }
    clubContent.visibility = View.VISIBLE
    passContainer.visibility = View.GONE
    attachedPassFragment()?.let { fragment ->
      if (!fragment.isHidden) {
        supportFragmentManager.beginTransaction().hide(fragment).commit()
      }
    }
  }

  private fun showPassTab(clubContent: View, passContainer: View) {
    clubContent.visibility = View.GONE
    passContainer.visibility = View.VISIBLE
    attachedPassFragment()?.let { existing ->
      if (existing.isHidden) {
        supportFragmentManager.beginTransaction().show(existing).commit()
      }
    }
  }

  private fun openDeepLink(
    raw: String,
    status: TextView,
    bottomNav: BottomNavigationView
  ) {
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
      .commitNow()
    bottomNav.selectedItemId = R.id.nav_pass
  }

  private fun attachedPassFragment(): Fragment? {
    val fragment = passFragment
      ?: supportFragmentManager.findFragmentByTag(PASS_TAG)
    if (fragment == null || !fragment.isAdded) {
      passFragment = null
      return null
    }
    passFragment = fragment
    return fragment
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
    private const val HOME_COPY =
      "Welcome back, fan\n\nNext home game\nDucks vs Kings\nSaturday 7:00 PM · Honda Center\n\nSeason\n12–8–2 · 3rd in Pacific"
    private const val MATCHES_COPY =
      "Schedule\n\nSat  ·  Ducks vs Kings  ·  Honda Center\nTue  ·  Ducks @ Sharks  ·  SAP Center\nFri  ·  Ducks vs Oilers  ·  Honda Center\nSun  ·  Ducks @ Knights  ·  T-Mobile Arena"
    private const val PLAYERS_COPY =
      "Roster\n\nC  ·  #11  ·  Trevor Zegras\nW  ·  #38  ·  Ryan Kesler\nD  ·  #4   ·  Cam Fowler\nG  ·  #36  ·  John Gibson"
  }
}
