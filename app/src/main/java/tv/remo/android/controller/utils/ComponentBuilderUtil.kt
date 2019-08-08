package tv.remo.android.controller.utils

import android.os.Bundle
import org.btelman.controlsdk.hardware.components.HardwareComponent
import org.btelman.controlsdk.hardware.interfaces.HardwareDriver
import org.btelman.controlsdk.models.ComponentHolder
import org.btelman.controlsdk.streaming.components.AudioComponent
import org.btelman.controlsdk.streaming.components.VideoComponent
import org.btelman.controlsdk.streaming.enums.Orientation
import org.btelman.controlsdk.streaming.models.CameraDeviceInfo
import org.btelman.controlsdk.streaming.models.StreamInfo
import org.btelman.controlsdk.tts.SystemDefaultTTSComponent
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.components.HardwareWatchdogComponent
import tv.remo.android.controller.sdk.components.RemoSocketComponent

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

    fun createStreamingComponents(settings: RemoSettingsUtil): Collection<ComponentHolder<*>> {
        val streamList = ArrayList<ComponentHolder<*>>()
        val steamingBundle = Bundle().apply {
            val channel = settings.channelId.getPref()
            val streamInfo = StreamInfo(
                "http://dev.remo.tv:1567/transmit?name=$channel-video",
                "http://dev.remo.tv:1567/transmit?name=$channel-audio"
                ,deviceInfo = CameraDeviceInfo.fromCamera(settings.cameraDeviceId.getPref())
                ,orientation = Orientation.valueOf(settings.cameraOrientation.getPref())
            )
            streamInfo.addToExistingBundle(this)
        }

        if(settings.cameraEnabled.getPref()){
            val videoComponent = ComponentHolder(VideoComponent::class.java, steamingBundle)
            streamList.add(videoComponent)
        }

        if(settings.microphoneEnabled.getPref()){
            val audioComponent = ComponentHolder(AudioComponent::class.java, steamingBundle)
            streamList.add(audioComponent)
        }
        return streamList
    }

    fun createSocketComponent(settings: RemoSettingsUtil): ComponentHolder<*> {
        return ComponentHolder(
            RemoSocketComponent::class.java,
            RemoSocketComponent.createBundle(settings.apiKey.getPref(), settings.channelId.getPref()))
    }
}