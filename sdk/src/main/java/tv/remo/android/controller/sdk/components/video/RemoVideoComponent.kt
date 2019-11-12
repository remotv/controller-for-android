package tv.remo.android.controller.sdk.components.video

import android.os.Bundle
import org.btelman.controlsdk.models.ComponentEventObject
import org.btelman.controlsdk.streaming.components.VideoComponent
import org.btelman.controlsdk.streaming.models.StreamInfo
import tv.remo.android.controller.sdk.components.RemoCommandHandler
import tv.remo.android.controller.sdk.components.RemoSocketComponent
import tv.remo.android.controller.sdk.models.api.Channel
import tv.remo.android.controller.sdk.utils.EndpointBuilder

/**
 * Created by Brendon on 10/27/2019.
 */
class RemoVideoComponent : VideoComponent() {
    private var sleepMode = false

    override fun enableInternal() {
        //Do nothing. We want to delay it until we get a response from the control socket
    }

    override fun doWorkLoop() {
        if(!sleepMode)
            super.doWorkLoop()
    }

    override fun handleExternalMessage(message: ComponentEventObject): Boolean {
        if(message.source is RemoSocketComponent || message.source is RemoCommandHandler){
            handleSocketCommand(message)
        }
        return super.handleExternalMessage(message)
    }

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
                retriever.disable()
            }
            data == "/stream wakeup" -> {
                sleepMode = false
                retriever.enable(context!!, streamInfo)
            }
            data == "/stream reset" -> {
                sleepMode = false
                reload()
            }
            data.startsWith("/bitrate") -> {
                data.replace("/bitrate ", "").toIntOrNull()?.let{
                    setNewBitrate(it)
                }
            }
        }
    }

    private fun setNewBitrate(bitrate: Int) {
        rebuildStream {
            //TODO save this value to prefs
            putInt("bitrate", bitrate) //overwrite the endpoint with the new one
        }
        reload()
    }

    private fun setNewEndpoint(channel : Channel) {
        context?.let {
            val endpoint = EndpointBuilder.getVideoUrl(it, channel.id)
            rebuildStream {
                putString("endpoint", endpoint) //overwrite the endpoint with the new one
            }
        }
        reload()
    }

    private fun reload() {
        disableInternal()
        super.enableInternal()
    }

    private fun rebuildStream(action: Bundle.() -> Unit){
        val bundle = streamInfo.toBundle().apply {
            action()
        }
        streamInfo = StreamInfo.fromBundle(bundle)!!
    }
}