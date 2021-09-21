package tv.remo.android.controller.sdk.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import org.btelman.controlsdk.enums.ComponentStatus
import org.btelman.controlsdk.hardware.interfaces.DriverComponent
import org.btelman.controlsdk.hardware.interfaces.HardwareDriver
import org.btelman.controlsdk.interfaces.ComponentEventListener
import org.btelman.controlsdk.utils.InlineBroadcastReceiver
import tv.remo.android.controller.sdk.utils.ChatUtil
import java.nio.charset.Charset

/**
 * Created by Brendon on 9/22/2019.
 */
@DriverComponent(
    description = "Broadcasts controls to other apps that were given permission. Only supports ArduinoSendString",
    requiresSetup = false
)
class RemoBroadcaster : HardwareDriver{
    private lateinit var context: Context
    private var eventListener: ComponentEventListener? = null
    private var sendChatBroadcastReceiver: InlineBroadcastReceiver? = null
    private var stopStreamBroadcastReceiver: InlineBroadcastReceiver? = null

    override fun enable() {
        sendChatBroadcastReceiver = InlineBroadcastReceiver("tv.remo.android.controller.sdk.chat.send") { _, intent: Intent? ->
            val message = intent?.getStringExtra("message")
            if (eventListener != null && message != null) {
                ChatUtil.sendToSiteChat(eventListener, message)
            }
        }.also { it.register(context) }

        stopStreamBroadcastReceiver = InlineBroadcastReceiver("tv.remo.android.controller.sdk.stream.stop") { _, _ ->
            val stopIntent = Intent("org.btelman.controlsdk.request.stop")
            stopIntent.`package` = context.packageName
            context.sendBroadcast(stopIntent)
        }.also { it.register(context) }

        sendToBroadcast("start")
    }

    override fun disable() {
        sendToBroadcast("stop")
        sendChatBroadcastReceiver?.unregister(context)
        stopStreamBroadcastReceiver?.unregister(context)
    }

    override fun setEventListener(listener: ComponentEventListener?) {
        eventListener = listener
    }

    override fun getStatus(): ComponentStatus {
        return ComponentStatus.STABLE
    }

    override fun initConnection(context: Context) {
        this.context = context
    }

    override fun isConnected(): Boolean {
        return true
    }

    override fun send(byteArray: ByteArray): Boolean {
        sendToBroadcast(byteArray.toString(Charset.defaultCharset()))
        return true
    }

    fun sendToBroadcast(data: String){
        val intent = Intent("tv.remo.android.controller.sdk.socket.controls").apply {
            putExtra("command", data)
        }
        context.sendBroadcast(intent)
    }

    override fun setupComponent(activity: Activity, force: Boolean): Int {
        return -1
    }

    override fun usesCustomSetup(): Boolean {
        return false
    }

    override fun clearSetup(context: Context) {

    }

    override fun getAutoReboot(): Boolean {
        return false
    }

    override fun needsSetup(activity: Activity): Boolean {
        return false
    }

    override fun receivedComponentSetupDetails(context: Context, intent: Intent?) {

    }
}
