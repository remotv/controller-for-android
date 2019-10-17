package tv.remo.android.controller.sdk.utils

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

/**
 * Created by Brendon on 8/3/2019.
 */
class SocketListener : WebSocketListener(){
    val listeners = HashMap<String, (String)->Unit>()

    fun on(event : String, callback : (String)->Unit) : SocketListener{
        listeners[event] = callback
        return this
    }

    fun revoke(event: String){
        listeners.remove(event)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        listeners[ON_OPEN]?.invoke(response.message())
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        listeners[ON_ERROR]?.invoke(t.message ?: response?.message() ?: "UNKNOWN")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        listeners[ON_CLOSING]?.invoke("$code:$reason")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        try {
            val message = JSONObject(text)
            listeners[message.getString("e")]?.invoke(message.getJSONObject("d").toString())
        }catch (_ : Exception){

        }
        listeners[ON_MESSAGE]?.invoke(text)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        listeners[ON_CLOSE]?.invoke("$code:$reason")
    }

    companion object{
        const val ON_MESSAGE = "SOCKET.MESSAGE.STRING"
        const val ON_OPEN = "SOCKET.OPEN"
        const val ON_CLOSING = "SOCKET.CLOSING"
        const val ON_CLOSE = "SOCKET.CLOSE"
        const val ON_ERROR = "SOCKET.ERROR"
    }
}