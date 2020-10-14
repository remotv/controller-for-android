package tv.remo.android.controller.sdk.components

import android.media.MediaPlayer
import android.net.Uri
import org.btelman.controlsdk.enums.ComponentType
import org.btelman.controlsdk.models.Component
import org.btelman.controlsdk.models.ComponentEventObject
import tv.remo.android.controller.sdk.components.RemoCommandHandler
import tv.remo.android.controller.sdk.interfaces.RemoCommandSender

class SoundPlayerComponent : Component(), RemoCommandSender {
    var player : MediaPlayer? = null

    override fun disableInternal() {
        player?.release()
    }

    override fun enableInternal() {

    }

    override fun handleExternalMessage(message: ComponentEventObject): Boolean {
        if(message.type == ComponentType.CUSTOM && message.data is RemoCommandHandler.Packet){
            handleCommand(message.data as RemoCommandHandler.Packet)
        }
        return super.handleExternalMessage(message)
    }

    private fun handleCommand(packet: RemoCommandHandler.Packet) {
        val command = packet.message
        if(command.startsWith(".audioplay ")){
            val audio = command.substringAfter(".audioplay ")
            val exists = context?.let {
                it.assets.list("ignore")?.contains("$audio.mp3") ?: false
            } ?: false
            if(exists && player?.isPlaying != true){
                try {
                    player?.release()
                } catch (e: Exception) {
                    //ignore
                }
                player = MediaPlayer()
                val afd = context!!.assets.openFd("ignore/${audio}.mp3")
                player?.setDataSource(afd.fileDescriptor,afd.startOffset,afd.length);
                player?.prepare()
                player?.start()
            }
        }
        else if(command == ".stopAudio"){
            player?.stop()
            player?.release()
            player = null
        }
    }

    override fun getType(): ComponentType {
        return ComponentType.HARDWARE
    }
}