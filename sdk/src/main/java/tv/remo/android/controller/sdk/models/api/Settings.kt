package tv.remo.android.controller.sdk.models.api


import com.google.gson.annotations.SerializedName

data class Settings(
    @SerializedName("public")
    val `public`: Boolean,
    @SerializedName("access")
    val access: String
)