package ai.lava.demoapp.android.embed

import ai.lava.demoapp.android.R
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.lava.lavasdk.Lava
import com.lava.lavasdk.PassPage

/**
 * Host-owned embed: the pass fragment lives in [R.id.passContainer].
 * Tab changes only hide/show that container — they do not call requestHide.
 */
class EmbedPassDemoActivity : AppCompatActivity() {

  private var passFragment: Fragment? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_embed_pass)

    if (savedInstanceState != null) {
      passFragment = supportFragmentManager.findFragmentByTag(PASS_TAG)
    }

    val clubBody = findViewById<TextView>(R.id.clubBody)
    val clubContent = findViewById<View>(R.id.clubContent)
    val passContainer = findViewById<View>(R.id.passContainer)
    val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

    showClubTab(R.id.nav_home, clubBody, clubContent, passContainer)
    bottomNav.selectedItemId = R.id.nav_home

    bottomNav.setOnItemSelectedListener { item ->
      if (item.itemId == R.id.nav_pass) {
        showPassTab(clubContent, passContainer)
      } else {
        showClubTab(item.itemId, clubBody, clubContent, passContainer)
      }
      true
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
    passContainer: View
  ) {
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

    val existing = attachedPassFragment()
    if (existing == null) {
      val fragment = Lava.instance.createInAppPassFragment(PassPage.PASS)
      passFragment = fragment
      supportFragmentManager.beginTransaction()
        .replace(R.id.passContainer, fragment, PASS_TAG)
        .commit()
    } else if (existing.isHidden) {
      supportFragmentManager.beginTransaction().show(existing).commit()
    }
  }

  /**
   * The SDK can remove the embed fragment (another [Lava.showInAppPass],
   * [Lava.hideInAppPass] on logout, etc.) while this activity still holds a
   * reference. Never hide/show a detached instance.
   */
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

  override fun onDestroy() {
    if (isFinishing) {
      Lava.instance.hideInAppPass(true)
    }
    super.onDestroy()
  }

  companion object {
    private const val PASS_TAG = "embed_pass"
    private const val HOME_COPY =
      "Welcome back, fan\n\nNext home game\nDucks vs Kings\nSaturday 7:00 PM · Honda Center\n\nSeason\n12–8–2 · 3rd in Pacific"
    private const val MATCHES_COPY =
      "Schedule\n\nSat  ·  Ducks vs Kings  ·  Honda Center\nTue  ·  Ducks @ Sharks  ·  SAP Center\nFri  ·  Ducks vs Oilers  ·  Honda Center\nSun  ·  Ducks @ Knights  ·  T-Mobile Arena"
    private const val PLAYERS_COPY =
      "Roster\n\nC  ·  #11  ·  Trevor Zegras\nW  ·  #38  ·  Ryan Kesler\nD  ·  #4   ·  Cam Fowler\nG  ·  #36  ·  John Gibson"
  }
}
