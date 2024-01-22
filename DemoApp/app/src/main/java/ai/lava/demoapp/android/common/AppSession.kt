package ai.lava.demoapp.android.common

import android.content.Context
import android.content.SharedPreferences

internal class AppSession(context: Context) {

    private val spName = "LAVA_DEMO"


    private val sp = appPreference(context)

    private fun appPreference(context: Context): SharedPreferences = context.getSharedPreferences(spName, Context.MODE_PRIVATE)

    fun getEmail(): String? = sp.getString(KeyEmail, null)

    fun setEmail(value: String) {
        val editor = sp.edit()
        editor.putString(KeyEmail, value)
        editor.apply()
    }

    fun setConsentFlags(value: Set<String>) {
        val editor = sp.edit()
        editor.putStringSet(KeyConsentFlags, value)
        editor.apply()
    }

    fun getConsentFlags(): Set<String>? = sp.getStringSet(KeyConsentFlags, null)

    fun setUseCustomConsent(value: Boolean) {
        sp.edit().putBoolean(KeyUseCustomConsent, value).apply()
    }

    fun getUseCustomConsent(): Boolean = sp.getBoolean(KeyUseCustomConsent, false)

    companion object {
        private const val KeyEmail = "EMAIL"
        private const val KeyConsentFlags = "CONSENT"
        private const val KeyUseCustomConsent = "USE_CUSTOM_CONSENT"
        lateinit var instance: AppSession

        fun init(context: Context) {
            instance = AppSession(context)
        }
    }
}