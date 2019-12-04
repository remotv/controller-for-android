package tv.remo.android.controller.sdk.models.api

import com.google.gson.annotations.SerializedName

data class Moderation(
    @SerializedName("event")
    val event: String,
    @SerializedName("server_id")
    val serverId: String,
    @SerializedName("user")
    val user: String
)