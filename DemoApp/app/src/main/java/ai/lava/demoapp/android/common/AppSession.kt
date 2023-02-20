package ai.lava.demoapp.android.common

import android.content.Context
import android.content.SharedPreferences

internal class AppSession(context: Context) {

    private val spName = "LAVA_DEMO"
    private val KEY_EMAIL = "EMAIL"

    private val sp = appPreference(context)

    private fun appPreference(context: Context): SharedPreferences = context.getSharedPreferences(spName, Context.MODE_PRIVATE)

    fun getEmail(): String? = sp.getString(KEY_EMAIL, null)

    fun setEmail(value: String) {
        val editor = sp.edit()
        editor.putString(KEY_EMAIL, value)
        editor.apply()
    }

    companion object {
        lateinit var instance: AppSession

        fun init(context: Context) {
            instance = AppSession(context)
        }
    }
}