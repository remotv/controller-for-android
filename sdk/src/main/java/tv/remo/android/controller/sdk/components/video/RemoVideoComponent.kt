package tv.remo.android.controller.sdk.components.video

import android.content.Context
import android.os.Bundle
import android.util.Log
import org.btelman.controlsdk.enums.ComponentStatus
import org.btelman.controlsdk.interfaces.ComponentEventListener
import org.btelman.controlsdk.models.ComponentEventObject
import org.btelman.controlsdk.streaming.components.StreamSubComponent
import org.btelman.controlsdk.streaming.components.VideoComponent
import org.btelman.controlsdk.streaming.models.StreamInfo
import org.btelman.controlsdk.streaming.video.retrievers.AshmemRetriever
import org.btelman.controlsdk.streaming.video.retrievers.DummyRetriever
import tv.remo.android.controller.sdk.components.StreamCommandHandler
import tv.remo.android.controller.sdk.components.StreamCommandHandler.Companion.rebuildStream
import tv.remo.android.controller.sdk.interfaces.CommandStreamHandler
import tv.remo.android.controller.sdk.interfaces.RemoCommandSender
import tv.remo.android.controller.sdk.models.CommandSubscriptionData
import tv.remo.android.controller.sdk.utils.ChatUtil

/**
 * Created by Brendon on 10/27/2019.
 */
open class RemoVideoComponent : VideoComponent(), CommandStreamHandler {
    private var commandHandler : StreamCommandHandler? = null

    override fun setLoopMode() {
        if(retriever is DummyRetriever || retriever is AshmemRetriever){
            loopInterval = 100 //100 milliseconds... We are not updating camera here, so OK. Could be slower though
            shouldAutoUpdateLoop = true
        }
        else{
            super.setLoopMode() //go with the default
        }
    }

    override fun enableInternal() {
        //Do nothing. We want to delay it until we get a response from the control socket
        status = ComponentStatus.DISABLED
    }

    override fun disableInternal() {
        super.disableInternal()
        status = ComponentStatus.DISABLED
    }

    override fun onInitializeComponent(applicationContext: Context, bundle: Bundle?) {
        super.onInitializeComponent(applicationContext, bundle)
        commandHandler = StreamCommandHandler(applicationContext, this)
    }

    override fun setEventListener(listener: ComponentEventListener?) {
        super.setEventListener(listener)
        commandHandler?.setEventListener(listener)
    }

    override fun doWorkLoop() {
        if(commandHandler?.shouldDoWork() != false) //allows this to succeed if commandHandler is null
            super.doWorkLoop()
        status = processor.status //base status off of the processor
    }

    override fun handleExternalMessage(message: ComponentEventObject): Boolean {
        if(message.source is RemoCommandSender){
            commandHandler?.handleExternalMessage(message)
        }
        return super.handleExternalMessage(message)
    }

    private fun setNewBitrate(bitrate: Int) {
        streamInfo = rebuildStream(streamInfo) {
            //TODO save this value to prefs
            putInt("bitrate", bitrate) //overwrite the endpoint with the new one
            ChatUtil.sendToSiteChat(eventDispatcher,"Reloading streaming...")
        }
        resetComponents()
    }

    override fun resetComponents() {
        try { //maybe was never initialized?
            disableInternal()
        } catch (e: Exception) {
            Log.e("Video","Attempt to disable video components", e)
        }
        status = ComponentStatus.CONNECTING
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
            add(CommandSubscriptionData(false, ".bitrate ") { bitrateString ->
                setNewBitrate(bitrateString.toInt())
            })
            add(CommandSubscriptionData(true, ".stream sleep"){
                retriever.removeListenerForFrame()
            })
            add(CommandSubscriptionData(true, ".stream wakeup"){
                retriever.listenForFrame{
                    onFrameUpdate()
                }
            })
        }
    }
}