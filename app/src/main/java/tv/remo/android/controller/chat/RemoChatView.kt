package tv.remo.android.controller.chat

import android.content.Context
import android.content.IntentFilter
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    private val onChatMessageRemovedReceiver = LocalBroadcastReceiverExtended(
            context,
            IntentFilter(RemoSocketComponent.REMO_CHAT_MESSAGE_REMOVED_BROADCAST)){ _, intent ->
        intent?.extras?.getString("message_id", null)?.let {
            remoAdapter?.removeMessage(it)
        }
    }

    private val onUserRemovedReceiver = LocalBroadcastReceiverExtended(context,
            IntentFilter(RemoSocketComponent.REMO_CHAT_USER_REMOVED_BROADCAST)){ _, intent ->
        intent?.extras?.getString("username", null)?.let {
            remoAdapter?.removeUser(it)
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
        onChatMessageRemovedReceiver.register()
        onUserRemovedReceiver.register()
        onChatMessageReceiver.register()
    }
}