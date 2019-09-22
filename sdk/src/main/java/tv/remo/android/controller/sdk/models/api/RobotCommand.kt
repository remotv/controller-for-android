package tv.remo.android.controller.sdk.models.api


import com.google.gson.annotations.SerializedName

data class RobotCommand(
    @SerializedName("button")
    val button: Button,
    @SerializedName("channel")
    val channel: String,
    @SerializedName("controls_id")
    val controlsId: String,
    @SerializedName("server")
    val server: String,
    @SerializedName("user")
    val user: User
)