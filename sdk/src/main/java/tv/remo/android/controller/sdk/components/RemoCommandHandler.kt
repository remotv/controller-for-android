package tv.remo.android.controller.sdk.components

import android.content.Context
import android.os.Bundle
import org.btelman.controlsdk.enums.ComponentType
import org.btelman.controlsdk.models.Component
import org.btelman.controlsdk.models.ComponentEventObject
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.models.api.User
import tv.remo.android.controller.sdk.utils.ChatUtil
import kotlin.system.exitProcess

/**
 * Handle and potentially filter controls/commands.
 * Any commands not handled through Android will get sent the the robot if possible.
 *
 * ex. `.devmode on` will get processed in Android, and not sent to the bot,
 * but `.speed 100` is not processed, and will get sent to the bot as is
 *
 * Commands can come from a button or from the chat. Commands from chat are only able to be used
 * by the owner and moderators
 */
class RemoCommandHandler : Component(){
    private var devModeEnabled = false
    private var stationary = false
    private var serverOwner = ""
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
                serverOwner = settings.serverOwner.getPref()
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
        val command = packet.message
        when {
            command == ".estop" -> {
                sendChat("Shutting Down...")
                sendHardwareCommand("stop")
                exclusiveUser = ""
                handler.postDelayed({
                    exitProcess(0)
                }, 500)
            }
            command == ".stationary" -> handleStationary(packet)
            command == ".table on" -> handleStationary(packet, true)
            command == ".table off" -> handleStationary(packet, false)
            command == ".devmode on" -> handleDevMode(packet, true)
            command == ".devmode off" -> handleDevMode(packet, false)
            command.startsWith(".bitrate") -> handleVideoAudioCommand(command)
            command.startsWith(".stream") || command.startsWith("/audio") -> handleVideoAudioCommand(command)
            command.contains(".xcontrol") -> parseXControl(packet)
            else -> processCommand(packet)
        }
    }

    private fun handleVideoAudioCommand(command: String) {
        eventDispatcher?.handleMessage(
            ComponentEventObject(ComponentType.CUSTOM, EVENT_MAIN, command, this)
        )
    }

    private fun handleDevMode(packet: Packet, turnOn: Boolean) {
        if(packet.isOwner) {
            devModeEnabled = turnOn
            sendChat("Dev mode turned ${if(devModeEnabled) "on" else "off"}")
        }
    }

    private fun handleStationary(packet: Packet, turnOn : Boolean? = null) {
        if(!useInternalCommandBlocking) {
            eventDispatcher?.handleMessage(
                ComponentType.HARDWARE, EVENT_MAIN, packet.message, this
            )
        }
        stationary = turnOn ?: !stationary
        sendChat(
            if(stationary)
                "Robot is now stationary. Only turning allowed"
            else
                "Robot is no longer stationary"
        )
    }

    private fun processCommand(packet: Packet) {
        val command = packet.message
        if(devModeEnabled && packet.user?.username != serverOwner){ //only owner should be able to control
            return
        }
        else if(exclusiveUser != null && packet.user?.username != exclusiveUser){
            return
        }
        if(useInternalCommandBlocking){
            if(stationary && commandsToBlockWhenStationary.contains(command))
                return
        }
        sendHardwareCommand(command)
    }

    private fun sendHardwareCommand(command : String) {
        eventDispatcher?.handleMessage(ComponentType.HARDWARE, EVENT_MAIN, command, this)
    }

    private fun parseXControl(packet: Packet) {
        val split = packet.message.split(" ").iterator()
        split.next()
        if(split.hasNext())
            parseXControlAction(packet, split.next())
        else return
        if(split.hasNext())
            setXControlTimer(split.next())
    }

    private fun setXControlTimer(time: String) {
        //silently catch any errors since this parses raw text coming from the owner,
        //and could be wrong
        runCatching {
            sendChat("Exclusive control ending in ${time.toInt()} seconds")
            handler.postDelayed({
                exclusiveUser = null
                sendChat("Exclusive control is no longer active")
            }, time.toInt()*1000L)
        }
    }

    private fun parseXControlAction(packet: Packet, next: String) {
        when(next){
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
                exclusiveUser = next
                sendChat("Exclusive control given to $exclusiveUser")
            }
        }
    }

    private fun sendChat(message : String){
        ChatUtil.sendToSiteChat(eventDispatcher,message)
    }

    override fun getType(): ComponentType {
        return ComponentType.CUSTOM
    }

    data class Packet(
        val message : String,
        val user : User?,
        val isModerator : Boolean = false,
        val isOwner : Boolean = false
    )
}