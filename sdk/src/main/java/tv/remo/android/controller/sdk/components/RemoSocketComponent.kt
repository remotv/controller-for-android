package tv.remo.android.controller.sdk.components

import android.content.Context
import android.os.Bundle
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import org.btelman.controlsdk.enums.ComponentStatus
import org.btelman.controlsdk.enums.ComponentType
import org.btelman.controlsdk.models.Component
import org.btelman.controlsdk.models.ComponentEventObject
import org.btelman.controlsdk.tts.TTSBaseComponent
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.interfaces.RemoCommandSender
import tv.remo.android.controller.sdk.models.api.*
import tv.remo.android.controller.sdk.utils.*

/**
 * Remo Socket component
 *
 * Note: Do not instantiate in the activity! Must pass it to the ControlSDK Service
 */
class RemoSocketComponent : Component() , RemoCommandSender {
    private var socket: WebSocket? = null
    var apiKey : String? = null
    var activeChannelId : String? = null
    private val listener = SocketListener()
    private var url: String? = null
    var request : Request? = null
    private val socketClient = OkHttpClient()
    private lateinit var remoAPI: RemoAPI

    var allowChat = false
    private var activeChannel : Channel? = null

    override fun onInitializeComponent(applicationContext: Context, bundle: Bundle?) {
        super.onInitializeComponent(applicationContext, bundle)
        remoAPI = RemoAPI(applicationContext)
        apiKey = bundle?.getString(API_TOKEN_BUNDLE_KEY)
        url = EndpointBuilder.buildWebsocketUrl(applicationContext)
        activeChannelId = bundle?.getString(CHANNEL_ID_BUNDLE_KEY)
        apiKey?: throw Exception("api key not found")
        RemoSettingsUtil.with(applicationContext){
            allowChat = it.siteTextToSpeechEnabled.getPref()
        }
    }

    override fun enableInternal() {
        subToSocketEvents(listener)
        attemptReconnect()
    }

    override fun disableInternal() {
        socket?.close(1000, "service ended normally")
    }

    override fun getType(): ComponentType {
        return ComponentType.CUSTOM
    }

    override fun handleExternalMessage(message: ComponentEventObject): Boolean {
        if(message.type == ComponentType.CUSTOM && message.data is RemoSocketChatPacket){
            sendChatMessage((message.data as RemoSocketChatPacket).data)
        }
        return super.handleExternalMessage(message)
    }

    private fun subToSocketEvents(listener: SocketListener) {
        listener.on(SocketListener.ON_CLOSE) {
                status = ComponentStatus.ERROR
                log.d{
                    "onClosing $it"
                }
            }.on(SocketListener.ON_OPEN, this::sendHandshakeAuth)
            .on(SocketListener.ON_ERROR, this::handleConnectionError)
            .on("ROBOT_VALIDATED", this::getChannelAndConnect)
            .on("MESSAGE_RECEIVED", this::sendChatUpwards)
            .on("BUTTON_COMMAND", this::sendCommandUpwards)
            .on("LOCAL_MODERATION", this::processChatModeration)
            .on(SocketListener.ON_MESSAGE) {
                log.v{
                    "Socket Message: $it"
                }
            }
        //.on("SEND_CHAT") //TODO? of type Messages
    }

    private fun handleConnectionError(value: String) {
        status = ComponentStatus.ERROR
        handler.postDelayed({
            //attempt a reconnect every second
            attemptReconnect()
        }, 1000)
        log.e{
            "Failed to connect : $value"
        }
    }

    private fun attemptReconnect() {
        status = ComponentStatus.CONNECTING
        url ?: return
        socket?.close(1000, "service ended normally")
        request = Request.Builder().url(url!!).build()
        socketClient.connectionPool().evictAll()
        socket = socketClient.newWebSocket(request!!, listener)
    }

    private fun sendCommandUpwards(it: String) {
        Gson().fromJson(it, RobotCommand::class.java).also {
            eventDispatcher?.handleMessage(
                ComponentType.CUSTOM, EVENT_MAIN,
                RemoCommandHandler.Packet(it.button.command, it.user), this
            )
        }
    }

    /**
     * Send chat upwards using the event manager so other classes can intercept
     */
    private fun sendChatUpwards(json: String) {
        Gson().fromJson(json, Message::class.java).also { rawMessage ->
            if(rawMessage.type == "robot") return //we don't want the robot talking to itself
            if(activeChannel?.id != rawMessage.channelId) return
            val message = lookForCommandsAndMaybeReplace(rawMessage)

            if(searchAndSendCommand(message) || !allowChat) return
            ChatUtil.broadcastChatMessage(context!!, message)
            //filter urls out after command sending in case something is implemented to handle urls
            if(message.message.isUrl()) return

            val data = TTSBaseComponent.TTSObject(
                message.message,
                1.0f,
                message.sender,
                false,
                false,
                true,
                message.badges.contains("owner"),
                message_id = message.id
            )
            eventDispatcher?.handleMessage(ComponentEventObject(ComponentType.TTS, EVENT_MAIN, data, this))
        }.also {
            //TODO store in local database?
        }
    }

    private fun lookForCommandsAndMaybeReplace(message: Message): Message {
        return when(message.type){
            "self" -> {
                 message.also {
                     it.message = it.sender + " " + it.message
                 }
            }
            else -> message
        }
    }

    private fun searchAndSendCommand(message: Message) : Boolean{
        //TODO get list of mods or users that have access to commands
        if(message.message.startsWith(".", "/")){
            if(!message.badges.contains("owner")) return true
            eventDispatcher?.handleMessage(
                ComponentEventObject(ComponentType.CUSTOM, EVENT_MAIN,
                    RemoCommandHandler.Packet(
                        message.message, null, isModerator = true, isOwner = true
                    ), this)
            )
            return true
        }
        return false
    }

    private fun sendChatMessage(message: String) {
        activeChannel?.apply {
            sendMessage(socket, "ROBOT_MESSAGE_SENT", OutgoingMessage(message, chat, hostId).serialize(), false)
        }
    }

    private fun processChatModeration(json: String) {
        Gson().fromJson(json, Moderation::class.java).also { moderationData ->
            if(moderationData.serverId == activeChannel?.hostId){
                ChatUtil.broadcastChatMessageRemovalRequest(context!!, moderationData.user)
            }
        }
    }

    private fun getChannelAndConnect(json: String) {
        status = ComponentStatus.CONNECTING
        remoAPI.authRobot(apiKey!!) { channel: Channel?, exception: java.lang.Exception? ->
            channel?.let {
                verifyAndSubToChannel(channel)
            } ?: handleConnectionError(exception?.toString() ?: "Unable to get channel")
        }
    }

    private fun verifyAndSubToChannel(channel: Channel) {
        status = ComponentStatus.STABLE
        channel.apply {
            activeChannel = this
            if(RemoSettingsUtil.with(context!!).showStartMessage.getPref())
                sendChatMessage("Robot connected. Commands cleared")
            socket?.let { _socket ->
                sendMessage(_socket, "JOIN_CHANNEL", id) //apparently this value is ignored...
                sendMessage(_socket, "GET_CHAT", chat) //this value is not ignored though...
            }
            sendChannelUpwards(this)
        }
    }

    private fun sendChannelUpwards(channel: Channel) {
        eventDispatcher?.handleMessage(
            ComponentEventObject(ComponentType.CUSTOM, EVENT_MAIN, channel, this)
        )
    }

    private fun sendHandshakeAuth(value : String) {
        sendMessage(socket, "AUTHENTICATE_ROBOT", "{\"token\": \"$apiKey\"}", false)
    }

    data class RemoSocketChatPacket(val data : String)

    companion object{
        const val API_TOKEN_BUNDLE_KEY = "API_TOKEN"
        const val CHANNEL_ID_BUNDLE_KEY = "CHANNEL_ID"
        const val REMO_CHAT_MESSAGE_WITH_NAME_BROADCAST = "tv.remo.chat.chat_message_with_name"
        const val REMO_CHAT_USER_REMOVED_BROADCAST = "tv.remo.chat.user_removed"

        fun createBundle(apiKey : String, channelId : String? = null) : Bundle {
            return Bundle().apply {
                putString(API_TOKEN_BUNDLE_KEY, apiKey)
                channelId?.let { putString(CHANNEL_ID_BUNDLE_KEY, it) }
            }
        }

        private fun sendMessage(webSocket: WebSocket?, event: String, data: String, wrapData : Boolean = true) {
            val json = "{\"e\":\"$event\",\"d\":${if(wrapData) "\"$data\"" else data}}"
            webSocket?.send(json)
        }
    }
}