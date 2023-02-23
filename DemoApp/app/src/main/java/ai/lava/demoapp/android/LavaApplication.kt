package ai.lava.demoapp.android

import ai.lava.demoapp.android.deepLink.DeepLinkReceiver
import ai.lava.demoapp.android.utils.CLog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.google.firebase.messaging.FirebaseMessaging
import com.lava.lavasdk.Lava
import com.lava.lavasdk.LavaLogLevel
import com.lava.lavasdk.internal.Style

class LavaApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        CLog.logLevel = CLog.LogLevel.VERBOSE

        Lava.init(
            application = this,
            appKey = BuildConfig.appKey,
            clientId = BuildConfig.clientId,
            smallIcon = R.drawable.app_icon_shil.toString(),
            logLevel = LavaLogLevel.VERBOSE
        )

        val customStyle = Style()
            .setTitleFont(Typeface.createFromAsset(applicationContext.assets, "fonts/poppins_bold.ttf"))
            .setContentFont(Typeface.createFromAsset(applicationContext.assets, "fonts/poppins_regular.ttf"))
            .setBackgroundColor(Color.BLACK)
            .setTitleTextColor(Color.GREEN)
            .setContentTextColor(Color.LTGRAY)
            .setCloseImage(R.drawable.test_close)

        Lava.instance.setCustomStyle(customStyle)

        registerForNotification()
        Lava.instance.registerDeepLinkReceiver(DeepLinkReceiver::class.java)
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
}