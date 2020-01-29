package tv.remo.android.controller.fragments
import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import tv.remo.android.controller.R
import tv.remo.android.settingsutil.fragments.BasePreferenceFragmentCompat

class SettingsDisplay : BasePreferenceFragmentCompat(
        R.xml.settings_display
){
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
                super.onCreatePreferences(savedInstanceState, rootKey)
                handleDozeKey()
        }

        private fun handleDozeKey() {
                activity?:return
                val pref = preferenceManager.findPreference<Preference>(
                        getString(R.string.dozeSystemSettingsKey)
                ) ?: return
                val launchIntent = Intent("android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS")
                if(launchIntent.resolveActivity(activity!!.packageManager) == null) return
                pref.intent = launchIntent
                pref.isVisible = true
        }
}
