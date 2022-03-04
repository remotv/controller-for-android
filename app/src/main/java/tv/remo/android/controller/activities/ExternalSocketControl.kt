package tv.remo.android.controller.activities

import android.content.Context
import android.os.Bundle
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import org.btelman.controlsdk.enums.ComponentStatus
import org.btelman.controlsdk.enums.ComponentType
import org.btelman.controlsdk.interfaces.ComponentEventListener
import org.btelman.controlsdk.interfaces.ControlSDKMessenger
import org.btelman.controlsdk.interfaces.IController
import org.btelman.controlsdk.models.Component
import org.btelman.controlsdk.models.ComponentEventObject
import org.json.JSONObject
import tv.remo.android.controller.ServiceInterface
import tv.remo.android.controller.sdk.interfaces.RemoCommandSender
import tv.remo.android.controller.sdk.models.api.Channel
import tv.remo.android.controller.sdk.utils.SocketListener

class ExternalSocketControl : Component(), IController {
    private var serviceInterface: ServiceInterface? = null
    private var controlSDKMessenger: ControlSDKMessenger? = null
    private var socket: WebSocket? = null
    private val listener = SocketListener()
    private var url: String = "ws://localhost:4342"
    var request : Request? = null
    private val socketClient = OkHttpClient()

    override fun onControlAPI(controlSDKMessenger: ControlSDKMessenger) {
        super.onControlAPI(controlSDKMessenger)
        this.controlSDKMessenger = controlSDKMessenger
    }

    override fun onInitializeComponent(applicationContext: Context, bundle: Bundle?) {
        super.onInitializeComponent(applicationContext, bundle)
        log.d("onInitializeComponent")
        connectToWebSocket()
//        serviceInterface = ServiceInterface(context!!, onServiceBind, onServiceStatus)
//        serviceInterface?.setup(false)
    }
//
//    private val onServiceStatus : (Operation) -> Unit = { serviceStatus ->
////        binding.powerButton.let {
////            binding.powerButton.setTextColor(parseColorForOperation(serviceStatus))
////            val isLoading = serviceStatus == Operation.LOADING
////            binding.powerButton.isEnabled = !isLoading
////            if(isLoading) return@let //processing command. Disable button
////            recording = serviceStatus == Operation.OK
////            if(recording) {
////                handleSleepLayoutTouch()
////            }
////            else{
////                binding.remoChatView.keepScreenOn = false //go ahead and remove the flag
////            }
////        }
////        streamStatus = serviceStatus
////        autoStartStreamIfAppropriate()
//    }
//
//    private val onServiceBind : (Operation) -> Unit = { serviceBoundState ->
////        binding.powerButton.isEnabled = serviceBoundState == Operation.OK
////        boundToService = serviceBoundState == Operation.OK
////        autoStartStreamIfAppropriate()
//    }

    override fun disableInternal() {
        log.d("disableInternal")
        val status = JSONObject()
        status.put("type","streamState")
        status.put("state","down")
        socket?.send(status.toString())
    }

    override fun enableInternal() {
        log.d("enableInternal")
        val status = JSONObject()
        status.put("type","streamState")
        status.put("state","connect")
        socket?.send(status.toString())
    }

    override fun getType(): ComponentType {
        return ComponentType.CUSTOM
    }

    override fun onRemoved() {
        log.d("onRemoved")
        socket?.close(1000, "service ended normally")
        socket = null
    }

    override fun setEventListener(listener: ComponentEventListener?) {
        super<Component>.setEventListener(listener)
    }

    override fun handleExternalMessage(message: ComponentEventObject): Boolean {
        if(message.source is RemoCommandSender){
            handleSocketCommand(message)
        }
        return super.handleExternalMessage(message)
    }

    private fun handleSocketCommand(message: ComponentEventObject) {
        when(message.data){
            is Channel -> {
                val status = JSONObject()
                status.put("type","streamState")
                status.put("state","up")
                socket?.send(status.toString())
            }
            is String -> {
                handleStringCommand(message.data as String)
            }
        }
    }

    private fun handleStringCommand(s: String) {
        val status = JSONObject()
        status.put("type","command")
        status.put("command",s)
        socket?.send(status.toString())
    }

    private fun connectToWebSocket(){
        status = ComponentStatus.CONNECTING
        socket?.close(1000, "service ended normally")
        request = Request.Builder().url(url!!).build()
//        socketClient.connectionPool().evictAll()
        socket = socketClient.newWebSocket(request!!, listener)
        subToSocketEvents(listener)

    }

    private fun subToSocketEvents(listener: SocketListener) {
        listener.on(SocketListener.ON_CLOSE) {
            status = ComponentStatus.ERROR
            log.d{
                "onClosing $it"
            }
        }.on(SocketListener.ON_OPEN, this::onOpen)
            .on(SocketListener.ON_ERROR, this::handleConnectionError)
            .on(SocketListener.ON_MESSAGE, this::onMessage)
    }

    private fun onMessage(message: String) {
        log.d(message)
        when(message){
            ".start" -> {
                socket?.send(".start")
                handler.post{
                    controlSDKMessenger?.enable()
                }
            }
            ".stop" -> {
                socket?.send(".stop")
                handler.post{
                    controlSDKMessenger?.disable()
                }
            }
            ".restart" -> {
                //TODO
                handler.post{
                    controlSDKMessenger?.disable()
                    controlSDKMessenger?.enable()
                }
                socket?.send(".restart Not implemented")
            }
        }
    }


    private fun handleConnectionError(value : String) {
        status = ComponentStatus.ERROR
        handler.postDelayed({
            //attempt a reconnect every second
            connectToWebSocket()
        }, 1000)
        log.e{
            "Failed to connect : $value"
        }
    }

    private fun onOpen(value : String) {
        socket?.send("!ping")
    }
}