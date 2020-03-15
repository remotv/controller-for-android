package tv.remo.android.controller.sdk.models.api


import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

data class Message(
    @SerializedName("badges")
    var badges: List<String>,
    @SerializedName("broadcast")
    var broadcast: String,
    @SerializedName("chat_id")
    var chatId: String,
    @SerializedName("display_message")
    var displayMessage: Boolean,
    @SerializedName("id")
    var id: String,
    @SerializedName("message")
    var message: String,
    @SerializedName("sender")
    var sender: String,
    @SerializedName("sender_id")
    var senderId: String,
    @SerializedName("server_id")
    var serverId: String,
    @SerializedName("channel_id")
    var channelId: String,
    @SerializedName("time_stamp")
    var timeStamp: Long,
    @SerializedName("type")
    var type: String
) : Serializable{
    companion object{
        const val serialversionUID = 3124135231234895L
        fun createDummyMessage(username : String, message: String) : Message{
            val json = "{\"message\":\"test\",\"sender\":\"ReconDelta090\",\"sender_id\":\"user-6a9591cc-f3d3-4e47-a208-e749679a899a\",\"chat_id\":\"chat-8a05f730-c663-434d-9f24-6d8c24453c5f\",\"server_id\":\"serv-46437781-4a9b-4531-9db1-74bc2f818b58\",\"id\":\"mesg-ec54bf9a-23d4-4fa6-b6f1-9a5c8e3e2440\",\"time_stamp\":1574296097994,\"broadcast\":\"\",\"channel_id\":\"chan-7a304995-cba0-463c-81a6-ffeffc059058\",\"display_message\":true,\"badges\":[\"owner\"],\"type\":\"\"}"
            return Gson().fromJson(json, Message::class.java).also {
                it.sender = username
                it.displayMessage = true
                it.message = message
                it.timeStamp = System.currentTimeMillis()
                val id = UUID.randomUUID().toString()
                it.id = id
            }
        }
    }
}