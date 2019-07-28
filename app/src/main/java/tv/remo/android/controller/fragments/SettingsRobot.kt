package tv.remo.android.controller.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.preference.Preference
import tv.remo.android.controller.R
import tv.remo.android.settingsutil.fragments.BasePreferenceFragmentCompat
import tv.remo.android.settingsutil.preferences.ListSettingsPreference

class SettingsRobot : BasePreferenceFragmentCompat(
        R.xml.settings_robot,
        R.string.robotSettingsEnableKey
){
    private var pendingResultCode: Int? = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val connPref = findPreference<ListSettingsPreference>(getString(R.string.robotConnectionTypeKey))
        val protoPref = findPreference<ListSettingsPreference>(getString(R.string.robotProtocolTypeKey))
        //createFromDefaultAndListen(connPref, LRPreferences.INSTANCE.communication) //TODO
        //createFromDefaultAndListen(protoPref, LRPreferences.INSTANCE.protocol) //TODO
//        connPref?.setOnClickListener {
//            val enum = LRPreferences.INSTANCE.communication.value //TODO
//            val clazz = enum.getInstantiatedClass
//            pendingResultCode = clazz?.setupComponent(activity!!)
//        }

//        protoPref?.setOnClickListener {
//              TODO()
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(pendingResultCode == resultCode)
            Toast.makeText(context, "Success", Toast.LENGTH_LONG).show()
    }

    /*TODO
        private fun <T : Enum<*>> createFromDefaultAndListen(pref : ListPreference?, lrPreference: EnumPreference<T>){
        pref ?: return //skip if null
        val enumValue = lrPreference.value
        pref.entries = enumValue.getEntries()
        pref.entryValues = enumValue.getEntries()
        pref.value = enumValue.name
        maybeDisplayExpandedSetup(pref, enumValue)
        pref.setOnPreferenceChangeListener { preference, newValue ->
            val any = searchForEnum(newValue, enumValue)
            any?.let { lrPreference.saveValue(it) }
            maybeDisplayExpandedSetup(preference, enumValue)
            true
        }
    }*/

    private fun maybeDisplayExpandedSetup(preference: Preference?, enumValue : Enum<*>) {
        val settingsPref = preference as? ListSettingsPreference
        settingsPref?.let {
            it.hideSecondTarget = !hasSetup(enumValue)
        }
    }

    private fun hasSetup(enumValue : Enum<*>) : Boolean{
        return when(enumValue){
            /*TODO is CommunicationType -> {
                val clazz = enumValue.getInstantiatedClass
                return clazz?.usesCustomSetup() == true //because result could be null
            }*/
//            RobotConfig.protocol -> TODO()
            else -> false
        }
    }

    private fun searchForEnum(newValue: Any?, enumValue: Enum<*>): Enum<*>? {
        for(value in enumValue::class.java.enumConstants){
            if(value.name == newValue)
                return value
        }
        return null
    }
}
