package tv.remo.android.controller.sdk.components.audio

import android.content.Context
import android.os.Bundle
import kotlinx.coroutines.runBlocking
import org.btelman.controlsdk.enums.ComponentType
import org.btelman.controlsdk.models.ComponentEventObject
import org.btelman.controlsdk.streaming.components.AudioComponent
import org.btelman.controlsdk.streaming.components.StreamSubComponent
import org.btelman.controlsdk.streaming.models.StreamInfo
import org.btelman.controlsdk.tts.TTSBaseComponent
import tv.remo.android.controller.sdk.R
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.components.StreamCommandHandler
import tv.remo.android.controller.sdk.interfaces.CommandStreamHandler
import tv.remo.android.controller.sdk.interfaces.RemoCommandSender
import tv.remo.android.controller.sdk.models.CommandSubscriptionData
import tv.remo.android.controller.sdk.utils.ChatUtil

/**
 * Remo Audio component.
 */
class RemoAudioComponent : AudioComponent() , CommandStreamHandler {
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
        if(message.source is RemoCommandSender){
            commandHandler?.handleExternalMessage(message)
        }
        return super.handleExternalMessage(message)
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
            add(CommandSubscriptionData(false, ".audio bitrate "){ bitrateString ->
                setNewBitrate(bitrateString.toInt())
            })
            add(CommandSubscriptionData(false, ".audio mute"){
                disableInternal()
                "audio muted".also { text ->
                    sendChatUp(text)
                    ChatUtil.sendToSiteChat(eventDispatcher,text)
                }
            })
            add(CommandSubscriptionData(false, ".audio unmute"){
                super.enableInternal()
                ChatUtil.sendToSiteChat(eventDispatcher,"audio unmuted")
                sendChatUp("The Microphone is back on")
            })
        }
    }

    private fun sendChatUp(text : String){
        val data = TTSBaseComponent.TTSObject(
            text,
            .5f,
            isSpeakable = true
        )
        eventDispatcher?.handleMessage(ComponentEventObject(ComponentType.TTS, EVENT_MAIN, data, this))
    }

    private fun setNewBitrate(value: Int) {
        if(context?.resources?.getStringArray(R.array.bitrate_audio_list_values)?.contains(value.toString()) != true){
            ChatUtil.sendToSiteChat(eventDispatcher, "audio bitrate not accepted! Accepted options: 32, 64, 128, 192")
        }
        else{
            RemoSettingsUtil.with(context!!){
                it.microphoneBitrate.apply {
                    sharedPreferences.edit().putString(key, value.toString()).apply()
                    ChatUtil.sendToSiteChat(eventDispatcher,"Setting saved. Reloading audio...")
                }

                //we only care about rebooting ffmpeg
                runBlocking {
                    processor.disable().await()
                    processor.updateStreamInfo(streamInfo)
                    processor.enable().await()
                }
            }
        }
    }
}