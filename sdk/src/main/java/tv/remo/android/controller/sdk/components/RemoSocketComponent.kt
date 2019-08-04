package tv.remo.android.controller.sdk.components

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import org.btelman.controlsdk.enums.ComponentType
import org.btelman.controlsdk.models.Component
import org.btelman.controlsdk.models.ComponentEventObject
import org.btelman.controlsdk.tts.TTSBaseComponent
import org.json.JSONObject
import tv.remo.android.controller.sdk.models.api.Channel
import tv.remo.android.controller.sdk.models.api.Message
import tv.remo.android.controller.sdk.models.api.RobotCommand
import tv.remo.android.controller.sdk.models.api.RobotServerInfo
import tv.remo.android.controller.sdk.utils.SocketListener

/**
 * Remo Socket component
 *
 * Note: Do not instantiate in the activity! Must pass it to the ControlSDK Service
 */
class RemoSocketComponent : Component() {
    private var socket: WebSocket? = null
    var apiKey : String? = null
    var activeChannelId : String? = null
    val request = Request.Builder().url("ws://dev.remo.tv:3231/").build()
    val client = OkHttpClient()
    private var serverInfo: RobotServerInfo? = null
    private var activeChannel : Channel? = null

    override fun onInitializeComponent(applicationContext: Context, bundle: Bundle?) {
        super.onInitializeComponent(applicationContext, bundle)
        apiKey = bundle?.getString(API_TOKEN_BUNDLE_KEY)
        activeChannelId = bundle?.getString(CHANNEL_ID_BUNDLE_KEY)
        apiKey?: throw Exception("api key not found")
    }

    override fun enableInternal() {
        val listener = SocketListener()
        subToSocketEvents(listener)
        socket = client.newWebSocket(request, listener)
        client.dispatcher().executorService().shutdown()
    }

    override fun disableInternal() {
        socket?.close(1000, "service ended normally")
    }

    override fun getType(): ComponentType {
        return ComponentType.CUSTOM
    }

    private fun subToSocketEvents(listener: SocketListener) {
        listener.on(SocketListener.ON_OPEN){
            sendHandshakeAuth()
        }.on(SocketListener.ON_CLOSE){
            Log.d("TAG","onClosing $it")
        }.on(SocketListener.ON_ERROR){
            Log.d("TAG","onFailure $it")
        }.on("ROBOT_VALIDATED"){
            sendChannelsRequest(it)
        }.on("SEND_ROBOT_SERVER_INFO"){
            verifyAndSubToChannel(it)
        }.on(SocketListener.ON_MESSAGE){
            Log.d("SOCKET", it)
        }.on("MESSAGE_RECEIVED"){
            sendChatUpwards(it)
        }.on("BUTTON_COMMAND"){
            sendCommandUpwards(it)
        }
        //.on("SEND_CHAT") //TODO? of type Messages
    }

    private fun sendCommandUpwards(it: String) {
        Gson().fromJson(it, RobotCommand::class.java).also {
            eventDispatcher?.handleMessage(ComponentType.HARDWARE, EVENT_MAIN, it.button.label, this)
        }
    }

    /**
     * Send chat upwards using the event manager so other classes can intercept
     */
    private fun sendChatUpwards(json: String) {
        Gson().fromJson(json, Message::class.java).also {
            if(activeChannel?.chat != it.chatId) return
            if(searchAndSendCommand(it)) return
            val data = TTSBaseComponent.TTSObject(
                it.message,
                1.0f,
                it.sender,
                false,
                false,
                true,
                it.badges.contains("owner"),
                message_id = it.id
            )
            eventDispatcher?.handleMessage(ComponentEventObject(ComponentType.TTS, EVENT_MAIN, data, this))
        }.also {
            //TODO store in local database?
        }
    }

    private fun searchAndSendCommand(message: Message) : Boolean{
        if(message.badges.contains("owner") && message.message.startsWith(".")){
            //TODO
        }
        return false
    }

    private fun verifyAndSubToChannel(json: String) {
        serverInfo = Gson().fromJson(json, RobotServerInfo::class.java).also { serverInfo ->
            for (channel in serverInfo.channels) {
                if(channel.id != activeChannelId) continue
                activeChannel = channel
            }
            activeChannel?.apply {
                socket?.let { _socket ->
                    sendMessage(_socket, "JOIN_CHANNEL", id)
                    sendMessage(_socket, "GET_CHAT", chat)
                }
            }
        }
    }

    private fun sendChannelsRequest(json : String) {
        val jsonObject = JSONObject(json)
        val host = jsonObject.getString("host")
        val str = "{\"e\":\"GET_CHANNELS\",\"d\":{\"server_id\":\"$host\"}}"
        socket?.send(str)
    }

    private fun sendHandshakeAuth() {
        val json = "{\"e\": \"AUTHENTICATE_ROBOT\", \"d\": {\"token\": \"$apiKey\"}}"
        socket?.send(json)
    }

    companion object{
        const val API_TOKEN_BUNDLE_KEY = "API_TOKEN"
        const val CHANNEL_ID_BUNDLE_KEY = "CHANNEL_ID"

        fun createBundle(apiKey : String, channelId : String? = null) : Bundle {
            return Bundle().apply {
                putString(API_TOKEN_BUNDLE_KEY, apiKey)
                channelId?.let { putString(CHANNEL_ID_BUNDLE_KEY, it) }
            }
        }

        private fun sendMessage(webSocket: WebSocket, event: String, data: String) {
            webSocket.send("{\"e\":\"$event\",\"d\":\"$data\"}")
        }
    }
}