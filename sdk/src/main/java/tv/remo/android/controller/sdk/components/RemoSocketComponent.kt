package tv.remo.android.controller.sdk.components

import android.content.Context
import android.os.Bundle
import android.util.Log
import okhttp3.*
import okio.ByteString
import org.btelman.controlsdk.enums.ComponentType
import org.btelman.controlsdk.models.Component
import org.json.JSONObject

/**
 * Remo Socket component
 *
 * Note: Do not instantiate in the activity! Must pass it to the ControlSDK Service
 */
class RemoSocketComponent : Component() {
    private var socket: WebSocket? = null
    var apiKey : String? = null
    var channelId : String? = null
    val request = Request.Builder().url("ws://dev.remo.tv:3231/").build()
    val client = OkHttpClient()

    override fun onInitializeComponent(applicationContext: Context, bundle: Bundle?) {
        super.onInitializeComponent(applicationContext, bundle)
        apiKey = bundle?.getString(API_TOKEN_BUNDLE_KEY)
        channelId = bundle?.getString(CHANNEL_ID_BUNDLE_KEY)
        apiKey?: throw Exception("api key not found")
    }

    override fun enableInternal() {
        socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                Log.d("TAG","onOpen")
                val json = "{\"e\": \"AUTHENTICATE_ROBOT\", \"d\": {\"token\": \"$apiKey\"}}"
                webSocket.send(json)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Log.d("TAG","onFailure")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                Log.d("TAG","onClosing $reason $code")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("TAG",text)
                super.onMessage(webSocket, text)
                val jsonObject = JSONObject(text)
                Log.d("TAG","Validated JSON")
                if(jsonObject["e"] == "ROBOT_VALIDATED"){
                    val host = jsonObject.getJSONObject("d")["host"] as String
                    val str = "{\"e\":\"GET_CHANNELS\",\"d\":{\"server_id\":\"$host\"}}"
                    webSocket.send(str)
                }
                if(jsonObject["e"] == "SEND_ROBOT_SERVER_INFO"){
                    //sendMessage(webSocket, "JOIN_CHANNEL", channelId?:"") //TODO grab first one if channel ID not found
                    //sendMessage(webSocket, "GET_CHAT", chat)
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                Log.d("TAG","onClosed $reason $code")
            }
        })
        client.dispatcher().executorService().shutdown()
    }

    override fun disableInternal() {
        socket?.close(1000, "service ended normally")
    }

    override fun getType(): ComponentType {
        return ComponentType.CUSTOM
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