package tv.remo.android.controller.fragments

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.Preference
import org.btelman.controlsdk.hardware.interfaces.DriverComponent
import org.btelman.controlsdk.hardware.interfaces.TranslatorComponent
import org.btelman.controlsdk.utils.ClassScanner
import tv.remo.android.controller.R
import tv.remo.android.settingsutil.fragments.BasePreferenceFragmentCompat
import tv.remo.android.settingsutil.preferences.ListSettingsPreference
import java.util.*
import kotlin.collections.ArrayList

class SettingsRobot : BasePreferenceFragmentCompat(
        R.xml.settings_robot,
        R.string.robotSettingsEnableKey
){
    private var pendingResultCode: Int? = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val connPref = findPreference<ListSettingsPreference>(getString(R.string.robotConnectionTypeKey))
        val protoPref = findPreference<ListSettingsPreference>(getString(R.string.robotProtocolTypeKey))
        createFromDefaultAndListen(connPref, DriverComponent::class.java)
        createFromDefaultAndListen(protoPref, TranslatorComponent::class.java)
        connPref?.setOnClickListener {
            //val enum = LRPreferences.INSTANCE.communication.value //TODO
            //val clazz = enum.getInstantiatedClass
            //pendingResultCode = clazz?.setupComponent(activity!!)
        }

        protoPref?.setOnClickListener {
              TODO()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(pendingResultCode == resultCode)
            Toast.makeText(context, "Success", Toast.LENGTH_LONG).show()
    }

    private fun <T : Annotation> createFromDefaultAndListen(pref : ListPreference?, annotation: Class<T>){
        pref ?: return //skip if null
        val classes = ClassScanner.getClassesWithAnnotation(context!!, annotation)
        val classNames = ArrayList<String>()
        val simpleClassNames = ArrayList<String>()
        classes.forEach {
            var deviceSupportsHardware = true
            if(annotation == DriverComponent::class.java){
                //check via hardcoding for now.
                if(it.name.toLowerCase(Locale.US).contains("bluetooth")){
                    //driver is a bluetooth driver
                    if(!context!!.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
                        deviceSupportsHardware = false //no support found
                    //TODO check for ble?
                }
            }
            if(deviceSupportsHardware){
                classNames.add(it.name)
                simpleClassNames.add(it.simpleName)
            }
        }
        val nameArray = simpleClassNames.toArray(Array(0){""})
        val valueArray = classNames.toArray(Array(0){""})

        //val enumValue = lrPreference.value
        pref.entries = nameArray
        pref.entryValues = valueArray
        //pref.value = valueArray[0]
        //maybeDisplayExpandedSetup(pref, enumValue)
        /*pref.setOnPreferenceChangeListener { preference, newValue ->
            val any = searchForEnum(newValue, enumValue)
            any?.let { lrPreference.saveValue(it) }
            maybeDisplayExpandedSetup(preference, enumValue)
            true
        }*/
    }

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
