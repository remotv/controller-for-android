package tv.remo.android.controller.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.btelman.controlsdk.hardware.drivers.BluetoothClassicDriver
import org.btelman.controlsdk.hardware.drivers.FelhrUsbSerialDriver
import org.btelman.controlsdk.hardware.interfaces.HardwareDriver
import org.btelman.controlsdk.hardware.interfaces.Translator
import org.btelman.controlsdk.hardware.translators.ArduinoSendSingleCharTranslator
import org.btelman.controlsdk.hardware.translators.ArduinoTranslator
import org.btelman.controlsdk.hardware.translators.NXTJoystickDriverTranslator
import org.btelman.controlsdk.hardware.translators.SingleByteTranslator
import org.btelman.controlsdk.streaming.enums.Orientation
import tv.remo.android.controller.R
import tv.remo.android.controller.RemoApplication
import tv.remo.android.controller.databinding.ActivityExternalControlBinding
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.components.RemoBroadcaster
import tv.remo.android.controller.sdk.models.api.Channel
import tv.remo.android.controller.sdk.utils.RemoAPI

/**
 * Activity that external applications can use to start a stream
 *
 * Right now, this directly modifies the preferences for the whole application. At some point it
 * might be nice to have the preferences sent by the external application apply only to the stream
 * that it starts.
 */

class ExternalControlActivity: AppCompatActivity() {
    private val log = RemoApplication.getLogger(this)
    private lateinit var binding: ActivityExternalControlBinding

    private var apiKeyIsInvalid = false;
    private var externalAppParameters: ExternalAppParameters? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExternalControlBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        binding.denyButton.setOnClickListener() {
            exitWithErrorResult(RESULT_USER_DENIED, "The request was denied by the user")
        }
        externalAppParameters = processExternalAppParameters()

        binding.channelNameText.text = getString(R.string.channelNameString, "loading...")

        val action = intent?.action
        if (action != "tv.remo.android.controller.action.REQUEST_REMO_STREAM_START") {
            binding.promptText.text = getString(R.string.startedWithUnexpectedAction, action)
            binding.cameraUsageText.text = Html.fromHtml(getString(R.string.cameraUsageString, "will"))
            binding.micUsageText.text = Html.fromHtml(getString(R.string.micUsageString, "will not"))
            return
        }

        val appName = try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(callingPackage?: "", 0)) as String
        } catch (e: PackageManager.NameNotFoundException) {
            callingPackage ?: "unknown"
        }
        binding.promptText.text = getString(R.string.externalControlPrompt, appName)

        binding.cameraUsageText.text = Html.fromHtml(getString(R.string.cameraUsageString, if (externalAppParameters?.enableCamera == true) "will" else "will not"))
        binding.micUsageText.text = Html.fromHtml(getString(R.string.micUsageString, if (externalAppParameters?.enableMicrophone == true) "will" else "will not"))

        RemoAPI(applicationContext).authRobot(externalAppParameters?.apiKey?: "") { channel: Channel?, _: Exception? ->
            val channelName: String
            var invalidKey = false

            if (channel?.name == null) {
                log.w("The provided API key does not appear to be valid")
                invalidKey = true;
                channelName = "none (invalid API key)"
            } else {
                channelName = channel.name
            }

            runOnUiThread() {
                apiKeyIsInvalid = invalidKey
                binding.channelNameText.text = getString(R.string.channelNameString, channelName)
            }
        }
        binding.startStreamButton.setOnClickListener(this::onStartStreamButtonClicked)

        // There are several reasons that we don't need the security of making the user approve
        // starting the stream on the REV Robotics Control Hub, and since that device is headless,
        // we can just immediately pretend that the start stream button was clicked.
        if (Build.MANUFACTURER == "REV Robotics" && Build.MODEL.contains("Control Hub")) {
            onStartStreamButtonClicked(binding.startStreamButton)
        }
    }

    private fun processExternalAppParameters(): ExternalAppParameters {
        return ExternalAppParameters(
            apiKey = intent.getStringExtra("ApiKey")?: "fakeKey",
            enableRobot = intent.getBooleanExtra("EnableRobot", true),
            robotCommunicationDriver = when(intent.getStringExtra("RobotCommunicationDriver")) {
                "BluetoothClassic" -> BluetoothClassicDriver::class.java
                "FelhrUsb" -> FelhrUsbSerialDriver::class.java
                "RemoBroadcaster" -> RemoBroadcaster::class.java
                else -> RemoBroadcaster::class.java
            },
            robotProtocolTranslator = when (intent.getStringExtra("RobotProtocolTranslator")) {
                "Arduino" -> ArduinoTranslator::class.java
                "ArduinoSendSingleChar" -> ArduinoSendSingleCharTranslator::class.java
                "NXTJoystick" -> NXTJoystickDriverTranslator::class.java
                "SingleByte" -> SingleByteTranslator::class.java
                else -> ArduinoTranslator::class.java
            },
            enableCamera = intent.getBooleanExtra("EnableCamera", false),
            cameraWidth = intent.getIntExtra("CameraWidth", 640),
            cameraHeight = intent.getIntExtra("CameraWidth", 480),
            cameraOrientation = when(intent.getStringExtra("CameraOrientation")) {
                "DIR_0" -> Orientation.DIR_0
                "DIR_90" -> Orientation.DIR_90
                "DIR_180" -> Orientation.DIR_180
                "DIR_270" -> Orientation.DIR_270
                else -> Orientation.DIR_90
            },
            cameraDeviceId = intent.getIntExtra("CameraDeviceId", -1),
            cameraBitrateKbps = intent.getIntExtra("CameraBitrateKbps", 1024),
            enableMicrophone = intent.getBooleanExtra("EnableMicrophone", false),
            microphoneVolumeMultiplier = intent.getDoubleExtra("MicrophoneVolumeMultiplier", 1.0),
            microphoneBitrateKbps = intent.getIntExtra("MicrophoneBitrateKbps", 64))
    }

    private fun applyExternalAppParameters(externalAppParameters: ExternalAppParameters) {
        // TODO(Noah): Somehow one of these causes the camera settings activity to crash, but I don't see any discrepancies
        RemoSettingsUtil.with(applicationContext) {
            it.apiKey.savePref(externalAppParameters.apiKey)
            it.robotSettingsEnable.savePref(externalAppParameters.enableRobot)
            it.robotCommunicationDriver.savePref(externalAppParameters.robotCommunicationDriver)
            it.robotProtocolTranslator.savePref(externalAppParameters.robotProtocolTranslator)
            it.cameraEnabled.savePref(externalAppParameters.enableCamera)
            it.cameraResolution.savePref("${externalAppParameters.cameraWidth}x${externalAppParameters.cameraHeight}")
            it.cameraOrientation.savePref(externalAppParameters.cameraOrientation.name)
            it.cameraDeviceId.savePref(externalAppParameters.cameraDeviceId)
            it.cameraBitrate.savePref(externalAppParameters.cameraBitrateKbps.toString())
            it.microphoneEnabled.savePref(externalAppParameters.enableMicrophone)
            it.micVolume.savePref(externalAppParameters.microphoneVolumeMultiplier.toString())
            it.microphoneBitrate.savePref(externalAppParameters.microphoneBitrateKbps.toString())
        }
    }

    private fun onStartStreamButtonClicked(view: View) {
        if (apiKeyIsInvalid) {
            exitWithErrorResult(RESULT_INVALID_API_KEY,"The provided API key is invalid")
        } else {
            externalAppParameters?.let { applyExternalAppParameters(it) }
            val splashIntent = Intent(applicationContext, SplashScreen::class.java)
            splashIntent.putExtra(EXTRA_STARTED_FROM_EXTERNAL_APP, true)
            startActivityForResult(splashIntent, 0)
            // TODO(Noah): Instead of calling finish() here, call it in onActivityResult(), so that
            //  we can call setResult() with the result from SplashScreen/MainActivity
            finish()
        }
    }

    private fun exitWithErrorResult(code: Int, message: String?) {
        log.e("$message. Exiting.")
        setResult(code, Intent().putExtra("errorMessage", message))
        finish();
    }

    // Un-implemented options:
    //  - internal command blocking
    //  - ffmpeg options
    private data class ExternalAppParameters(
        val apiKey: String,
        val enableRobot: Boolean,
        val robotCommunicationDriver: Class<out HardwareDriver>,
        val robotProtocolTranslator: Class<out Translator>,
        val enableCamera: Boolean,
        val cameraWidth: Int,
        val cameraHeight: Int,
        val cameraOrientation: Orientation,
        val cameraBitrateKbps: Int,
        val cameraDeviceId: Int,
        val enableMicrophone: Boolean,
        val microphoneVolumeMultiplier: Double,
        val microphoneBitrateKbps: Int)
}

const val EXTRA_STARTED_FROM_EXTERNAL_APP = "startedFromExternalApp"

private const val RESULT_USER_DENIED = -1
private const val RESULT_INVALID_API_KEY = -2
