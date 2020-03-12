package tv.remo.android.controller.wear

import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log
import com.google.android.gms.wearable.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : WearableActivity(), CapabilityClient.OnCapabilityChangedListener,
    DataClient.OnDataChangedListener, MessageClient.OnMessageReceivedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        text.text = "---"
        Wearable.getDataClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
    }

    fun print(value : String){
        Log.d("Main", value)
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {

    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        print("event");
        dataEvents.forEach { event->
            if(event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/robots"){
                val item = DataMapItem.fromDataItem(event.dataItem).dataMap
                text.text = item.getStringArrayList("robots")?.firstOrNull()?:"aaaa"
//                item.getStringArrayList("robots")?.joinToString { "\n" }?.let {
//                    text.text = it
//                }
            }
        }
    }

    override fun onMessageReceived(p0: MessageEvent) {

    }
}
