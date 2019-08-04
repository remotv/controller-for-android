package tv.remo.android.controller.sdk.models.api


import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("created")
    val created: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("status")
    val status: Status,
    @SerializedName("type")
    val type: List<String>,
    @SerializedName("username")
    val username: String
)