package tv.remo.android.controller.sdk.integration.discord

import android.content.Context
import android.os.Bundle
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.btelman.controlsdk.enums.ComponentType
import org.btelman.controlsdk.models.Component
import org.btelman.controlsdk.models.ComponentEventObject
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.components.RemoCommandHandler
import tv.remo.android.controller.sdk.components.RemoSocketComponent
import tv.remo.android.controller.sdk.models.api.Channel
import java.util.*


/**
 * Created by Brendon on 11/29/2019.
 */
class DiscordComponent : Component() {

    private var jda: JDA? = null
    //private var discordClient: DiscordClient? = null
    private lateinit var discordData : DiscordData
    private var channel : Channel? = null

    override fun onInitializeComponent(applicationContext: Context, bundle: Bundle?) {
        super.onInitializeComponent(applicationContext, bundle)
        discordData = DiscordData.fromBundle(bundle) ?: return
        RemoSettingsUtil.with(applicationContext){

        }
    }

    val messageHandler = object : ListenerAdapter() {
        override fun onMessageReceived(event: MessageReceivedEvent) {
            val msg = event.message
            val channelName = msg.channel.name.toLowerCase(Locale.US)
            if(channelName != channel?.name?.toLowerCase(Locale.US) && channelName != "all_bots") return

            when(msg.contentRaw){
                "!ping" -> pong(event)
                "!estop" -> sendCommandAsOwner("/estop")
                "!honk" -> sendCommandAsOwner("/honk")
            }
        }
    }

    private fun pong(event: MessageReceivedEvent) {
        val channel = event.channel
        val time = System.currentTimeMillis()
        channel.sendMessage("Pong!") /* => RestAction<Message> */
            .queue { response /* => Message */ ->
                response.editMessageFormat(
                    "Pong: %d ms",
                    System.currentTimeMillis() - time
                ).queue()
            }
    }

    private fun sendCommandAsOwner(command: String) {
        eventDispatcher?.handleMessage(ComponentType.CUSTOM, EVENT_MAIN,
            RemoCommandHandler.Packet(command,
                user = null,
                isModerator = true,
                isOwner = true
        ), this)
    }

    override fun disableInternal() {
        jda?.shutdown()
    }

    override fun enableInternal() {
        jda = JDABuilder(discordData.token).also {builder ->
            // Set activity (like "playing Something")
            //builder.setActivity(EntityBuilder.createActivity("Remo.TV", null, Activity.ActivityType.DEFAULT))
            builder.addEventListeners(messageHandler)
        }.build()
    }

    override fun handleExternalMessage(message: ComponentEventObject): Boolean {
        if(message.source is RemoSocketComponent || message.source is RemoCommandHandler){
            handleSocketCommand(message)
        }
        return super.handleExternalMessage(message)
    }

    private fun handleSocketCommand(message: ComponentEventObject) {
        when(message.data) {
            is Channel -> {
                channel = message.data as Channel
            }
        }
    }

    override fun getType(): ComponentType {
        return ComponentType.CUSTOM
    }
}