package tv.remo.android.controller.sdk.models

import android.content.Context
import android.content.SharedPreferences

class IntPref(
    context : Context,
    sharedPreferences: SharedPreferences,
              resId: Int,
              defaultValue: Int
) : Pref<Int>(context, sharedPreferences, resId, defaultValue) {
    override fun getPref(): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }
}
