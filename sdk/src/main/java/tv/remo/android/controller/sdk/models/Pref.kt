package tv.remo.android.controller.sdk.models

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes

/**
 * Preference model for easy retrieval of setting
 */
abstract class Pref<T>(
    context : Context,
    val sharedPreferences: SharedPreferences,
    @StringRes private val resId: Int,
    val defaultValue : T,
    val key : String = context.getString(resId)
) {
    abstract fun getPref() : T
    abstract fun savePref(value : T)
}