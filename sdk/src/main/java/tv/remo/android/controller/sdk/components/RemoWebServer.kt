package tv.remo.android.controller.sdk.components

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.models.StringPref

class RemoWebServer (val context: Context, val onSettingsUpdated : (()->Unit)? = null){
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
                        onSettingsUpdated?.invoke()
                        "<p>Saved!</p>"
                    }
                    return newFixedLengthResponse("$msg</body></html>\n")
                }
                else -> {
                    val msg = "<html><body><h1>Hello</h1>\n"

                    return newFixedLengthResponse("$msg</body></html>\n")
                }
            }
        }
    }

    fun open(){
        server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n")
    }

    fun close(){
        server.stop()
    }

    private fun saveSettings(parms: Map<String, String>) {
        RemoSettingsUtil.with(context){ settings ->
            trySaveStringSetting(settings.apiKey, parms)
            trySaveStringSetting(settings.serverOwner, parms)
        }
    }

    private fun trySaveStringSetting(pref : StringPref, parms: Map<String, String>){
        parms[pref.key]?.takeIf { it.isNotEmpty() }?.let {
            pref.savePref(it)
        }
    }

    private fun buildSettings() : String{
        return RemoSettingsUtil.with(context){
            var msg = "<form action='?' method='get'>"
            msg += getSettingHtml(it.apiKey, "API key", hide = true)
            msg += getSettingHtml(it.serverOwner,"Owner name")
            msg += "<input type='submit' value='Submit'\"'>"
            msg += "</form>\n"
            return@with msg
        }
    }

    private fun getSettingHtml(pref : StringPref, label : String = pref.key, hide : Boolean = false) : String{
        val value = if(hide) "" else pref.getPref()
        return "\n<p>$label: <input type='${if(hide) "password" else "text"}' name='${pref.key}' value='$value'/></p>"
    }
}