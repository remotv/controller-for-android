package tv.remo.android.controller.sdk.models

import android.content.Context
import android.content.SharedPreferences

/**
 * Get an Integer preference OR parse a string preference as an integer
 */
class IntPref(
    context : Context,
    sharedPreferences: SharedPreferences,
              resId: Int,
              defaultValue: Int
) : Pref<Int>(context, sharedPreferences, resId, defaultValue) {
    override fun getPref(): Int {
        return try {
            sharedPreferences.getInt(key, defaultValue)
        } catch (e: ClassCastException) {
            sharedPreferences.getString(key, defaultValue.toString())?.toInt() ?: defaultValue
        }
    }
}
