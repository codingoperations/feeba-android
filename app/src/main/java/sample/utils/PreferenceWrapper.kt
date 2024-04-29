package sample.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

object PreferenceWrapper {
    private lateinit var preference: SharedPreferences

    fun init(context: Context) {
        preference = PreferenceManager.getDefaultSharedPreferences(context);
    }

    var isProd: Boolean
        get() = preference.getBoolean("isProd", false)
        set(value) = preference.edit().putBoolean("isProd", value).apply();

    var langCode: String
        get() = preference.getString("langCode", "en") ?: "en"
        set(value) = preference.edit().putString("langCode", value).apply();

    var jwtToken: String
        get() = preference.getString("jwtToken", "") ?: ""
        set(value) = preference.edit().putString("jwtToken", value).apply();

}