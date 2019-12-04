package tv.remo.android.controller.sdk.utils

import android.content.Intent
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject

/**
 * Simple utility functions for JsonObject
 */
object JsonObjectUtils{

    /**
     * Gets json from url synchronously
     */
    fun getJsonObjectFromUrl(url : String) : JSONObject?{
        val client = OkHttpClient()
        val call = client.newCall(Request.Builder().url(url).build())
        try {
            val response = call.execute()
            if (response.body() != null) {
                return JSONObject(response.body()!!.string())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Get a single string value from the json
     */
    fun getValueJsonObject(url : String, key : String) : String?{
        return getJsonObjectFromUrl(url)?.let{
            try {
                it.getString(key)
            } catch (e: Exception) {
                null
            }
        }
    }

    @Throws(JSONException::class)
    fun createIntentWithJson(action : String, json : JSONObject) : Intent {
        return Intent(action).also {
            json.keys().forEach { key ->
                it.putExtra(key, json.getString(key))
            }
        }
    }
}
