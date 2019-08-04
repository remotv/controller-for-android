package tv.remo.android.controller.sdk.models.api


import com.google.gson.annotations.SerializedName

data class Button(
    @SerializedName("hot_key")
    val hotKey: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("label")
    val label: String
)