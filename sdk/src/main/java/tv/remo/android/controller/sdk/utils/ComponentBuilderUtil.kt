package tv.remo.android.controller.sdk.utils

import android.content.Context
import android.os.Bundle
import org.btelman.controlsdk.hardware.components.HardwareComponent
import org.btelman.controlsdk.hardware.interfaces.HardwareDriver
import org.btelman.controlsdk.models.ComponentHolder
import org.btelman.controlsdk.streaming.enums.Orientation
import org.btelman.controlsdk.streaming.factories.AudioProcessorFactory
import org.btelman.controlsdk.streaming.factories.VideoProcessorFactory
import org.btelman.controlsdk.streaming.factories.VideoRetrieverFactory
import org.btelman.controlsdk.streaming.models.CameraDeviceInfo
import org.btelman.controlsdk.streaming.models.StreamInfo
import org.btelman.controlsdk.streaming.utils.CameraUtil
import org.btelman.controlsdk.streaming.video.retrievers.DummyRetriever
import org.btelman.controlsdk.tts.SystemDefaultTTSComponent
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.components.RemoCommandHandler
import tv.remo.android.controller.sdk.components.RemoSocketComponent
import tv.remo.android.controller.sdk.components.audio.RemoAudioComponent
import tv.remo.android.controller.sdk.components.audio.RemoAudioProcessor
import tv.remo.android.controller.sdk.components.hardware.HardwareWatchdogComponent
import tv.remo.android.controller.sdk.components.video.CameraCompatOverride
import tv.remo.android.controller.sdk.components.video.RemoVideoComponent
import tv.remo.android.controller.sdk.components.video.RemoVideoProcessor
import tv.remo.android.controller.sdk.components.video.RemoVideoProcessorLegacy

/**
 * Helper class for assembling our list of components that we will use when using the robot
 */
object ComponentBuilderUtil {
    fun createHardwareComponents(settings: RemoSettingsUtil): Collection<ComponentHolder<*>> {
        val hardwareList = ArrayList<ComponentHolder<*>>()
        if(settings.robotSettingsEnable.getPref()){
            val hardwareBundle = Bundle().apply {
                putSerializable(HardwareDriver.BUNDLE_ID, settings.robotCommunicationDriver.getPref())
                putSerializable(HardwareComponent.HARDWARE_TRANSLATOR_BUNDLE_ID, settings.robotProtocolTranslator.getPref())
            }
            val hardwareComponent = ComponentHolder(HardwareComponent::class.java, hardwareBundle)
            hardwareList.add(hardwareComponent)
            hardwareList.add(ComponentHolder(HardwareWatchdogComponent::class.java, null))
        }
        hardwareList.add(ComponentHolder(RemoCommandHandler::class.java, null))
        return hardwareList
    }

    fun createTTSComponents(settings: RemoSettingsUtil): Collection<ComponentHolder<*>> {
        val ttsList = ArrayList<ComponentHolder<*>>()
        if(settings.textToSpeechEnabled.getPref()){
            val tts = ComponentHolder(SystemDefaultTTSComponent::class.java, null)
            ttsList.add(tts)
        }
        return ttsList
    }

    fun createStreamingComponents(context: Context, settings: RemoSettingsUtil): Collection<ComponentHolder<*>> {
        val streamList = ArrayList<ComponentHolder<*>>()
        buildStreamingBundle(context, settings).apply {
            if(settings.cameraEnabled.getPref()){
                val videoComponent = ComponentHolder(RemoVideoComponent::class.java, this)
                streamList.add(videoComponent)
            }

            if(settings.microphoneEnabled.getPref()){
                val audioComponent = ComponentHolder(RemoAudioComponent::class.java, this)
                streamList.add(audioComponent)
            }
        }

        return streamList
    }

    fun createSocketComponent(settings: RemoSettingsUtil): ComponentHolder<*> {
        return ComponentHolder(
            RemoSocketComponent::class.java,
            RemoSocketComponent.createBundle(settings.apiKey.getPref(), settings.channelId.getPref()))
    }

    private fun buildStreamingBundle(context: Context, settings: RemoSettingsUtil): Bundle {
        return Bundle().apply {
            val resolution = settings.cameraResolution.getPref().split("x")
            val streamInfo = StreamInfo(
                settings.videoUrl,
                settings.audioUrl,
                deviceInfo = CameraDeviceInfo.fromCamera(settings.cameraDeviceId.getPref()),
                orientation = Orientation.valueOf(settings.cameraOrientation.getPref()),
                bitrate = settings.cameraBitrate.getPref().toIntOrNull() ?: 512,
                width = resolution[0].toInt(),
                height = resolution[1].toInt()
            )
            //use our customized remo classes
            if(CameraUtil.supportsNDKCamera(context, settings.cameraDeviceId.getPref())){
                VideoRetrieverFactory.putClassInBundle(DummyRetriever::class.java, this)
                VideoProcessorFactory.putClassInBundle(RemoVideoProcessor::class.java, this)
            }
            else{
                VideoRetrieverFactory.putClassInBundle(CameraCompatOverride::class.java, this)
                VideoProcessorFactory.putClassInBundle(RemoVideoProcessorLegacy::class.java, this)
            }
            AudioProcessorFactory.putClassInBundle(RemoAudioProcessor::class.java, this)
            streamInfo.addToExistingBundle(this)
        }
    }
}