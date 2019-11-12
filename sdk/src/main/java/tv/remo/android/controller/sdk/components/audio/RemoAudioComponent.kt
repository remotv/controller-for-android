package tv.remo.android.controller.sdk.components.audio

import android.os.Bundle
import org.btelman.controlsdk.models.ComponentEventObject
import org.btelman.controlsdk.streaming.components.AudioComponent
import org.btelman.controlsdk.streaming.models.StreamInfo
import tv.remo.android.controller.sdk.components.RemoCommandHandler
import tv.remo.android.controller.sdk.components.RemoSocketComponent
import tv.remo.android.controller.sdk.models.api.Channel
import tv.remo.android.controller.sdk.utils.EndpointBuilder

/**
 * Remo Audio component.
 */
class RemoAudioComponent : AudioComponent() {
    private var sleepMode = false
    override fun enableInternal() {
        //Do nothing. We want to delay it until we get a response from the control socket
    }

    override fun handleExternalMessage(message: ComponentEventObject): Boolean {
        if(message.source is RemoSocketComponent || message.source is RemoCommandHandler){
            handleSocketCommand(message)
        }
        return super.handleExternalMessage(message)
    }

    override fun doWorkLoop() {
        if(!sleepMode)
            super.doWorkLoop()
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
        }
    }

    private fun setNewEndpoint(channel : Channel) {
        context?.let {
            val endpoint = EndpointBuilder.getAudioUrl(it, channel.id)
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