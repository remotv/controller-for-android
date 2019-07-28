package tv.remo.android.controller.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.navigation.Navigation
import androidx.preference.Preference
import tv.remo.android.controller.R
import tv.remo.android.settingsutil.fragments.BasePreferenceFragmentCompat


class SettingsLanding : BasePreferenceFragmentCompat(
        R.xml.settings_landing_options
) {
    /**
     * Settings navigation links are placed here. This requires that the key is a StringId
     */
    private val linkedPageSet = HashMap<@IdRes Int, @IdRes Int>().also {
        it[R.string.connectionSettingsKey] = R.id.action_settingsLanding_to_settingsConnection
        it[R.string.robotSettingsEnableKey] = R.id.action_settingsLanding_to_settingsRobot
        it[R.string.cameraSettingsEnableKey] = R.id.action_settingsLanding_to_settingsCamera
        it[R.string.microphoneSettingsEnableKey] = R.id.action_settingsLanding_to_settingsMicrophone
        it[R.string.audioSettingsEnableKey] = R.id.action_settingsLanding_to_settingsAudio
        it[R.string.displaySettingsKey] = R.id.action_settingsLanding_to_settingsDisplay
        it[R.string.openSourceSettingsKey] = R.id.action_settingsLanding_to_licenseViewer
    }

    /**
     * This converts the linkedPageSet keys to strings for easy access
     */
    private val dict = HashMap<String, @IdRes Int>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateDictFromLinkedPageSet()
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        dict[preference?.key]?.let {
            navigate(it)
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun navigate(@IdRes resId : Int){
        Navigation.findNavController(view!!).navigate(resId)
    }

    private fun populateDictFromLinkedPageSet() {
        linkedPageSet.forEach { entry ->
            val idString = getString(entry.key)
            dict[idString] = entry.value
        }
    }
}
