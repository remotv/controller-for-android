package tv.remo.android.controller.sdk.utils

import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.json.JSONObject

fun Array<out Any>.getJsonObject() : JSONObject?{
    if(size == 0) return null
    return this[0] as? JSONObject
}

fun LocalBroadcastManager.sendJson(action : String, json : JSONObject){
    val intent = JsonObjectUtils.createIntentWithJson(action, json)
    sendBroadcast(intent)
}