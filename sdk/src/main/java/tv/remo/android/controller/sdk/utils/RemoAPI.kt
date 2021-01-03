package tv.remo.android.controller.sdk.utils

import android.content.Context
import com.google.gson.Gson
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import tv.remo.android.controller.sdk.models.api.Channel
import java.io.IOException

/**
 * Created by Brendon on 12/25/2020.
 */
class RemoAPI (private val context: Context){
    private val apiClient = OkHttpClient()
    fun authRobot(apiKey : String, callback : (message : Channel?, error: Exception?)->Unit){
        val path = EndpointBuilder.buildUrl(context, "/api/dev/robot/auth")
        val jsonAuth = "{\"token\":\"${apiKey}\"}"
        postJSON(jsonAuth, path){ json: JSONObject?, exception: Exception? ->
            var finalException = exception
            var channel : Channel? = null
            json?.let{
                try {
                    val status = json.getString("status")
                    if(status == "success!"){
                        channel = Gson().fromJson(json.getString("robot"), Channel::class.java)
                    }
                    else{
                        finalException = Exception("Status for get channel was $status")
                    }
                } catch (e: Exception) {
                    finalException = e
                }
            }
            callback(channel, finalException)
            return@postJSON
        }
    }

    private fun postJSON(json : String, path : String, callback : (JSONObject?, Exception?) -> Unit){
        val body = RequestBody.create(MediaType.parse("application/json"), json)
        val request = Request.Builder().url(path).post(body).build()
        apiClient.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.json()?.let {
                    callback(it, null)
                }?: callback(null, Exception("unable to parse response as JSON"))
            }
        })
    }

    fun cancelRequests(){
        for (call in apiClient.dispatcher().runningCalls()) {
            call.cancel()
        }
    }
}

private fun Response.json() : JSONObject?{
    try{
        this.body()?.string()?.let {
            return JSONObject(it).also {
                this.body()?.close()
            }
        }
    }catch (e : JSONException){
        e.printStackTrace()
    }
    return null
}