package tv.remo.android.controller.sdk.models

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by Brendon on 8/2/2019.
 */
class ClassPref(context : Context, sharedPreferences: SharedPreferences, resId: Int, defaultValue: Class<*>) :
    Pref<Class<*>>(context, sharedPreferences, resId, defaultValue) {

    override fun getPref(): Class<*> {
        val clazz = sharedPreferences.getString(key, "")?: ""
        return try{
            Class.forName(clazz)
        }catch (e : ClassNotFoundException){
            defaultValue
        }
    }
}