package tv.remo.android.controller.sdk

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import org.btelman.controlsdk.hardware.drivers.FelhrUsbSerialDriver
import org.btelman.controlsdk.hardware.translators.ArduinoTranslator
import org.btelman.logutil.kotlin.LogLevel
import tv.remo.android.controller.sdk.models.BooleanPref
import tv.remo.android.controller.sdk.models.ClassPref
import tv.remo.android.controller.sdk.models.IntPref
import tv.remo.android.controller.sdk.models.StringPref
import tv.remo.android.controller.sdk.utils.EndpointBuilder

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
    val serverOwner = StringPref(context, sharedPreferences, R.string.serverOwnerKey, "")

    //url settings (Read Only)
    val videoUrl = EndpointBuilder.getVideoUrl(context, channelId.getPref())
    val audioUrl = EndpointBuilder.getAudioUrl(context, channelId.getPref())

    //hardware related settings
    val robotSettingsEnable = BooleanPref(context, sharedPreferences, R.string.robotSettingsEnableKey, false)
    val robotCommunicationDriver = ClassPref(context, sharedPreferences, R.string.robotConnectionTypeKey,
        FelhrUsbSerialDriver::class.java)
    val robotProtocolTranslator = ClassPref(context, sharedPreferences, R.string.robotProtocolTypeKey,
        ArduinoTranslator::class.java)
    val useInternalCommandBlocking = BooleanPref(context, sharedPreferences, R.string.internalCommandBlocking, false)
    val internalCommandsToBlock = StringPref(context, sharedPreferences, R.string.internalCommandsToBlock, "f,b")

    //Camera related settings
    val cameraEnabled = BooleanPref(context, sharedPreferences, R.string.cameraSettingsEnableKey, false)
    val cameraResolution = StringPref(context, sharedPreferences, R.string.cameraResolutionKey, "640x480")
    val cameraFocus = StringPref(context, sharedPreferences, R.string.cameraFocusKey, "auto")
    val cameraOrientation = StringPref(context, sharedPreferences, R.string.cameraOrientationKey, "DIR_90")
    val cameraDeviceId = IntPref(context, sharedPreferences, R.string.cameraDeviceIdKey, 0)
    val cameraBitrate = StringPref(context, sharedPreferences, R.string.cameraBitrateKey, "1024")
    val useCamera2 = BooleanPref(context, sharedPreferences, R.string.useCamera2, Build.VERSION.SDK_INT >= 21)

    val cameraFFmpegFilterOptions = StringPref(context, sharedPreferences,
        R.string.ffmpegFilterAddition, context.getString(R.string.ffmpegDefaultFilterOptions))
    val ffmpegInputOptions = StringPref(context, sharedPreferences,
        R.string.ffmpegInputOptionsPrefsKey, context.getString(R.string.ffmpegDefaultInputOptions))
    val ffmpegOutputOptions = StringPref(context, sharedPreferences,
        R.string.ffmpegOutputOptionsPrefsKey, context.getString(R.string.ffmpegDefaultOutputOptions))

    //microphone related settings
    val microphoneEnabled = BooleanPref(context, sharedPreferences, R.string.microphoneSettingsEnableKey, false)
    val micVolume = StringPref(context, sharedPreferences, R.string.micVolumeBoostKey, "1")
    val microphoneBitrate = StringPref(context, sharedPreferences, R.string.micAudioBitrateKey, "64")

    //tts related settings
    val textToSpeechEnabled = BooleanPref(context, sharedPreferences, R.string.audioSettingsEnableKey, false)
    val siteTextToSpeechEnabled = BooleanPref(context, sharedPreferences, R.string.audioSettingsTTSRemoEnabledKey, true)
    val ttsInternalEnabled = BooleanPref(context, sharedPreferences, R.string.audioTTSInternalEnabledKey, false)

    //display settings
    val chatDisplayEnabled = BooleanPref(context, sharedPreferences, R.string.displayChatEnabledKey, true)
    val keepScreenOn = BooleanPref(context, sharedPreferences, R.string.displayPersistKey, false)
    val hideScreenControls = BooleanPref(context, sharedPreferences, R.string.autoHideControlsEnabledKey, false)

    //misc settings
    val streamSleepMode = BooleanPref(context, sharedPreferences, R.string.streamAutoSleepEnabledKey, false)
    val streamSleepTimeOut = IntPref(context, sharedPreferences, R.string.streamAutoSleepTimeoutKey, 5*60) //5 minutes

    val logLevel = StringPref(context, sharedPreferences, R.string.logLevelPrefsKey, LogLevel.ERROR.toString())
    val showStartMessage = BooleanPref(context, sharedPreferences, R.string.showStartMessageKey, false)

    companion object{
        private var INSTANCE : RemoSettingsUtil? = null

        private fun create(context: Context) : RemoSettingsUtil{
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val settings = RemoSettingsUtil(context, sharedPreferences)
            INSTANCE = settings
            return settings
        }

        fun <T> with(context: Context, func : (RemoSettingsUtil)->T) : T{
            val settingsUtil = with(context)
            return func(settingsUtil)
        }

        fun with(context: Context) : RemoSettingsUtil{
            return INSTANCE ?: create(context)
        }
    }
}
