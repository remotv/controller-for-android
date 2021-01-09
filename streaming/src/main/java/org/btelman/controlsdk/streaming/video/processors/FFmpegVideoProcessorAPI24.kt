package org.btelman.controlsdk.streaming.video.processors

import android.os.AsyncTask
import androidx.annotation.RequiresApi
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.ExecuteCallback
import com.arthenica.mobileffmpeg.FFmpeg
import org.btelman.controlsdk.enums.ComponentStatus
import org.btelman.controlsdk.streaming.enums.Orientation
import org.btelman.controlsdk.streaming.models.ImageDataPacket
import org.btelman.controlsdk.streaming.models.StreamInfo
import org.btelman.controlsdk.streaming.utils.FFmpegUtil
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Process frames via FFmpeg
 */
@RequiresApi(24)
open class FFmpegVideoProcessorAPI24 : BaseVideoProcessor(){
    private val streaming = AtomicBoolean(false)
    private val ffmpegRunning = AtomicBoolean(false)
    private var successCounter = 0
    var process : Process? = null

    override fun enableInternal() {
        super.enableInternal()
        streaming.set(true)
        status = ComponentStatus.CONNECTING
        bootFFmpeg()
    }

    override fun disableInternal() {
        super.disableInternal()
        streamInfo = null
        streaming.set(false)
        status = ComponentStatus.DISABLED
        FFmpeg.cancel()
        //let's just hope this is always long enough for FFmpeg to die since we can't always monitor it's death...
        Thread.sleep(1000)
    }

    override fun processData(packet: ImageDataPacket) {
        if(streaming.get() /*&& limiter.tryAcquire() TODO?*/) {
            if (!ffmpegRunning.getAndSet(true)) {
                tryBootFFmpeg()
            }
        }
    }

    /**
     * Boot ffmpeg using config. If given a Rect, use that for resolution instead.
     */
    protected open fun tryBootFFmpeg(){
        if(!streaming.get()){
            status = ComponentStatus.DISABLED
            process?.outputStream?.close()
            return
        }
        try{
            bootFFmpeg()
        } catch (e: java.lang.Exception) {
            status = ComponentStatus.ERROR
            e.printStackTrace()
            // Handle if FFmpeg is already running
        }
    }

    var lastRenderedTime = 0

    protected open fun bootFFmpeg() {
        lastRenderedTime = 0
        successCounter = 0
        status = ComponentStatus.CONNECTING
        ffmpegRunning.set(true)
        enableListener()
        val command = getCommand()
        log.d{
            command
        }

        FFmpeg.executeAsync(command,
            ExecuteCallback { p0, p1 ->
                disableListener()
                ffmpegRunning.set(false)
                status = ComponentStatus.DISABLED
            },
            AsyncTask.THREAD_POOL_EXECUTOR
        )
    }

    private fun enableListener() {
        Config.enableStatisticsCallback { newStatistics ->
            status = when {
                newStatistics.videoFps < 10 -> ComponentStatus.INTERMITTENT
                newStatistics.time > lastRenderedTime -> ComponentStatus.STABLE
                else -> {
                    ComponentStatus.INTERMITTENT
                }
            }
            lastRenderedTime = newStatistics.time
        }
    }

    private fun disableListener(){
        Config.enableStatisticsCallback(null)
    }

    protected open fun getCommand() : String{
        val props = streamInfo ?: throw IllegalStateException("no StreamInfo supplied!")
        props.endpoint?:throw IllegalStateException("no endpoint supplied!")
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

    private fun getVideoInputOptions(props : StreamInfo): ArrayList<String> {
        val videoSize = when(props.orientation){
            //Portrait orientations, flip resolutions
            Orientation.DIR_0 -> "${props.height}x${props.width}"
            Orientation.DIR_180 -> "${props.height}x${props.width}"

            //Landscape orientations, standard resolutions
            Orientation.DIR_90 -> "${props.width}x${props.height}"
            Orientation.DIR_270 -> "${props.width}x${props.height}"
        }
        return arrayListOf(
            "-f android_camera",
            "-input_queue_size 10",
            "-video_size $videoSize",
            "-i 0:0",
            "-camera_index ${props.deviceInfo.getCameraId()}",
            "-s $videoSize",
            "-r 25"
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
        return FFmpegUtil.getFilterOptions(props, offset = -1)
    }
}