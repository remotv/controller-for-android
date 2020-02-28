package tv.remo.android.controller.fragments
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import tv.remo.android.controller.R
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.utils.ValueUtil
import tv.remo.android.controller.utils.CameraUtil
import tv.remo.android.settingsutil.fragments.BasePreferenceFragmentCompat
import tv.remo.android.settingsutil.interfaces.SwitchBarCapableActivity
import java.util.*

class SettingsCamera : BasePreferenceFragmentCompat(
        R.xml.settings_camera,
        R.string.cameraSettingsEnableKey
) {

    private val cameraId: Int
        get() {
            return RemoSettingsUtil.with(context!!).cameraDeviceId.getPref()
        }

    private val cameraAllowed : Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context!!.checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as SwitchBarCapableActivity).getSwitchBar().apply {
            addOnSwitchChangeListener{ _, isChecked ->
                if(isChecked)
                    checkPermissions()
            }
        }

        RemoSettingsUtil.with(context!!) {
            val currCameraId = it.cameraDeviceId.getPref()
            checkForCamera2SupportAndReact(it.cameraDeviceId.getPref())
            updateUIFromCameraSelection(currCameraId)
            addListeners()
            if(it.cameraEnabled.getPref())
                checkPermissions()
        }
    }

    private fun checkPermissions() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !cameraAllowed){
            preferenceManager.preferenceScreen.isEnabled = false
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 111)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 111){
            if(cameraAllowed){
                preferenceManager.preferenceScreen.isEnabled = true
                updateUIFromCameraSelection(cameraId)
            }
            else{
                Toast.makeText(context!!,
                    "Camera permissions required to modify camera settings!",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addListeners() {
        listenAndReplaceIfEmpty(
            R.string.ffmpegOutputOptionsPrefsKey,
            R.string.ffmpegDefaultOutputOptions
        )
        listenAndReplaceIfEmpty(
            R.string.ffmpegInputOptionsPrefsKey,
            R.string.ffmpegDefaultInputOptions
        )
        listenAndReplaceIfEmpty(
            R.string.ffmpegFilterAddition,
            R.string.ffmpegDefaultFilterOptions, true
        )
        listenToPref<ListPreference>(R.string.cameraDeviceIdKey) { _, value ->
            (value as String).toInt().also { value ->
                updateUIFromCameraSelection(value)
                checkForCamera2SupportAndReact(value)
            }

            true
        }
    }

    private fun updateUIFromCameraSelection(cameraIndex: Int) {
        val sizes = try {
            CameraUtil.getCameraSizes(requireContext(), cameraIndex)
        } catch (e: IndexOutOfBoundsException) {
            ArrayList<Pair<Int, Int>>().also {
                it.add(Pair(640, 480))
            }
        }
        setNewResolutionList(sizes)
    }

    private fun setNewResolutionList(sizes: ArrayList<Pair<Int, Int>>) {
        val resolutionList = findPreference<ListPreference>(
            getString(R.string.cameraResolutionKey)
        ) ?: return

        //check and remove sizes that are known not to work if present
        val blacklist = listOf(Pair(720, 480), Pair(1440, 1080))
        sizes.removeAll { pair ->
            blacklist.contains(pair)
        }
        if(sizes.isEmpty()) {
            //this one always will work.
            // We also need a fallback in case permissions have not been accepted
            sizes.add(Pair(640, 480))
        }

        //init values for listPreference
        val values = initResolutionArray(sizes.size)
        val userFacingValues = initResolutionArray(sizes.size)

        //create a list of known resolutions that are okay. Unknown ones are marked with (not tested)
        val knownWorkingResolutions =
            listOf(Pair(640, 480), Pair(1280, 720), Pair(1280, 960), Pair(1920, 1080))

        //iterate through the array to add sizes to values and userFacingValues arrays
        sizes.forEachIndexed { i, pair ->
            var appendText = ""
            val width = pair.first
            val height = pair.second

            //check for good formats
            if (width == 640 && height == 480)
                appendText = "(recommended)"
            //mark resolutions higher than 1280x720 as (may be slow)
            if (width > 1280 || height > 720)
                appendText = "(may be slow)"
            //mark unknown resolutions as untested
            if (!knownWorkingResolutions.contains(pair))
                appendText = "(not tested)"

            values[i] = "${width}x${height}" //values that will be stored in preferences

            val gcm = ValueUtil.gcm(width, height) //used to find readable ratio
            //create readable value (ex. "640x480(4:3) (recommended)")
            userFacingValues[i] =
                "${width}x${height}(${width / gcm}:${height / gcm}) $appendText"
        }
        //assign values to their respective fields in the resolution ListPreference
        resolutionList.entries = userFacingValues.reversedArray()
        resolutionList.entryValues = values.reversedArray()

        //set current selected resolution to the first in the list if list does not contain it
        if (!resolutionList.entryValues.contains(resolutionList.value))
            resolutionList.value = resolutionList.entryValues[0].toString()
    }

    private fun checkForCamera2SupportAndReact(cameraId: Int): Boolean {
        val supportsCamera2 = validateCamera2Support(
            context!!,
            cameraId
        )
        val disabledPref = findPreference<Preference>(getString(R.string.camera2features))
        disabledPref?.isVisible = !supportsCamera2
        val featureSwitch = findPreference<SwitchPreferenceCompat>(getString(R.string.useCamera2))
        featureSwitch?.isEnabled = supportsCamera2
        featureSwitch?.isChecked = supportsCamera2
        if (!supportsCamera2)
            findPreference<ListPreference>(
                getString(R.string.cameraResolutionKey)
            )?.let {
                it.value = "640x480"
            }
        return supportsCamera2
    }

    private fun listenAndReplaceIfEmpty(
        prefRes: Int,
        defaultValue: Int,
        allowBlank: Boolean = false
    ) {
        listenToPref<EditTextPreference>(prefRes) { preference, newValue ->
            if (newValue.toString().isBlank() && !allowBlank || newValue.toString().isEmpty()) {
                (preference as EditTextPreference).text =
                    getString(defaultValue)
                false
            } else {
                true
            }
        }
    }

    private fun initResolutionArray(size : Int): Array<CharSequence> {
        return Array(size) {
            "0x0"
        }
    }

    fun <T : Preference> listenToPref(prefRes: Int, func: (Preference, Any) -> Boolean) {
        findPreference<T>(getString(prefRes))
            ?.setOnPreferenceChangeListener(func)
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
