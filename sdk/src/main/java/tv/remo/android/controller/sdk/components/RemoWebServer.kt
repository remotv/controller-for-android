package tv.remo.android.controller.sdk.components

import android.content.Context
import android.os.Bundle
import fi.iki.elonen.NanoHTTPD
import org.btelman.controlsdk.interfaces.ControlSDKMessenger
import org.btelman.controlsdk.interfaces.IController
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.models.StringPref

class RemoWebServer : IController {
    private lateinit var context: Context
    private var api: ControlSDKMessenger? = null
    private val server = object : NanoHTTPD(8080) {
        override fun serve(session: IHTTPSession): Response? {
            when(session.uri){
                "/config" -> {
                    var msg = "<html><body><h1>Robot settings</h1>\n"
                    val parms = session.parms
                    msg += if (parms[RemoSettingsUtil.with(context).apiKey.key] == null) {
                        buildSettings()
                    } else {
                        saveSettings(parms)
                        "<p>Saved!</p>"
                    }
                    return newFixedLengthResponse("$msg</body></html>\n")
                }
//                "/on" -> {
//                    enableRobot() //TODO no components were added!
//                    val msg = "<html><body><h1>Turning robot on</h1>\n"
//                    return newFixedLengthResponse("$msg</body></html>\n")
//                }
//                "/off" -> {
//                    disableRobot()
//                    val msg = "<html><body><h1>Turning robot off</h1>\n"
//                    return newFixedLengthResponse("$msg</body></html>\n")
//                }
                else -> {
                    val msg = "<html><body><h1>Hello</h1>\n"

                    return newFixedLengthResponse("$msg</body></html>\n")
                }
            }
        }
    }

    override fun onInitializeComponent(applicationContext: Context, bundle: Bundle?) {
        server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
        context = applicationContext
        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n")
    }

    override fun onControlAPI(controlSDKMessenger: ControlSDKMessenger) {
        super.onControlAPI(controlSDKMessenger)
        api = controlSDKMessenger
    }

    override fun onRemoved() {
        server.stop()
    }

    private fun saveSettings(parms: Map<String, String>) {
        RemoSettingsUtil.with(context){ settings ->
            trySaveStringSetting(settings.apiKey, parms)
            trySaveStringSetting(settings.channelId, parms)
            trySaveStringSetting(settings.serverOwner, parms)
        }
    }

    private fun trySaveStringSetting(pref : StringPref, parms: Map<String, String>){
        parms[pref.key]?.takeIf { it.isNotEmpty() }?.let {
            pref.savePref(it)
        }
    }

    private fun enableRobot() {
        api?.enable()
    }

    private fun disableRobot() {
        api?.disable()
    }

    private fun buildSettings() : String{
        return RemoSettingsUtil.with(context){
            var msg = "<form action='?' method='get'>"
            msg += getSettingHtml(it.apiKey.key, "API key")
            msg += getSettingHtml(it.channelId.key,"Channel name")
            msg += getSettingHtml(it.serverOwner.key,"Owner name")
            msg += "<button>Save</button>"
            msg += "</form>\n"
            return@with msg
        }
    }

    fun getSettingHtml(key : String, userValue : String = key) : String{
        return "\n<p>$userValue: <input type='text' name='$key'></p>"
    }
}