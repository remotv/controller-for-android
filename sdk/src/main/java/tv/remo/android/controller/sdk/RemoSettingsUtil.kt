package tv.remo.android.controller.sdk

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import org.btelman.controlsdk.hardware.drivers.FelhrUsbSerialDriver
import org.btelman.controlsdk.hardware.translators.ArduinoTranslator
import tv.remo.android.controller.sdk.models.BooleanPref
import tv.remo.android.controller.sdk.models.ClassPref
import tv.remo.android.controller.sdk.models.IntPref
import tv.remo.android.controller.sdk.models.StringPref

/**
 * Each settings key in the settings, easily accessible throughout the app.
 *
 * StringProperty annotation tells the StoreUtil what stringId the field is attached to,
 * based off of the name of the field
 */
class RemoSettingsUtil(context : Context, sharedPreferences: SharedPreferences) {

    //Connection settings
    val apiKey = StringPref(context, sharedPreferences, R.string.connectionApiTokenKey, "")
    val channelId = StringPref(context, sharedPreferences, R.string.connectionChannelIdKey, "")

    //hardware related settings
    val robotSettingsEnable = BooleanPref(context, sharedPreferences, R.string.robotSettingsEnableKey, false)
    val robotCommunicationDriver = ClassPref(context, sharedPreferences, R.string.robotConnectionTypeKey,
        FelhrUsbSerialDriver::class.java)
    val robotProtocolTranslator = ClassPref(context, sharedPreferences, R.string.robotProtocolTypeKey,
        ArduinoTranslator::class.java)
    val useInternalCommandBlocking = BooleanPref(context, sharedPreferences, R.string.internalCommandBlocking, false)
    val internalCommandsToBlock = StringPref(context, sharedPreferences, R.string.internalCommandsToBlock, "f,b,l,r")

    //Camera related settings
    val cameraEnabled = BooleanPref(context, sharedPreferences, R.string.cameraSettingsEnableKey, false)
    val cameraResolution = StringPref(context, sharedPreferences, R.string.cameraResolutionKey, "640x480")
    val cameraOrientation = StringPref(context, sharedPreferences, R.string.cameraOrientationKey, "DIR_90")
    val cameraDeviceId = IntPref(context, sharedPreferences, R.string.cameraDeviceIdKey, 0)
    val cameraBitrate = StringPref(context, sharedPreferences, R.string.cameraBitrateKey, "1024")
    val useCamera2 = BooleanPref(context, sharedPreferences, R.string.useCamera2, Build.VERSION.SDK_INT >= 21)

    //microphone related settings
    val microphoneEnabled = BooleanPref(context, sharedPreferences, R.string.microphoneSettingsEnableKey, false)
    val micVolume = StringPref(context, sharedPreferences, R.string.micVolumeBoostKey, "1")
    val microphoneBitrate = StringPref(context, sharedPreferences, R.string.micAudioBitrateKey, "64")

    //tts related settings
    val textToSpeechEnabled = BooleanPref(context, sharedPreferences, R.string.audioSettingsEnableKey, false)
    val siteTextToSpeechEnabled = BooleanPref(context, sharedPreferences, R.string.audioSettingsTTSRemoEnabledKey, false)
    val ttsWhenUserBannedEnabled = BooleanPref(context, sharedPreferences, R.string.audioBanVoiceEnabledKey, false)
    val ttsInternalEnabled = BooleanPref(context, sharedPreferences, R.string.audioTTSInternalEnabledKey, false)

    //display settings
    val chatDisplayEnabled = BooleanPref(context, sharedPreferences, R.string.displayChatEnabledKey, false)
    val keepScreenOn = BooleanPref(context, sharedPreferences, R.string.displayPersistKey, false)
    val hideScreenControls = BooleanPref(context, sharedPreferences, R.string.autoHideControlsEnabledKey, false)

    companion object{
        private var INSTANCE : RemoSettingsUtil? = null

        private fun create(context: Context) : RemoSettingsUtil{
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val settings = RemoSettingsUtil(context, sharedPreferences)
            INSTANCE = settings
            return settings
        }

        fun <T> with(context: Context, func : (RemoSettingsUtil)->T) : T{
            val settingsUtil = INSTANCE ?: create(context)
            return func(settingsUtil)
        }
    }
}
