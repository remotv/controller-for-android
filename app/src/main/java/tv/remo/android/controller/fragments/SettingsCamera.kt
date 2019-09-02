package tv.remo.android.controller.fragments
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
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
                        val switchPref = findPreference<SwitchPreferenceCompat>(getString(R.string.useCamera2))
                        if(!supportsCamera2){
                                switchPref?.isChecked = false
                                switchPref?.isEnabled = false
                        }
                        else{
                                switchPref?.isEnabled = true
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
