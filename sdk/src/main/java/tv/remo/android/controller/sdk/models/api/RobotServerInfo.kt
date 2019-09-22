package tv.remo.android.controller.sdk.models.api


import com.google.gson.annotations.SerializedName

data class RobotServerInfo(
    @SerializedName("channels")
    val channels: List<Channel>,
    @SerializedName("invites")
    val invites: List<Invite>,
    @SerializedName("users")
    val users: List<User>
)