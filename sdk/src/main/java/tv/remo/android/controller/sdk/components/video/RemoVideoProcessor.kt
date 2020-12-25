package tv.remo.android.controller.sdk.components.video

import androidx.annotation.RequiresApi
import org.btelman.controlsdk.streaming.models.StreamInfo
import org.btelman.controlsdk.streaming.video.processors.FFmpegVideoProcessorAPI27
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.models.StringPref

/**
 * More customized video processor that ties into the remo app better
 */
@RequiresApi(27)
class RemoVideoProcessor : FFmpegVideoProcessorAPI27() {
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

//    override fun getVideoInputOptions(props: StreamInfo): ArrayList<String> {
//        return RemoSettingsUtil.with(context!!){
//            return@with getAndProcessPreference(it.ffmpegInputOptions, props)
//        }
//    }

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

    fun getHeaders() : String{
        val apiKey = RemoSettingsUtil.with(context!!).apiKey.getPref()
        return "-headers \"Authorization:Bearer ${apiKey}\""
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
                .replace("\${headers}", getHeaders())
                .replace("\${bitrateflags}",
                    "-b:v ${bitrate}k")
        }

        return ArrayList<String>().also { list ->
            list.add(processedCommand)
        }
    }
}