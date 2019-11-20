package tv.remo.android.controller.sdk.models

import android.util.Log

/**
 * Packet for subscribed events to be triggered if the desired outcome happens.
 * Supports subscribing to an exact string or a String that starts with a subString.
 *
 * Invokes the lambda when hit, with the exact data that triggered the event
 */
data class CommandSubscriptionData(
    val hasToEqual : Boolean = true,
    val message : String,
    val lambda : (String)->Unit
){
    fun callFun(extMessage : String){
        try {
            extMessage.replace(message, "").let(lambda)
        } catch (e: Exception) {
            Log.e("CommandSubscriptionData",
                "failed to handle $extMessage for $message", e)
        }
    }
}