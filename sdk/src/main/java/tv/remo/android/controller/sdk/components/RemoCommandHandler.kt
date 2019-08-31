package tv.remo.android.controller.sdk.components

import android.content.Context
import android.os.Bundle
import org.btelman.controlsdk.enums.ComponentType
import org.btelman.controlsdk.models.Component
import org.btelman.controlsdk.models.ComponentEventObject
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.models.api.User

/**
 * Created by Brendon on 8/28/2019.
 */
class RemoCommandHandler : Component(){
    private var devModeEnabled = false
    private var stationary = false
    private var exclusiveUser : String? = null
    private var useInternalCommandBlocking = false
    private var commandsToBlockWhenStationary = ArrayList<String>()

    override fun onInitializeComponent(applicationContext: Context, bundle: Bundle?) {
        super.onInitializeComponent(applicationContext, bundle)
        context?.let {
            RemoSettingsUtil.with(it){ settings ->
                useInternalCommandBlocking = settings.useInternalCommandBlocking.getPref()
                commandsToBlockWhenStationary.addAll(
                    settings.internalCommandsToBlock.getPref().split(",")
                )
            }
        }
    }

    override fun disableInternal() {
        //don't need
    }

    override fun enableInternal() {
        //don't need
    }

    override fun handleExternalMessage(message: ComponentEventObject): Boolean {
        if(message.type == ComponentType.CUSTOM && message.data is Packet){
            handleCommand(message.data as Packet)
        }
        return super.handleExternalMessage(message)
    }

    private fun handleCommand(packet: Packet) {
        val command = packet.string
        when {
            command == "/stationary on" -> {
                if(!useInternalCommandBlocking) eventDispatcher?.handleMessage(ComponentType.HARDWARE, EVENT_MAIN, command, this)
                else stationary = true
                sendChat("Robot is now stationary. Only turning allowed")
            }
            command == "/stationary off" -> {
                if(!useInternalCommandBlocking) eventDispatcher?.handleMessage(ComponentType.HARDWARE, EVENT_MAIN, command, this)
                else stationary = false
                sendChat("Robot is no longer stationary")
            }
            command == "/devmode on" -> {
                if(packet.isOwner) {
                    devModeEnabled = true
                    sendChat("Dev mode turned on")
                }
            }
            command == "/devmode off" -> {
                if(packet.isOwner) {
                    devModeEnabled = false
                    sendChat("Dev mode turned off")
                }
            }
            command.contains("/xcontrol") -> {
                parseXControl(packet)
            }
            else -> {
                if(devModeEnabled && !packet.isOwner){ //only owner should be able to control
                    return
                }
                else if(exclusiveUser != null && packet.user?.username != exclusiveUser){
                    return
                }
                if(useInternalCommandBlocking){
                    if(stationary && commandsToBlockWhenStationary.contains(command))
                        return
                }
                eventDispatcher?.handleMessage(ComponentType.HARDWARE, EVENT_MAIN, command, this)
            }
        }
    }

    fun sendChat(message : String){
        eventDispatcher?.handleMessage(ComponentEventObject(ComponentType.CUSTOM, EVENT_MAIN,
            RemoSocketComponent.RemoSocketChatPacket(message), this))
    }

    private fun parseXControl(packet: Packet) {
        val split = packet.string.split(" ").iterator()
        split.next()
        if(split.hasNext())
            when(val portion = split.next()){
                "off" -> {
                    if(!packet.isModerator) return
                    exclusiveUser = null
                    sendChat("Exclusive control is no longer active")
                }
                "~" -> {
                    packet.user?.username?.let {
                        exclusiveUser = it
                        sendChat("Exclusive control given to $exclusiveUser")
                    }
                }
                else -> {
                    if(!packet.isModerator) return
                    exclusiveUser = portion
                    sendChat("Exclusive control given to $exclusiveUser")
                }
            }
        else return
        if(split.hasNext())
            runCatching {
                val next = split.next()
                sendChat("Exclusive control ending in ${next.toInt()} seconds")
                handler.postDelayed({
                    exclusiveUser = null
                    sendChat("Exclusive control is no longer active")
                }, next.toInt()*1000L)
            } //silently catch any errors since this parses raw text coming from the owner, and could be wrong
    }

    override fun getType(): ComponentType {
        return ComponentType.CUSTOM
    }

    data class Packet(val string : String, val user : User?, val isModerator : Boolean = false, val isOwner : Boolean = false)
}