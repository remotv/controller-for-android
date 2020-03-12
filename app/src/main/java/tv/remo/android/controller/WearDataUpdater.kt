package tv.remo.android.controller

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class WearDataUpdater(lifecycleOwner: LifecycleOwner, observer : (ArrayList<String>?)->Unit){
    fun print(value : String){
        Log.d("Main", value)
    }

    private val _observer = Observer<ArrayList<String>> {
        observer(it)
    }

    private val data : MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>()
    }

    private val handlerThread = HandlerThread("networkLooper").also {
        it.start()
    }
    private val networkHandler = Handler(handlerThread.looper)
    private val url = URL("http://remo.tv:3231/api/dev/robot-server/list")

    val poll = Runnable{
        print("poll")
        kotlin.runCatching {
            handlePoll()
        }.exceptionOrNull()?.let {
            it.printStackTrace()
        }
        schedulePoll()
    }

    init {
        data.observe(lifecycleOwner, _observer)
        networkHandler.post(poll)
    }

    fun destroy(){
        data.removeObserver(_observer)
        handlerThread.quit()
    }

    private fun schedulePoll() {
        networkHandler.postDelayed(poll, 30000) /*30 seconds*/
    }

    private fun handlePoll() {
        print("handlePoll")
        val connection = url.openConnection() as HttpURLConnection
        print("connect?")
        connection.connectTimeout = 5000
        connection.connect()
        print("connected?")
        val list = ArrayList<String>()
        print(""+connection.responseCode)
        if(connection.responseCode == 200){
            var result = "{\"data\":"
            val input = connection.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(input))
            var line : String? = null
            while (bufferedReader.readLine().also { line = it } != null) result += line
            result+="}"
            JSONObject(result).also {
                it.getJSONArray("data").also { servers ->
                    for(i in 0 until servers.length()){
                        servers.getJSONObject(i).also { server->
                            if(server.getString("server_name") == "ReconDelta090"){
                                kotlin.runCatching {
                                    server.getJSONObject("status")?.getJSONArray("liveDevices")?.also { liveArray ->
                                        for(j in 0 until liveArray.length()){
                                            list.add(liveArray.getJSONObject(j).getString("name"))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        data.postValue(list)
        connection.disconnect()
    }
}