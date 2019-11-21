package tv.remo.android.controller.sdk.models.api


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Message(
    @SerializedName("badges")
    val badges: List<String>,
    @SerializedName("broadcast")
    val broadcast: String,
    @SerializedName("chat_id")
    val chatId: String,
    @SerializedName("display_message")
    val displayMessage: Boolean,
    @SerializedName("id")
    val id: String,
    @SerializedName("message")
    var message: String,
    @SerializedName("sender")
    val sender: String,
    @SerializedName("sender_id")
    val senderId: String,
    @SerializedName("server_id")
    val serverId: String,
    @SerializedName("channel_id")
    val channelId: String,
    @SerializedName("time_stamp")
    val timeStamp: Long,
    @SerializedName("type")
    val type: String
) : Serializable{
    companion object{
        const val serialversionUID = 3124135231234895L
    }
}