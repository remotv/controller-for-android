package tv.remo.android.controller.fragments
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import tv.remo.android.controller.R
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.settingsutil.fragments.BasePreferenceFragmentCompat

class SettingsCamera : BasePreferenceFragmentCompat(
        R.xml.settings_camera,
        R.string.cameraSettingsEnableKey
){
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
                super.onCreatePreferences(savedInstanceState, rootKey)
                RemoSettingsUtil.with(context!!){
                        val supportsCamera2 = validateCamera2Support(context!!,
                                it.cameraDeviceId.getPref())
                        val disabledPref = findPreference<Preference>(getString(R.string.camera2features))
                        disabledPref?.isVisible = !supportsCamera2
                        val featureSwitch = findPreference<SwitchPreferenceCompat>(getString(R.string.useCamera2))
                        featureSwitch?.isEnabled = supportsCamera2
                        featureSwitch?.isChecked = supportsCamera2
                        listenAndReplaceIfEmpty(R.string.ffmpegOutputOptionsPrefsKey,
                                R.string.ffmpegDefaultOutputOptions)
                        listenAndReplaceIfEmpty(R.string.ffmpegInputOptionsPrefsKey,
                                R.string.ffmpegDefaultInputOptions)
                }
        }

        private fun listenAndReplaceIfEmpty(prefRes : Int, defaultValue : Int) {
                findPreference<EditTextPreference>(getString(prefRes))
                        ?.setOnPreferenceChangeListener { preference, newValue ->
                        if(newValue.toString().isBlank()){
                                (preference as EditTextPreference).text =
                                        getString(defaultValue)
                                false
                        }
                        else{
                                true
                        }
                }
        }

        private fun validateCamera2Support(context: Context, cameraId: Int): Boolean {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        try {
                                val cm =
                                        (context.getSystemService(Context.CAMERA_SERVICE) as CameraManager)
                                val hardwareLevel = cm.getCameraCharacteristics(
                                        cm.cameraIdList[cameraId]
                                )[CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL]
                                return hardwareLevel != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY
                                        && hardwareLevel != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED
                        } catch (_: Exception) {

                        }
                }
                return false
        }
}
