package tv.remo.android.controller.sdk.utils

import android.content.Context
import tv.remo.android.controller.sdk.RemoSettingsUtil

/**
 * Get the endpoint from resources
 */
object EndpointBuilder {
    fun getEndpointUrl(context : Context) : String{
        return RemoSettingsUtil.with(context).endpoint.getPref()
    }

    fun getVideoUrl(context : Context, channel : String) : String{
        return "http://${getEndpointUrl(context)}:1567/transmit?name=$channel-video"
    }

    fun getAudioUrl(context: Context, channel : String) : String{
        return "http://${getEndpointUrl(context)}:1567/transmit?name=$channel-audio"
    }

    fun buildWebsocketUrl(context: Context) : String{
        return "wss://${getEndpointUrl(context)}/"
    }

    fun buildUrl(context: Context, path: String): String {
        val baseUrl = getEndpointUrl(context)
        return "https://$baseUrl/$path"
    }
}