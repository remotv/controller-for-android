package tv.remo.android.controller.sdk.components.video

import android.content.Context
import android.os.Bundle
import org.btelman.controlsdk.models.ComponentEventObject
import org.btelman.controlsdk.streaming.components.StreamSubComponent
import org.btelman.controlsdk.streaming.components.VideoComponent
import org.btelman.controlsdk.streaming.models.StreamInfo
import tv.remo.android.controller.sdk.components.RemoCommandHandler
import tv.remo.android.controller.sdk.components.RemoSocketComponent
import tv.remo.android.controller.sdk.components.StreamCommandHandler
import tv.remo.android.controller.sdk.components.StreamCommandHandler.Companion.rebuildStream
import tv.remo.android.controller.sdk.interfaces.CommandStreamHandler
import tv.remo.android.controller.sdk.models.CommandSubscriptionData

/**
 * Created by Brendon on 10/27/2019.
 */
class RemoVideoComponent : VideoComponent(), CommandStreamHandler {
    private var commandHandler : StreamCommandHandler? = null

    override fun enableInternal() {
        //Do nothing. We want to delay it until we get a response from the control socket
    }

    override fun onInitializeComponent(applicationContext: Context, bundle: Bundle?) {
        super.onInitializeComponent(applicationContext, bundle)
        commandHandler = StreamCommandHandler(applicationContext, this)
    }

    override fun doWorkLoop() {
        if(commandHandler?.shouldDoWork() != false) //allows this to succeed if commandHandler is null
            super.doWorkLoop()
    }

    override fun handleExternalMessage(message: ComponentEventObject): Boolean {
        if(message.source is RemoSocketComponent || message.source is RemoCommandHandler){
            commandHandler?.handleExternalMessage(message)
        }
        return super.handleExternalMessage(message)
    }

    private fun setNewBitrate(bitrate: Int) {
        streamInfo = rebuildStream(streamInfo) {
            //TODO save this value to prefs
            putInt("bitrate", bitrate) //overwrite the endpoint with the new one
        }
        resetComponents()
    }

    override fun resetComponents() {
        disableInternal()
        super.enableInternal()
    }

    override fun acquireRetriever(): StreamSubComponent {
        return retriever
    }

    override fun acquireProcessor(): StreamSubComponent {
        return processor
    }

    override fun pullStreamInfo(): StreamInfo {
        return streamInfo
    }

    override fun pushStreamInfo(streamInfo: StreamInfo) {
        this.streamInfo = streamInfo
    }

    override fun onRegisterCustomCommands(): ArrayList<CommandSubscriptionData>? {
        return ArrayList<CommandSubscriptionData>().apply {
            add(CommandSubscriptionData(false, "/bitrate"){ bitrateUnprocessed ->
                bitrateUnprocessed.replace("/bitrate ", "").toIntOrNull()?.let{
                    setNewBitrate(it)
                }
            })
        }
    }
}