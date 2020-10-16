package org.btelman.controlsdk.streaming.audio.processors

import android.content.Context
import android.os.Bundle
import org.btelman.controlsdk.enums.ComponentStatus
import org.btelman.controlsdk.streaming.models.AudioPacket
import org.btelman.controlsdk.streaming.models.StreamInfo
import org.btelman.controlsdk.streaming.utils.FFmpegUtil
import org.btelman.controlsdk.streaming.utils.OutputStreamUtil
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Process audio from a source and send it to a specified endpoint with FFmpeg
 */
open class FFmpegAudioProcessor : BaseAudioProcessor() {
    private var lastTimecode: Long = 0L
    private var endpoint: String? = null
    private val streaming = AtomicBoolean(false)
    private var ffmpegRunning = AtomicBoolean(false)

    private var process: Process? = null

    private var successCounter: Int = 0

    override fun onInitializeComponent(applicationContext: Context, bundle: Bundle?) {
        super.onInitializeComponent(applicationContext, bundle)
        endpoint = streamInfo?.audioEndpoint ?: return//?: else we can't do anything
    }

    override fun enableInternal() {
        super.enableInternal()
        FFmpegUtil.initFFmpeg(context!!){ success ->
            streaming.set(success)
            if(!success){
                throw ExceptionInInitializerError("Unable to stream : FFMpeg Not Supported on this device")
            }
        }
    }

    override fun disableInternal() {
        super.disableInternal()
        process?.destroy()
        process = null
        streaming.set(false)
        FFmpegUtil.killFFmpeg(process)
    }

    override fun processAudioByteArray(data: AudioPacket) {
        if(!streaming.get()) return
        try {
            if (!ffmpegRunning.getAndSet(true)) {
                tryBootFFmpeg()
            }
            process?.let { _process ->
                if(data.timecode != lastTimecode){ //make sure we only send each packet once
                    lastTimecode = data.timecode
                    OutputStreamUtil.handleSendByteArray(_process.outputStream, data.b)
                }
            }
        } catch (e: Exception) {
            status = ComponentStatus.ERROR
            e.printStackTrace()
        }
    }

    val ffmpegListener = object : FFmpegUtil.FFmpegExecuteResponseHandler(){
        override fun onStart() {
            ffmpegRunning.set(true)
            log.d("FFMPEG : onStart")
        }

        override fun onProgress(message: String) {
            @Suppress("ConstantConditionIf")
            log.d{
                "FFMPEG : onProgress : $message"
            }
            successCounter++
            status = ComponentStatus.STABLE
        }

        override fun onError(message: String) {
            status = ComponentStatus.ERROR //TODO
            log.e{
                "FFMPEG : onError : $message"
            }
        }

        override fun onComplete(statusCode: Int?) {
            @Suppress("ConstantConditionIf")
            log.d {
                "FFMPEG : onComplete : $statusCode"
            }
            status = ComponentStatus.DISABLED
            ffmpegRunning.set(false)
        }

        override fun onProcess(process: Process) {
            @Suppress("ConstantConditionIf")
            log.v{
                "FFMPEG : onProcess"
            }
            this@FFmpegAudioProcessor.process = process
        }
    }

    protected open fun tryBootFFmpeg() {
        if(!streaming.get()){
            ffmpegRunning.set(false)
            status = ComponentStatus.DISABLED
            process?.outputStream?.close()
        }
        try {
            bootFFmpeg()
        } catch (e: Exception) {
            status = ComponentStatus.ERROR
            e.printStackTrace()
        }
    }

    protected open fun bootFFmpeg() {
        successCounter = 0
        status = ComponentStatus.CONNECTING
        FFmpegUtil.execute(context!!, UUID, getCommand(), ffmpegListener)
    }

    protected open fun getCommand() : String{
        val props = streamInfo ?: throw IllegalStateException("no StreamInfo supplied!")
        val list = ArrayList<String>()
        list.apply {
            addAll(getVideoInputOptions(props))
            addAll(getVideoOutputOptions(props))
            add(props.audioEndpoint!!)
        }
        return list.joinToString (" ")
    }

    protected open fun getVideoInputOptions(props: StreamInfo): ArrayList<String> {
        return arrayListOf(
            "-f s16be",
            "-i -"
        )
    }

    protected open fun getVideoOutputOptions(props: StreamInfo): Collection<String> {
        val bitrate = 32 //TODO make adjustable
        val volumeBoost = 1 //TODO make adjustable
        return arrayListOf(
            "-f mpegts",
            "-codec:a mp2",
            "-b:a ${bitrate}k",
            "-ar 44100",
            "-muxdelay 0.001",
            "-filter:a volume=$volumeBoost"
        )
    }

    companion object {
        val UUID = java.util.UUID.randomUUID().toString()
    }
}