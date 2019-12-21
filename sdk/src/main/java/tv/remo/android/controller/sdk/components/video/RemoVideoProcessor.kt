package tv.remo.android.controller.sdk.components.video

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.btelman.controlsdk.streaming.models.StreamInfo
import org.btelman.controlsdk.streaming.video.processors.FFmpegVideoProcessor
import org.btelman.controlsdk.streaming.video.retrievers.api21.MediaRecorderCameraRetriever
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.models.StringPref
import java.io.FileInputStream
import java.io.IOException
import java.lang.Thread.sleep


/**
 * More customized video processor that ties into the remo app better
 */
class RemoVideoProcessor : FFmpegVideoProcessor() {

    var handler : Handler? = null
    private var streamProps: StreamInfo? = null
    val buffer = ByteArray(8192)
    var read = 0
    var running = false

    var reader : FileInputStream? = null

    override fun enable(context: Context, streamInfo: StreamInfo) {
        Looper.myLooper() ?: Looper.prepare()
        handler = Handler(Looper.myLooper())
        super.enable(context, streamInfo)
        this.streamProps = streamInfo
        publishRunnable.run()
    }

    override fun disable() {
        super.disable()
        handler?.removeCallbacks(publishRunnable)
    }

    fun runSchedule(){
        handler?.postDelayed(publishRunnable, 1)
    }

    val publishRunnable = Runnable {
        reader?:let {
            if(MediaRecorderCameraRetriever.fdPair.isNotEmpty())
                reader = FileInputStream(MediaRecorderCameraRetriever.fdPair[0]!!.fileDescriptor)
        }
        reader?.let {
            try {
                if (it.available() > 0) {
                    tryWrite(it)
                } else {
                    //sleep(10)
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
                reader = null
                restartFFmpeg()
            } catch (e: IOException) {
                e.printStackTrace()
                reader = null
                restartFFmpeg()
            }
        }
        runSchedule()
    }

    private fun tryWrite(it: FileInputStream) {
        read = it.read(buffer)
        restartFFmpeg()
        sendBytes()
    }

    private fun sendBytes() {
        var process = process
        var outputStream = process?.outputStream
        outputStream?.let {stream ->
            notNullYay()
            stream.write(buffer, 0, read)
        }
    }

    private fun notNullYay() {
        sleep(1)
    }

    private fun restartFFmpeg() {
        if(process == null && !running){
            running = true
            tryBootFFmpeg()
        }
    }

    override fun onFinish() {
        super.onFinish()
        running = false
    }

    override fun getFilterOptions(props: StreamInfo): String {
        var filter = ""
        context?.let {
            RemoSettingsUtil.with(it){ settings ->
                filter = settings.cameraFFmpegFilterOptions.getPref()
            }
        }
        filter = filter.replace("\${internal}", super.getFilterOptions(props))
        return filter
    }

    override fun getVideoInputOptions(props: StreamInfo): ArrayList<String> {
        return RemoSettingsUtil.with(context!!){
            return@with getAndProcessPreference(it.ffmpegInputOptions, props)
        }
    }

    override fun getVideoOutputOptions(props: StreamInfo): ArrayList<String> {
        return RemoSettingsUtil.with(context!!){
            return@with getAndProcessPreference(it.ffmpegOutputOptions, props)
        }
    }

    private fun getAndProcessPreference(options: StringPref, props: StreamInfo) : ArrayList<String>{
        options.getPref().let { inputCommand ->
            return processCommand(inputCommand, props)
                //was not right, so lets just process the default
                ?: processCommand(options.defaultValue, props)
                        //default is also broken. Should never happen outside of development
                        ?: throw IllegalArgumentException("Invalid options!")
        }
    }

    /**
     * process command to replace with camera props
     */
    private fun processCommand(command: String?, props: StreamInfo): ArrayList<String>? {
        if(command.isNullOrEmpty()) return null //nothing here, cannot use this command
        var processedCommand : String = command

        //replace any defined variables that were wrapped in ${}
        props.apply {
            processedCommand = processedCommand.replace("\$width", "$width")
                .replace("\${height}", "$height")
                .replace("\${resolution}", "${width}x$height")
                .replace("\${framerate}", "$framerate")
                .replace("\${bitrate}", "$bitrate")
                .replace("\${inputStream}", "-")
                .replace("\${endpoint}", endpoint)
                .replace("\${bitrateflags}",
                    "-b:v ${bitrate}k -minrate ${bitrate}k -maxrate ${bitrate}k -bufsize ${bitrate/1.5}k")
        }

        return ArrayList<String>().also { list ->
            list.add(processedCommand)
        }
    }
}