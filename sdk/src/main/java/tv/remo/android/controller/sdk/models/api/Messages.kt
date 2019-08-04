package tv.remo.android.controller.sdk.models.api

import com.google.gson.annotations.SerializedName

data class Messages(
    @SerializedName("created")
    val created: String,
    @SerializedName("host_id")
    val hostId: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("messages")
    val messages: List<Message>,
    @SerializedName("name")
    val name: String
)