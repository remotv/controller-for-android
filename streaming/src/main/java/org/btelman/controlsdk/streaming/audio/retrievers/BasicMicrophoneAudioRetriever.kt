package org.btelman.controlsdk.streaming.audio.retrievers

import org.btelman.controlsdk.enums.ComponentStatus
import org.btelman.controlsdk.streaming.models.AudioPacket
import org.btelman.controlsdk.streaming.utils.AudioRecordingThread
import org.btelman.controlsdk.streaming.utils.AudioUtil

/**
 * Grab audio from the microphone and send it to the processor when asked to
 */
open class BasicMicrophoneAudioRetriever : BaseAudioRetriever(), AudioRecordingThread.AudioDataReceivedListener {

    private val recordingThread = AudioRecordingThread(this)

    private var dataArray : AudioPacket? = null

    override fun enableInternal() {
        super.enableInternal()
        recordingThread.startRecording()
    }

    override fun disableInternal() {
        super.disableInternal()
        recordingThread.stopRecording()
    }

    override fun onAudioDataReceived(data: ShortArray?) {
        if(status != ComponentStatus.STABLE)
            status = ComponentStatus.STABLE
        dataArray = data?.let { audioArr ->
            AudioPacket(AudioUtil.ShortToByte_ByteBuffer_Method(audioArr))
        } //?: null
    }

    override fun retrieveAudioByteArray(): AudioPacket? {
        return dataArray
    }
}