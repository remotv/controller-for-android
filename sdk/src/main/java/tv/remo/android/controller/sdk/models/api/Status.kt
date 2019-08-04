package tv.remo.android.controller.sdk.models.api


import com.google.gson.annotations.SerializedName

data class Status(
    @SerializedName("timeout")
    val timeout: Boolean
)