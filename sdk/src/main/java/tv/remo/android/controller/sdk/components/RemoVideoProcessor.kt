package tv.remo.android.controller.sdk.components

import android.content.Context
import org.btelman.controlsdk.streaming.models.StreamInfo
import org.btelman.controlsdk.streaming.video.processors.FFmpegVideoProcessor
import tv.remo.android.controller.sdk.RemoSettingsUtil

/**
 * More customized video processor that ties into the remo app better
 */
class RemoVideoProcessor : FFmpegVideoProcessor() {

    private var streamProps: StreamInfo? = null

    override fun enable(context: Context, streamInfo: StreamInfo) {
        super.enable(context, streamInfo)
        this.streamProps = streamInfo
    }

    override fun getFilterOptions(props: StreamInfo): String {
        var filter = ""
        context?.let {
            RemoSettingsUtil.with(it){ settings ->
                filter = settings.cameraFFmpegFilterOptions.getPref()
            }
        }
        return super.getFilterOptions(props)+filter //intentionally no space added.
    }

    override fun getVideoInputOptions(props: StreamInfo): ArrayList<String> {
        var command : ArrayList<String>? = null
        RemoSettingsUtil.with(context!!){
            it.ffmpegInputOptions.getPref().let {inputCommand ->
                command = processCommand(inputCommand, props)
                    ?: processCommand(it.ffmpegInputOptions.defaultValue, props)
                    ?: throw IllegalArgumentException("Invalid input options")
            }
        }
        return command!!
    }

    override fun getVideoOutputOptions(props: StreamInfo): ArrayList<String> {
        var command : ArrayList<String>? = null
        RemoSettingsUtil.with(context!!){
            it.ffmpegOutputOptions.getPref().let {outputCommand ->
                command = processCommand(outputCommand, props)
                    ?: processCommand(it.ffmpegOutputOptions.defaultValue, props)
                    ?: throw IllegalArgumentException("Invalid output options")
            }
        }
        return command!!
    }

    /**
     * process command to replace with camera props
     */
    private fun processCommand(command: String?, props: StreamInfo): ArrayList<String>? {
        if(command.isNullOrEmpty()) return null
        var processedCommand : String = command
        props.apply {
            processedCommand = processedCommand.replace("\$width", "$width")
                .replace("\${height}", "$height")
                .replace("\${resolution}", "${width}x$height")
                .replace("\${framerate}", "$framerate")
                .replace("\${bitrate}", "$bitrate")
                .replace("\${inputStream}", "-")
                .replace("\${endpoint}", endpoint)
                .replace("\${bitrateflags}",
                    "-b ${bitrate}k -minrate ${bitrate}k -maxrate ${bitrate}k -bufsize ${bitrate/1.5}k")
        }

        return ArrayList<String>().also { list ->
            list.add(processedCommand)
        }
    }
}