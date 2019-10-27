package tv.remo.android.controller.sdk.components

import android.util.Log
import okhttp3.*
import org.btelman.controlsdk.tts.TTSBaseComponent
import java.io.IOException

object TTSFilterUtil {
    var list = ArrayList<String>().also {
        it.add("QWERTY")
        it.add("WWW")
    }
    fun precache(){
        var client = OkHttpClient.Builder().build()
        val request = Request.Builder().url(
            "https://raw.githubusercontent.com/RobertJGabriel/Google-profanity-words/master/list.txt"
        ).build()
        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                response.body()?.string().also {
                    response.body()?.close()
                }?.let {body ->
                    body.split("\n").forEach {
                        if(it.trim().isNotEmpty())
                            list.add(it)
                    }
                }
            }
        })
    }


    fun filter(data : TTSBaseComponent.TTSObject) : Boolean{
        val listt = list
        val text = data.text
        listt.forEach {
            if((" $text ").contains(" $it ")){
                Log.d("FILTER", "Filtering out message")
                data.text = ""
                return true
            }
        }
        return false
    }
}
