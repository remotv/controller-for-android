package tv.remo.android.controller.sdk.models.api


import com.google.gson.annotations.SerializedName

data class Channel(
    @SerializedName("chat_id")
    val chat: String,
    @SerializedName("controls_id")
    val controls: String,
    @SerializedName("created")
    val created: String,
    @SerializedName("server_id")
    val hostId: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("heartbeat")
    val heartbeat : String
)