package tv.remo.android.controller.chat

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import tv.remo.android.controller.R
import tv.remo.android.controller.sdk.models.api.Message
import tv.remo.android.controller.sdk.utils.ValueUtil
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * A Chat adapter for Remo.TV chat that converts a List of TTSBaseComponent.TTSObject into UI
 */
class RemoChatAdapter(
        internal var mCtx: Context,
        internal var chatMessages: LinkedHashMap<String, Message>
) : RecyclerView.Adapter<RemoChatAdapter.RemoChatViewHolder>() {

    val colorTable = HashMap<String, Int>()

    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): RemoChatViewHolder {
        val view = LayoutInflater.from(mCtx).inflate(R.layout.remo_chat_item_layout, parent, false)
        return RemoChatViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: RemoChatViewHolder, position: Int) {
        val chatMessage = chatMessages.values.elementAt(position)
        holder.userField.text = chatMessage.sender
        holder.userField.setTextColor(getColorFromTable(chatMessage.senderId))
        holder.messageField.text = chatMessage.message
        if(chatMessage.type == "moderation")
            holder.messageField.setTextColor(Color.RED)
        //TODO icons if mod or owner? For now, just a simple UI
    }

    private fun getColorFromTable(senderId: String): Int {
        var color = colorTable[senderId]
        if(color == null){
            val random = Random()
            val min = .2f
            val max = 1f
            color = rgbCompat(
                randomFloatRange(min, max, random),
                randomFloatRange(min, max, random),
                randomFloatRange(min, max, random)
            )
            colorTable[senderId] = color
        }
        return color
    }

    fun randomFloatRange(min : Float, max : Float, random : Random = Random()) : Float{
        return ValueUtil.map(random.nextFloat(), 0f, 1f, min, max, 1.0f)
    }

    /**
     * We have to copy and paste Color.rgb to actually get this Int...
     */
    private fun rgbCompat(red : Float, green : Float, blue : Float) : Int{
        return -0x1000000 or
                ((red * 255.0f + 0.5f).toInt() shl 16) or
                ((green * 255.0f + 0.5f).toInt() shl 8) or
                (blue * 255.0f + 0.5f).toInt()
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    fun addMessage(obj : Message) {
        chatMessages[obj.id] = obj
        val index = chatMessages.keys.indexOf(obj.id)
        notifyItemInserted(index)
    }

    fun removeMessage(id : String){
        val index = chatMessages.keys.indexOf(id)
        if(index != -1){
            chatMessages.remove(id)
            notifyItemRemoved(index)
        }
    }

    fun removeUserWithUserId(userID: String) {
        val idsToRemove = ArrayList<String>()
        chatMessages.forEach {
            if(it.value.senderId == userID)
                idsToRemove.add(it.key)
        }
        idsToRemove.forEach {
            removeMessage(it)
        }
    }

    inner class RemoChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var userField = itemView.findViewById(R.id.remoChatUserField) as TextView
        internal var messageField = itemView.findViewById(R.id.remoChatMessageField) as TextView
    }
}