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

/**
 * A Chat adapter for Remo.TV chat that converts a List of TTSBaseComponent.TTSObject into UI
 */
class RemoChatAdapter(
        internal var mCtx: Context,
        internal var chatMessages: LinkedHashMap<String, Message>
) : RecyclerView.Adapter<RemoChatAdapter.RemoChatViewHolder>() {
    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): RemoChatViewHolder {
        val view = LayoutInflater.from(mCtx).inflate(R.layout.remo_chat_item_layout, parent, false)
        return RemoChatViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: RemoChatViewHolder, position: Int) {
        val chatMessage = chatMessages.values.elementAt(position)
        holder.userField.text = chatMessage.sender
        holder.userField.setTextColor(Color.parseColor("#e9a811"))
        holder.messageField.text = chatMessage.message
        //TODO icons if mod or owner? For now, just a simple UI
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

    fun removeUser(userID: String) {
        val idsToRemove = ArrayList<String>()
        chatMessages.forEach {
            if(it.value.sender == userID)
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