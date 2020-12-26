package tv.remo.android.controller.sdk.models.api

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * Created by Brendon on 12/25/2020.
 */
data class OutgoingMessage(
    @SerializedName("message")
    var message: String,
    @SerializedName("chatId")
    var chatId: String,
    @SerializedName("server_id")
    var server_id: String,
    @SerializedName("username")
    var username: String = "bot" //TODO I don't think this is really needed
){
    fun serialize() : String{
        return Gson().toJson(this)
    }

    companion object{
        fun parse(json : String): OutgoingMessage? {
            return try {
                Gson().fromJson(json, OutgoingMessage::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}