package org.btelman.controlsdk.streaming.video.processors

import android.graphics.Rect
import org.btelman.controlsdk.enums.ComponentStatus
import org.btelman.controlsdk.streaming.models.ImageDataPacket
import org.btelman.controlsdk.streaming.models.StreamInfo
import org.btelman.controlsdk.streaming.utils.FFmpegUtil
import org.btelman.controlsdk.streaming.utils.OutputStreamUtil
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Process frames via FFmpeg
 */
open class FFmpegVideoProcessor : BaseVideoProcessor(){
    private val streaming = AtomicBoolean(false)
    private val ffmpegRunning = AtomicBoolean(false)
    private var successCounter = 0
    var process : Process? = null

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
        streamInfo = null
        streaming.set(false)
        FFmpegUtil.killFFmpeg(process)
    }

    override fun processData(packet: ImageDataPacket) {
        if(streaming.get() /*&& limiter.tryAcquire() TODO?*/) {
            if (!ffmpegRunning.getAndSet(true)) {
                tryBootFFmpeg(packet.r)
            }
            try {
                OutputStreamUtil.sendImageDataToProcess(process, packet)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val ffmpegListener = object : FFmpegUtil.FFmpegExecuteResponseHandler(){
        override fun onStart() {
            ffmpegRunning.set(true)
            log.d{
                "FFMPEG : onStart"
            }
        }

        override fun onProgress(message: String) {
            log.d{
                "FFMPEG : onProgress : $message"
            }
            successCounter++
            status = ComponentStatus.STABLE
        }

        override fun onError(message: String) {
            log.e{
                "FFMPEG : onError : $message"
            }
            status = ComponentStatus.ERROR
        }

        override fun onComplete(statusCode: Int?) {
            log.d{
                "FFMPEG : onComplete : $statusCode"
            }
            ffmpegRunning.set(false)
            process?.destroy()
            process = null
            status = ComponentStatus.DISABLED
        }

        override fun onProcess(process: Process) {
            log.v{
                "FFMPEG : onProcess"
            }
            this@FFmpegVideoProcessor.process = process
        }
    }



    /**
     * Boot ffmpeg using config. If given a Rect, use that for resolution instead.
     */
    protected open fun tryBootFFmpeg(r : Rect? = null){
        if(!streaming.get()){
            ffmpegRunning.set(false)
            status = ComponentStatus.DISABLED
            process?.outputStream?.close()
            return
        }
        try{
            bootFFmpeg(r)
        } catch (e: java.lang.Exception) {
            status = ComponentStatus.ERROR
            e.printStackTrace()
            // Handle if FFmpeg is already running
        }
    }

    protected open fun bootFFmpeg(r : Rect? = null) {
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

            buildFilterOptions(props).let{
                if(it.isNotBlank())
                    add(it)
            }
            add(props.endpoint)
        }

        return list.joinToString (" ")
    }

    private fun buildFilterOptions(props: StreamInfo): String {
        val options = getFilterOptions(props)
        return if(options.isNotBlank()){
            "-vf $options"
        } else ""
    }

    protected open fun getVideoInputOptions(props : StreamInfo): ArrayList<String> {
        return arrayListOf(
            "-f rawvideo",
            "-vcodec rawvideo",
            "-s ${props.width}x${props.height}",
            "-r 25",
            "-pix_fmt nv21",
            "-i -"
        )
    }

    protected open fun getVideoOutputOptions(props : StreamInfo): ArrayList<String> {
        val bitrate = props.bitrate
        return arrayListOf(
            "-f mpegts",
            "-framerate ${props.framerate}",
            "-codec:v mpeg1video",
            "-b ${bitrate}k -minrate ${bitrate}k -maxrate ${bitrate}k -bufsize ${bitrate/1.5}k",
            "-bf 0"
        )
    }

    protected open fun getFilterOptions(props : StreamInfo): String {
        val rotationOption = props.orientation.ordinal //leave blank
        val filterList = ArrayList<String>()
        for (i in 0..rotationOption){
            filterList.add("transpose=1")
        }
        if(filterList.isNotEmpty()){
            return filterList.joinToString(",")
        }
        return ""
    }

    /**
     * Appends all elements that are not `null` to the given [destination].
     */
    private fun <C : MutableCollection<in String>, String : Any> Iterable<String?>.filterNotEmpty(destination: C): C {
        for (element in this) if (element != "" && element != null) destination.add(element)
        return destination
    }

    companion object{
        val UUID = java.util.UUID.randomUUID().toString()
    }
}