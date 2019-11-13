package tv.remo.android.controller.sdk.components

import android.content.Context
import android.os.Bundle
import org.btelman.controlsdk.models.ComponentEventObject
import org.btelman.controlsdk.streaming.models.StreamInfo
import tv.remo.android.controller.sdk.models.api.Channel
import tv.remo.android.controller.sdk.utils.EndpointBuilder

data class CommandSubscriptionData(val hasToEqual : Boolean = true, val message : String, val lambda : (String)->Boolean)

interface CommandStreamHandler{
    fun resetComponents()

    fun disableRetriever()
    fun disableProcessor()

    fun getStreamInfo() : StreamInfo
    fun setStreamInfo(streamInfo: StreamInfo)

    fun enableRetriever()
    fun enableProcessor()

    fun onRegisterCustomCommands() : ArrayList<CommandSubscriptionData>
}

class StreamComponentOverrides(val context: Context?, val streamHandler : CommandStreamHandler){
    var sleepMode = false

    fun handleExternalMessage(message: ComponentEventObject){
        if(message.source is RemoSocketComponent || message.source is RemoCommandHandler){
            handleSocketCommand(message)
        }
    }

    fun shouldDoWork() : Boolean = sleepMode

    private fun handleSocketCommand(message: ComponentEventObject) {
        when(message.data){
            is Channel -> {
                setNewEndpoint(message.data as Channel)
            }
            is String -> {
                handleStringCommand(message.data as String)
            }
        }
    }

    private fun handleStringCommand(data: String) {
        when {
            data == "/stream sleep" -> {
                sleepMode = true
                streamHandler.disableRetriever()
            }
            data == "/stream wakeup" -> {
                sleepMode = false
                streamHandler.enableProcessor()
            }
            data == "/stream reset" -> {
                sleepMode = false
                reload()
            }
        }
    }

    private fun setNewEndpoint(channel : Channel) {
        context?.let {
            val endpoint = EndpointBuilder.getAudioUrl(it, channel.id)
            val streamInfo = rebuildStream(streamHandler.getStreamInfo()) {
                putString("endpoint", endpoint) //overwrite the endpoint with the new one
            }
            streamHandler.setStreamInfo(streamInfo)
        }
        reload()
    }

    private fun reload() {
        streamHandler.resetComponents()
    }

    private fun rebuildStream(streamInfo: StreamInfo, action: Bundle.() -> Unit) : StreamInfo{
        val bundle = streamInfo.toBundle().apply {
            action()
        }
        return StreamInfo.fromBundle(bundle)!!
    }
}
