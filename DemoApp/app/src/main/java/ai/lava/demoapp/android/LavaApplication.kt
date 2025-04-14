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
import com.lava.lavasdk.LavaInitListener
import com.lava.lavasdk.LavaLogLevel
import com.lava.lavasdk.SecureMemberTokenExpiryListener
import com.lava.lavasdk.internal.Style

class LavaApplication : MultiDexApplication(), SecureMemberTokenExpiryListener, LavaInitListener {

    fun initLavaSdk(
        enableSecureMemberToken: Boolean,
        customConsent: Boolean = false,
        handleInitialization: Boolean = false,
    ) {
        if (handleInitialization) {
            Lava.init(
                this,
                BuildConfig.appKey,
                BuildConfig.clientId,
                R.drawable.app_icon_shil.toString(),
                LavaLogLevel.VERBOSE,
                LavaLogLevel.VERBOSE,
                hostAppUIReady = false,
                initListener = this
            )
        } else if (!customConsent) {
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
                },
            )
        } else {
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
        }

        val customStyle = Style()
            .setTitleFont(Typeface.createFromAsset(applicationContext.assets, "fonts/poppins_bold.ttf"))
            .setContentFont(Typeface.createFromAsset(applicationContext.assets, "fonts/poppins_regular.ttf"))
            .setContentTextColor(Color.LTGRAY)
            .setCloseImage(R.drawable.test_close)

        Lava.instance.setCustomStyle(customStyle)

        Lava.instance.registerDeepLinkReceiver(DeepLinkReceiver::class.java)

        if (enableSecureMemberToken) {
            Lava.instance.subscribeSecureMemberTokenExpiry(this)
        } else {
            Lava.instance.setSecureMemberToken(null)
            Lava.instance.unsubscribeSecureMemberTokenExpiry(this)
        }
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        CLog.logLevel = CLog.LogLevel.VERBOSE

        AppSession.init(this)
        initLavaSdk(
            BuildConfig.enableSecureMemberToken.toBoolean(),
            AppSession.instance.getUseCustomConsent(),
            BuildConfig.handleInitialization.toBoolean(),
        )
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

    override fun onCompleted() {
        CLog.i("LAVA SDK: Initialization completed")
    }
}