package tv.remo.android.controller.sdk.models

import android.content.Context
import android.content.SharedPreferences

open class StringPref(
    context : Context,
    sharedPreferences: SharedPreferences,
    resId: Int,
    defaultValue: String
) : Pref<String>(context, sharedPreferences, resId, defaultValue) {
    override fun getPref(): String {
        return sharedPreferences.getString(key, defaultValue)?: defaultValue
    }

    override fun setPref(value : String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
}