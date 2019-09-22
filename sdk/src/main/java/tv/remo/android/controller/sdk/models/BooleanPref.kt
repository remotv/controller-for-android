package tv.remo.android.controller.sdk.models

import android.content.Context
import android.content.SharedPreferences

class BooleanPref(
    context : Context,
    sharedPreferences: SharedPreferences,
    resId: Int,
    defaultValue: Boolean
) : Pref<Boolean>(context, sharedPreferences, resId, defaultValue) {
    override fun getPref(): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
}
