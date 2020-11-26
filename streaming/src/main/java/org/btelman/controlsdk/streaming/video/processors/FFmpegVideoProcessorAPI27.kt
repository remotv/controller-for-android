package org.btelman.controlsdk.streaming.video.processors

import androidx.annotation.RequiresApi
import com.arthenica.mobileffmpeg.ExecuteCallback
import com.arthenica.mobileffmpeg.FFmpeg
import org.btelman.controlsdk.enums.ComponentStatus
import org.btelman.controlsdk.streaming.models.ImageDataPacket
import org.btelman.controlsdk.streaming.models.StreamInfo
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max
import kotlin.math.min

/**
 * Process frames via FFmpeg
 */
@RequiresApi(27)
open class FFmpegVideoProcessorAPI27 : BaseVideoProcessor(){
    private val streaming = AtomicBoolean(false)
    private val ffmpegRunning = AtomicBoolean(false)
    private var successCounter = 0
    var process : Process? = null

    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()

    // We want at least 2 threads and at most 4 threads in the core pool,
    // preferring to have 1 less than the CPU count to avoid saturating
    // the CPU with background work
    private val CORE_POOL_SIZE = max(2, min(CPU_COUNT - 1, 4))
    private val MAXIMUM_POOL_SIZE = (CPU_COUNT * 2 + 1)
    private val KEEP_ALIVE_SECONDS : Long = 30

    val sThreadFactory: ThreadFactory =
        object : ThreadFactory {
            private val mCount =
                AtomicInteger(1)

            override fun newThread(r: Runnable): Thread {
                return Thread(r, "AsyncTask #" + mCount.getAndIncrement()).apply {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE)
                }
            }
        }

    val sPoolWorkQueue: BlockingQueue<Runnable> =
        LinkedBlockingQueue(128)

    val THREAD_POOL_EXECUTOR: Executor? = ThreadPoolExecutor(
        CORE_POOL_SIZE,
        MAXIMUM_POOL_SIZE,
        KEEP_ALIVE_SECONDS,
        TimeUnit.SECONDS,
        sPoolWorkQueue,
        sThreadFactory
    ).apply {
        allowCoreThreadTimeOut(true)
    }

    override fun enableInternal() {
        super.enableInternal()
        streaming.set(true)
        bootFFmpeg()
    }

    override fun disableInternal() {
        super.disableInternal()
        streamInfo = null
        streaming.set(false)
        FFmpeg.cancel()
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
            ffmpegRunning.set(false)
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

    protected open fun bootFFmpeg() {
        successCounter = 0
        status = ComponentStatus.CONNECTING
        ffmpegRunning.set(true)
        val command = getCommand()
        log.d{
            command
        }

        FFmpeg.executeAsync(command,
            ExecuteCallback { p0, p1 ->
                ffmpegRunning.set(false)
            },
            THREAD_POOL_EXECUTOR
        )
    }

    protected open fun getCommand() : String{
        val props = streamInfo ?: throw IllegalStateException("no StreamInfo supplied!")
        val list = ArrayList<String>()
        list.add("-hwaccel mediacodec")
        list.apply {
            addAll(getVideoInputOptions(props))
            addAll(getVideoOutputOptions(props))

            buildFilterOptions(props).let{
                if(it.isNotBlank())
                    add(it)
            }
            add("tcp://192.168.1.6:5466")
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
        return arrayListOf(
            "-f android_camera",
            "-input_queue_size 10",
            "-video_size ${props.width}x${props.height}",
            "-i 0:0",
            "-camera_index ${props.deviceInfo.getCameraId()}",
            "-s ${props.width}x${props.height}",
            "-r 25"
        )
    }

    protected open fun getVideoOutputOptions(props : StreamInfo): ArrayList<String> {
        val bitrate = props.bitrate
        return arrayListOf(
            "-f webm",
            "-framerate ${props.framerate}",
            "-codec:v vp8",
            "-b ${bitrate}k -minrate ${bitrate}k -maxrate ${bitrate}k -bufsize ${bitrate/1.5}k",
            "-bf 0"
        )
    }

    protected open fun getFilterOptions(props : StreamInfo): String {
        var rotationOption = props.orientation.ordinal-1 //leave blank
        if(rotationOption < 0)
            rotationOption = 3
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