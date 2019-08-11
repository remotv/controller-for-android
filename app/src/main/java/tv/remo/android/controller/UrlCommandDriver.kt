package tv.remo.android.controller

import android.app.Activity
import android.content.Context
import android.content.Intent
import okhttp3.*
import org.btelman.controlsdk.enums.ComponentStatus
import org.btelman.controlsdk.hardware.interfaces.DriverComponent
import org.btelman.controlsdk.hardware.interfaces.HardwareDriver
import java.io.IOException
import java.nio.charset.Charset

@DriverComponent("Send data via a web call given a URL. Useful for wifi boards or smart hubs")
class UrlCommandDriver : HardwareDriver{
    val client = OkHttpClient()

    override fun clearSetup(context: Context) {
    }

    override fun disable() {
    }

    override fun enable() {

    }

    override fun getAutoReboot(): Boolean {
        return false
    }

    override fun getStatus(): ComponentStatus {
        return ComponentStatus.STABLE
    }

    override fun initConnection(context: Context) {

    }

    override fun isConnected(): Boolean {
        return true
    }

    override fun needsSetup(activity: Activity): Boolean {
        return false
    }

    override fun receivedComponentSetupDetails(context: Context, intent: Intent?) {

    }

    override fun send(byteArray: ByteArray): Boolean {
        val url = "http://192.168.1.124/${byteArray.toString(Charset.defaultCharset()).trim('\r', '\n')}"
        client.newCall(
            Request.Builder().url(url).build())
            .enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
            }
        })
        return true
    }

    override fun setupComponent(activity: Activity, force: Boolean): Int {
        return -1
    }

    override fun usesCustomSetup(): Boolean {
        return false
    }

}