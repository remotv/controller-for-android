package tv.remo.android.controller.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import tv.remo.android.controller.R
import tv.remo.android.controller.models.Licenses

class SettingsLicenseViewer : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_licenses, rootKey)
        Licenses.licenses.forEach { license ->
            val pref = Preference(context).also { preference ->
                preference.title = license.name
                preference.intent = Intent(Intent.ACTION_VIEW, Uri.parse(license.licenseLink))
            }
            preferenceScreen.addPreference(pref)
        }
    }
}
