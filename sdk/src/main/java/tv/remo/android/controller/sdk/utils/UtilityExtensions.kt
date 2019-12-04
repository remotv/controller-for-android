package tv.remo.android.controller.sdk.utils

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.json.JSONObject
import java.io.InvalidObjectException
import java.io.Serializable

fun Array<out Any>.getJsonObject() : JSONObject?{
    if(size == 0) return null
    return this[0] as? JSONObject
}

fun LocalBroadcastManager.sendJson(action : String, json : JSONObject){
    val intent = JsonObjectUtils.createIntentWithJson(action, json)
    sendBroadcast(intent)
}

fun <V> LocalBroadcastManager.broadcastKeyValuePair(action : String, keyValuePair : Pair<String, V>){
    broadcastMap(action, HashMap<String, V>().apply {
        put(keyValuePair)
    })
}

fun <K, V> java.util.HashMap<K, V>.put(pair: Pair<K, V>) {
    put(pair.first, pair.second)
}

fun <V> LocalBroadcastManager.broadcastMap(action : String, map : Map<String, V>){
    sendBroadcast(Intent(action).also {
        map.forEach { pair ->
            it.putLazyExtra(pair.key, pair.value)
        }
    })
}

/**
 * Puts unknown variable type in the Intent after resolving its type.
 * Throws exception if type not supported.
 */
@Throws(InvalidObjectException::class)
fun <V> Intent.putLazyExtra(key: String, value: V) {
    when(value){
        //just use the same line after checking type... Compiler auto casts to the right type
        is CharSequence -> putExtra(key, value)
        is Byte -> putExtra(key, value)
        is Long -> putExtra(key, value)
        is Int -> putExtra(key, value)
        is Char -> putExtra(key, value)
        is String -> putExtra(key, value)
        is Boolean -> putExtra(key, value)
        is ByteArray -> putExtra(key, value)
        is LongArray -> putExtra(key, value)
        is IntArray -> putExtra(key, value)
        is CharArray -> putExtra(key, value)
        is BooleanArray -> putExtra(key, value)
        is FloatArray -> putExtra(key, value)
        is Parcelable -> putExtra(key, value)
        is Serializable -> putExtra(key, value)
        is Bundle -> putExtra(key, value)
        else -> {
            throw InvalidObjectException("type not supported! value=${value}")
        }
    }
}
