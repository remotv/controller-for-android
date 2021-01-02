package tv.remo.android.controller.sdk.models

import android.content.Context
import android.content.SharedPreferences
import org.btelman.logutil.kotlin.LogUtil

/**
 * Get an Integer preference OR parse a string preference as an integer
 */
class IntPref(
    context: Context,
    sharedPreferences: SharedPreferences,
    resId: Int,
    defaultValue: Int,
    val asString: Boolean = false
) : Pref<Int>(context, sharedPreferences, resId, defaultValue) {

    val log = LogUtil(IntPref::class.java.simpleName)

    override fun getPref(): Int {
        if(!asString){
            try{
                return sharedPreferences.getInt(key, defaultValue)
            } catch (e: java.lang.ClassCastException){
                log.w("Preference not stored as Int. Will attempt to convert from String to int")
            }
        }
        return sharedPreferences.getString(key, defaultValue.toString())?.toIntOrNull() ?: defaultValue
    }

    override fun savePref(value: Int) {
        sharedPreferences.edit().apply{
            if(asString){
                putString(key, value.toString()).apply()
            }
            else{
                putInt(key, value).apply()
            }
        }.apply()
    }
}
