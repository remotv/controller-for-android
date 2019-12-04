package tv.remo.android.controller.chat

import android.content.Context
import android.content.IntentFilter
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.components.RemoSocketComponent
import tv.remo.android.controller.sdk.models.api.Message
import tv.remo.android.controller.sdk.utils.LocalBroadcastReceiverExtended

/**
 * View that displays chat messages from remo.tv
 */
class RemoChatView : RecyclerView{
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)

    private val remoAdapter: RemoChatAdapter?
        get() {return adapter as? RemoChatAdapter
        }

    private val onChatMessageReceiver= LocalBroadcastReceiverExtended(context,
            IntentFilter(RemoSocketComponent.REMO_CHAT_MESSAGE_WITH_NAME_BROADCAST)){ _, intent ->
        intent?.extras?.getSerializable("json")?.let {
            remoAdapter?.addMessage(it as Message)
        }
    }

    private val onUserRemovedReceiver = LocalBroadcastReceiverExtended(context,
            IntentFilter(RemoSocketComponent.REMO_CHAT_USER_REMOVED_BROADCAST)){ _, intent ->
        intent?.extras?.getString("userId", null)?.let {
            remoAdapter?.removeUserWithUserId(it)
        }
    }

    init {
        adapter = RemoChatAdapter(context, LinkedHashMap())
        layoutManager = LinearLayoutManager(context)
        val recyclerView = this
        adapter!!.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                (layoutManager as LinearLayoutManager).smoothScrollToPosition(recyclerView, null, adapter!!.itemCount)
            }
        })
        RemoSettingsUtil.with(context){
            if(it.chatDisplayEnabled.getPref()){
                onUserRemovedReceiver.register()
                onChatMessageReceiver.register()
            }
        }
    }
}