package tv.remo.android.controller.sdk.components.audio

import org.btelman.controlsdk.streaming.audio.retrievers.BasicMicrophoneAudioRetriever
import tv.remo.android.controller.sdk.RemoSettingsUtil

class RemoAudioRetriever : BasicMicrophoneAudioRetriever() {
    override fun enableInternal() {
        context?.let {
            recordingThread.bufferSizeMultiplier = RemoSettingsUtil.with(it).microphoneBufferMultiplier.getPref().toFloatOrNull() ?: 2f
        }

        super.enableInternal()
    }
}