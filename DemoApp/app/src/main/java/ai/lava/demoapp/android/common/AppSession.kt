package ai.lava.demoapp.android.common

import ai.lava.demoapp.android.consent.ConsentFlag
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

    fun setConsentFlags(value: Set<ConsentFlag>) {
        val editor = sp.edit()
        editor.putStringSet(KeyConsentFlags, value.map { it.flag }.toSet())
        editor.apply()
    }

    fun getConsentFlags(): Set<ConsentFlag> {
        val raw = sp.getStringSet(KeyConsentFlags, ConsentFlag.values().map { it.flag }.toSet())
        return raw!!.mapNotNull { flagString ->
            ConsentFlag.values().firstOrNull { it.flag == flagString }
        }.toSet()
    }

    companion object {
        private const val KeyEmail = "EMAIL"
        private const val KeyConsentFlags = "CONSENT"
        lateinit var instance: AppSession

        fun init(context: Context) {
            instance = AppSession(context)
        }
    }
}