package tv.remo.android.controller.sdk.models.api


import com.google.gson.annotations.SerializedName

data class Invite(
    @SerializedName("created")
    val created: String,
    @SerializedName("created_by")
    val createdBy: String,
    @SerializedName("expires")
    val expires: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("server_id")
    val serverId: String,
    @SerializedName("status")
    val status: String
)