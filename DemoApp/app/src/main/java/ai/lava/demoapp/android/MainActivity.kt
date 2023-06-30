package ai.lava.demoapp.android

import ai.lava.demoapp.android.analytics.AnalyticsDemoFragment
import ai.lava.demoapp.android.auth.AccountSettingsFragment
import ai.lava.demoapp.android.auth.LoginActivity
import ai.lava.demoapp.android.debug.DebugActivity
import ai.lava.demoapp.android.debug.DebugFragment
import ai.lava.demoapp.android.inbox.NotificationInboxFragment
import ai.lava.demoapp.android.profile.EditProfileFragment
import ai.lava.demoapp.android.profile.EditProfileFragment.NotifierListener
import ai.lava.demoapp.android.profile.ProfileFragment
import ai.lava.demoapp.android.utils.GenericUtils
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.lava.lavasdk.Lava

class MainActivity : BaseActivity(), View.OnClickListener {
    private var mDrawerLayout: DrawerLayout? = null
    private var mDrawerToggle: ActionBarDrawerToggle? = null
    private var tvAccountSetting: TextView? = null
    private var tvLogOut: TextView? = null
    private var leftDrawer: ScrollView? = null
    private var toolbar: Toolbar? = null
    private var toolbarTitle: TextView? = null
    private var tvProfile: TextView? = null
    private var tvPrevious: TextView? = null
    private var notifierListener: NotifierListener? = null
    private var tvDebug: TextView? = null
    private var tvShowDebugInfo: TextView? = null
    private var tvDemo: TextView? = null
    private var tvInboxMessage: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isAlive = true
        setContentView(R.layout.activity_main)
        changeStatusBarColor()
        initUI()
        setUpUI()
    }

    private fun setUpUI() {
        tvProfile!!.setOnClickListener(this)
        tvAccountSetting!!.setOnClickListener(this)
        tvLogOut!!.setOnClickListener(this)
        tvDebug!!.setOnClickListener(this)
        tvShowDebugInfo!!.setOnClickListener(this)
        tvDemo!!.setOnClickListener(this)
        tvInboxMessage!!.setOnClickListener(this)
        setToolbarTile(R.id.tv_profile)
        tvPrevious = tvProfile
        changeTextColor(tvProfile)
        toolbar!!.findViewById<View>(R.id.toolbar_save).setOnClickListener(this)
    }

    private fun initUI() {
        toolbar = findViewById(R.id.toolbar)
        toolbarTitle = toolbar?.findViewById(R.id.toolbar_title)
        tvProfile = findViewById(R.id.tv_profile)
        tvAccountSetting = findViewById(R.id.tv_account_setting)
        tvLogOut = findViewById(R.id.tv_log_out)
        leftDrawer = findViewById(R.id.left_drawer)
        tvDebug = findViewById(R.id.tv_debug)
        tvShowDebugInfo = findViewById(R.id.tv_show_debug)
        tvDemo = findViewById(R.id.tv_demo)
        tvInboxMessage = findViewById(R.id.tv_inbox_message)
        mDrawerLayout = findViewById(R.id.drawer_layout)
        val version = findViewById<TextView>(R.id.tv_version)
        version.text = "Version " + GenericUtils.getVersionName(this)
        setSupportActionBar(toolbar)
        setupDrawer()
    }

    override fun onStart() {
        super.onStart()
        if (selectedTab == -1) {
            selectedTab = getIntent().getIntExtra("prevClickedId", -1)
        }

        if (selectedTab != -1) {
            selectTile(selectedTab)
            return
        }

        selectTile(R.id.tv_profile)
    }

    private fun setToolbarTile(id: Int) {
        when (id) {
            R.id.tv_account_setting -> toolbarTitle?.setText(R.string.account_settings)
            R.id.tv_log_out -> toolbarTitle?.setText(R.string.logout)
            R.id.tv_profile -> toolbarTitle?.setText(R.string.profile)
            R.id.tv_debug -> toolbarTitle?.setText(R.string.tv_debug)
            R.id.tv_demo -> toolbarTitle?.setText(R.string.analytics)
            R.id.tv_inbox_message -> toolbarTitle?.setText(R.string.inboxmessage)
        }
    }

    fun addFragments(fragment: Fragment, isAdd: Boolean, isBackStack: Boolean) {
        try {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            if (isAdd) fragmentTransaction.add(
                    R.id.fragment_container,
                    fragment,
                    fragment.javaClass.simpleName
            ) else fragmentTransaction.replace(
                    R.id.fragment_container,
                    fragment,
                    fragment.javaClass.simpleName
            )
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

    private fun setupDrawer() {
        mDrawerToggle = object : ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                invalidateOptionsMenu()
            }

            override fun onDrawerClosed(view: View) {
                super.onDrawerClosed(view)
                invalidateOptionsMenu()
            }
        }
        mDrawerToggle?.setDrawerIndicatorEnabled(true)
        mDrawerToggle?.let { mDrawerLayout?.addDrawerListener(it) }
        mDrawerToggle?.syncState()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("prevClickedId", selectedTab)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mDrawerToggle?.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle?.onConfigurationChanged(newConfig)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.action_settings)
        item.isVisible = false
        super.onPrepareOptionsMenu(menu)
        return true
    }

    override fun onClick(v: View) {
        if (selectedTab == v.id) {
            closeDrawer()
            return
        }
        changeTextColor(v)
        selectTile(v.id)
    }

    private fun selectTile(id: Int) {
        GenericUtils.hideKeyboard(this)
        setToolbarTile(id)
        selectedTab = id
        when (id) {
            R.id.tv_account_setting -> {
                closeDrawer()
                removeAllFragments()
                addFragments(AccountSettingsFragment(), false, false)
            }
            R.id.tv_log_out -> {
                closeDrawer()
                openLogoutDialog()
                selectedTab = -1
            }
            R.id.tv_profile -> {
                closeDrawer()
                removeAllFragments()
                addFragments(ProfileFragment(), false, false)
            }
            R.id.toolbar_save -> if (notifierListener != null) {
                notifierListener!!.onSaveClicked()
            }
            R.id.tv_debug -> {
                closeDrawer()
                removeAllFragments()
                addFragments(DebugFragment(), false, false)
            }
            R.id.tv_show_debug -> {
                closeDrawer()
                DebugActivity.showDebugInfo(this)
                selectedTab = -1
            }
            R.id.tv_demo -> {
                closeDrawer()
                removeAllFragments()
                addFragments(AnalyticsDemoFragment(), false, false)
            }
            R.id.tv_inbox_message -> {
                closeDrawer()
                removeAllFragments()
                addFragments(NotificationInboxFragment(), false, false)
            }
            else -> {
            }
        }
    }

    private fun doLogout() {
        val user = Lava.instance.getUser()
        if (user?.isNormalUser() == true) {
            //ProgressUtils.showProgress(this);
            localLogout()
            launchLoginActivity()
        }
    }

    private fun localLogout() {
        Lava.instance.setEmail(null, null)
    }

    fun addEditProfileFragment() {
        val fragment = supportFragmentManager.findFragmentByTag(
                EditProfileFragment::class.java.simpleName
        ) ?: EditProfileFragment()

        if (!fragment.isAdded) {
            addFragments(fragment, false, true)
        }
    }

    fun removeAllFragmentAndReplaceWithProfileFragment() {
        try {
            onBackPressed()
            val fragment = supportFragmentManager.findFragmentByTag(
                    EditProfileFragment::class.java.canonicalName
            )
            if (fragment != null) supportFragmentManager.beginTransaction().remove(fragment).commit()
            tvProfile?.let { onClick(it) }
            mDrawerToggle?.syncState()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun closeDrawer() {
        mDrawerLayout?.postDelayed({
            try {
                if (mDrawerLayout != null) {
                    mDrawerLayout?.closeDrawer(leftDrawer!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 350)
    }

    // Launch Login screen
    fun launchLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        finish()
        startActivity(intent)
    }

    private fun changeTextColor(view: View?) {
        if (view is TextView) {
            if (view.getId() == R.id.toolbar_save) {
                return
            }
            tvPrevious?.setTextColor(ContextCompat.getColor(this, R.color.white))
            tvPrevious = view
        }
        if (view is RelativeLayout && view.getChildAt(0) is TextView) {
            val tv = view.getChildAt(0) as TextView
            tvPrevious?.setTextColor(ContextCompat.getColor(this, R.color.white))
            tvPrevious = tv
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isAlive = false
        GenericUtils.hideKeyboard(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun changeStatusBarColor() {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.signinTextBackground)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun setEditProfileNotifier(notifierListener: NotifierListener?) {
        this.notifierListener = notifierListener
    }

    fun makeToolbarSaveButtonInvisible() {
        toolbar!!.findViewById<View>(R.id.toolbar_save).visibility =
                View.INVISIBLE
    }

    fun makeToolbarSaveButtonVisible() {
        toolbar!!.findViewById<View>(R.id.toolbar_save).visibility = View.VISIBLE
    }

    private fun removeAllFragments() {
        try {
            supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            for (fragment in supportFragmentManager.fragments) {
                if (fragment != null) {
                    try {
                        supportFragmentManager.beginTransaction().remove(fragment).commit()
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }

    private fun openLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Do you want to Logout?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            dialog.dismiss()
            doLogout()
        }
        builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    companion object {
        var isAlive = false
        var selectedTab = -1
    }
}