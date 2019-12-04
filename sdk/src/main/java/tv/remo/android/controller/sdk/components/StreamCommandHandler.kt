package tv.remo.android.controller.sdk.components

import android.content.Context
import android.os.Bundle
import org.btelman.controlsdk.models.ComponentEventObject
import org.btelman.controlsdk.streaming.models.StreamInfo
import tv.remo.android.controller.sdk.interfaces.CommandStreamHandler
import tv.remo.android.controller.sdk.models.api.Channel
import tv.remo.android.controller.sdk.utils.EndpointBuilder

/**
 * External command handler that can be shared between video and audio components.
 *
 * Probably the best way to do this without changing the structure of the Stream components,
 * and allows new commands to work for both seamlessly, but still may not look pretty
 */
class StreamCommandHandler(val context: Context?, val streamHandler : CommandStreamHandler){
    var sleepMode = false

    private val subscriptionList = streamHandler.onRegisterCustomCommands()

    fun handleExternalMessage(message: ComponentEventObject){
        if(message.source is RemoSocketComponent || message.source is RemoCommandHandler){
            handleSocketCommand(message)
        }
    }

    fun shouldDoWork() : Boolean = !sleepMode

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
        context?:return
        when {
            data == ".stream sleep" -> {
                sleepMode = true
                streamHandler.acquireRetriever().disable()
            }
            data == ".stream wakeup" -> {
                sleepMode = false
                streamHandler.acquireRetriever().enable(context, streamHandler.pullStreamInfo())
            }
            data == ".stream reset" -> {
                sleepMode = false
                reload()
            }
        }
        processSubscribedArrayForCommand(data)
    }

    /**
     * Iterate through the array and trigger the subscribers if conditions are met
     */
    private fun processSubscribedArrayForCommand(data: String) {
        subscriptionList?.forEach {
            if(it.hasToEqual && data == it.message){
                it.callFun(data)
            }
            else if(!it.hasToEqual && data.contains(it.message)){
                it.callFun(data)
            }
        }
    }

    private fun setNewEndpoint(channel : Channel) {
        context?.let {
            val audioEndPoint = EndpointBuilder.getAudioUrl(it, channel.id)
            val videoEndpoint = EndpointBuilder.getVideoUrl(it, channel.id)
            val streamInfo = rebuildStream(streamHandler.pullStreamInfo()) {
                //Yes, this duplicates and is not needed for all, but this makes it work no
                // matter what uses it
                putString("endpoint", videoEndpoint) //overwrite the endpoint with the new one
                putString("audioEndpoint", audioEndPoint) //overwrite the endpoint with the new one
            }
            streamHandler.pushStreamInfo(streamInfo)
        }
        reload()
    }

    private fun reload() {
        streamHandler.resetComponents()
    }

    companion object{
        fun rebuildStream(streamInfo: StreamInfo, action: Bundle.() -> Unit) : StreamInfo{
            val bundle = streamInfo.toBundle().apply {
                action()
            }
            return StreamInfo.fromBundle(bundle)!!
        }
    }
}
