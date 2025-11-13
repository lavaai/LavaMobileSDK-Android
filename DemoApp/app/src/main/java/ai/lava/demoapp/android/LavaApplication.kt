package ai.lava.demoapp.android

import ai.lava.demoapp.android.api.ApiResultListener
import ai.lava.demoapp.android.api.AuthResponse
import ai.lava.demoapp.android.api.RefreshTokenRequest
import ai.lava.demoapp.android.api.RestClient
import ai.lava.demoapp.android.common.AppSession
import ai.lava.demoapp.android.consent.ConsentUtils
import ai.lava.demoapp.android.deepLink.DeepLinkReceiver
import ai.lava.demoapp.android.utils.CLog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.google.firebase.messaging.FirebaseMessaging
import com.lava.lavasdk.ConsentListener
import com.lava.lavasdk.Lava
import com.lava.lavasdk.LavaConsent
import com.lava.lavasdk.LavaLogLevel
import com.lava.lavasdk.SecureMemberTokenExpiryListener
import com.lava.lavasdk.internal.Style

class LavaApplication : MultiDexApplication(), SecureMemberTokenExpiryListener {

    fun initLavaSDKWithDefaultConfig() {
        Lava.init(this,
            BuildConfig.appKey,
            BuildConfig.clientId,
            smallIcon = R.drawable.app_icon_shil.toString(),
            serverLogLevel = LavaLogLevel.VERBOSE
        )

        val customStyle = Style()
            .setTitleFont(Typeface.createFromAsset(applicationContext.assets, "fonts/poppins_bold.ttf"))
            .setContentFont(Typeface.createFromAsset(applicationContext.assets, "fonts/poppins_regular.ttf"))
            .setContentTextColor(Color.LTGRAY)
            .setCloseImage(R.drawable.test_close)

        Lava.instance.setCustomStyle(customStyle)

        Lava.instance.registerDeepLinkReceiver(DeepLinkReceiver::class.java)
    }

    fun initLavaSDKWithDefaultConsent() {
        Lava.init(
            this,
            BuildConfig.appKey,
            BuildConfig.clientId,
            R.drawable.app_icon_shil.toString(),
            LavaLogLevel.VERBOSE,
            LavaLogLevel.VERBOSE,
            ConsentUtils.getConsentFlags(),
            object: ConsentListener {
                override fun onResult(error: Throwable?, shouldLogout: Boolean) {
                    if (error != null) {
                        // Handle consent error
                        return
                    }
                }
            }
        )

        val customStyle = Style()
            .setTitleFont(Typeface.createFromAsset(applicationContext.assets, "fonts/poppins_bold.ttf"))
            .setContentFont(Typeface.createFromAsset(applicationContext.assets, "fonts/poppins_regular.ttf"))
            .setContentTextColor(Color.LTGRAY)
            .setCloseImage(R.drawable.test_close)

        Lava.instance.setCustomStyle(customStyle)

        Lava.instance.registerDeepLinkReceiver(DeepLinkReceiver::class.java)
    }

    fun initLavaSDKWithCustomConsent() {
        Lava.init(
            this,
            BuildConfig.appKey,
            BuildConfig.clientId,
            R.drawable.app_icon_shil.toString(),
            LavaLogLevel.VERBOSE,
            LavaLogLevel.VERBOSE,
            LavaConsent.OneTrustDefaultConsentMapping,
            ConsentUtils.getCustomConsentFlags(
                BuildConfig.customConsentFlags.toSet()
            ),
            object : ConsentListener {
                override fun onResult(error: Throwable?, shouldLogout: Boolean) {
                    if (error != null) {
                        // Handle consent error
                        return
                    }
                }
            }
        )

        val customStyle = Style()
            .setTitleFont(Typeface.createFromAsset(applicationContext.assets, "fonts/poppins_bold.ttf"))
            .setContentFont(Typeface.createFromAsset(applicationContext.assets, "fonts/poppins_regular.ttf"))
            .setContentTextColor(Color.LTGRAY)
            .setCloseImage(R.drawable.test_close)

        Lava.instance.setCustomStyle(customStyle)

        Lava.instance.registerDeepLinkReceiver(DeepLinkReceiver::class.java)
    }

    fun initLavaSDKWithInvalidConfig() {
        Lava.init(this,
            appKey = "invalid-app-key",
            clientId = "invalid-client-id",
            smallIcon = R.drawable.app_icon_shil.toString(),
            serverLogLevel = LavaLogLevel.VERBOSE
        )

        val customStyle = Style()
            .setTitleFont(Typeface.createFromAsset(applicationContext.assets, "fonts/poppins_bold.ttf"))
            .setContentFont(Typeface.createFromAsset(applicationContext.assets, "fonts/poppins_regular.ttf"))
            .setContentTextColor(Color.LTGRAY)
            .setCloseImage(R.drawable.test_close)

        Lava.instance.setCustomStyle(customStyle)

        Lava.instance.registerDeepLinkReceiver(DeepLinkReceiver::class.java)
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        CLog.logLevel = CLog.LogLevel.VERBOSE

        AppSession.init(this)
        initLavaSDKWithDefaultConfig()
        registerForNotification()
    }

    private fun registerForNotification() {        //FCM
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token: String ->
            CLog.i("FCM Token: $token")
            Lava.instance.setNotificationToken(token)
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onExpire(onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        val email = AppSession.instance.getEmail()
        email?.let {
            val body = RefreshTokenRequest(it)
            RestClient.refreshToken(body, object: ApiResultListener {
                override fun onResult(
                    success: Boolean,
                    authResponse: AuthResponse?,
                    errorMessage: String?
                ) {
                    if (!success) {
                        onError(Error("Not successful"))
                        return
                    }

                    authResponse?.memberToken?.let { token ->
                        Lava.instance.setSecureMemberToken(token)
                    }

                    onSuccess()
                }
            })
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        Lava.instance.unsubscribeSecureMemberTokenExpiry(this)
    }

    companion object {
        lateinit var instance: LavaApplication
    }
}