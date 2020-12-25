package tv.remo.android.controller.sdk.utils

import org.btelman.android.shellutil.Executor
import org.btelman.controlsdk.streaming.models.StreamInfo
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.models.StringPref

object FFmpegCommandBuilder {
    fun getAndProcessPreference(options: StringPref, props: StreamInfo, settingsUtil: RemoSettingsUtil, legacy: Boolean) : ArrayList<String>{
        options.getPref().let { inputCommand ->
            return processCommand(inputCommand, props, settingsUtil, legacy)
            //was not right, so lets just process the default
                ?: processCommand(options.defaultValue, props, settingsUtil, legacy)
                //default is also broken. Should never happen outside of development
                ?: throw IllegalArgumentException("Invalid options!")
        }
    }

    private fun getHeaders(settingsUtil: RemoSettingsUtil, legacy: Boolean) : String{
        val apiKey = settingsUtil.apiKey.getPref()
        return if(legacy){
            "-headers Authorization:Bearer${Executor.CHARACTER_SPACE}${apiKey}"
        }
        else{
            "-headers \"Authorization:Bearer ${apiKey}\""
        }
    }

    /**
     * process command to replace with camera props
     */
    private fun processCommand(command: String?, props: StreamInfo, settingsUtil: RemoSettingsUtil, legacy: Boolean): ArrayList<String>? {
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
                .replace("\${headers}", getHeaders(settingsUtil, legacy))
            processedCommand = replaceBitrate(processedCommand, props, legacy)
            processedCommand = processedCommand.replace("\${bitrateflags}",
                "-b:v ${bitrate}k")
        }

        return ArrayList<String>().also { list ->
            list.add(processedCommand)
        }
    }

    private fun replaceBitrate(processedCommand: String, props: StreamInfo, legacy: Boolean): String {
        val bitrate = props.bitrate
        val replacement =  if(legacy){
            "-b:v ${bitrate}k -minrate ${bitrate}k -maxrate ${bitrate}k -bufsize ${bitrate/1.5}k"
        } else{
            "-b:v ${bitrate}k"
        }
        return processedCommand.replace("\${bitrateflags}", replacement)
    }
}