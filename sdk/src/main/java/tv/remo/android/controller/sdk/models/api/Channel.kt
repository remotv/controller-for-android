package tv.remo.android.controller.sdk.models.api


import com.google.gson.annotations.SerializedName

data class Channel(
    @SerializedName("chat")
    val chat: String,
    @SerializedName("controls")
    val controls: String,
    @SerializedName("created")
    val created: String,
    @SerializedName("display")
    val display: String,
    @SerializedName("host_id")
    val hostId: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("robot")
    val robot: Any,
    @SerializedName("settings")
    val settings: Settings
)