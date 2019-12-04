package tv.remo.android.controller.sdk.utils

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.btelman.controlsdk.enums.ComponentType
import org.btelman.controlsdk.interfaces.ComponentEventListener
import org.btelman.controlsdk.models.Component
import org.btelman.controlsdk.models.ComponentEventObject
import tv.remo.android.controller.sdk.components.RemoSocketComponent
import tv.remo.android.controller.sdk.models.api.Message

object ChatUtil {
    fun broadcastChatMessageRemovalRequest(context: Context, userId: String){
        //send the packet via Local Broadcast. Anywhere in this app can intercept this
        LocalBroadcastManager.getInstance(context)
            .sendBroadcast(
                Intent(RemoSocketComponent.REMO_CHAT_USER_REMOVED_BROADCAST)
                    .also { intent ->
                        intent.putExtra("userId", userId)
                    })
    }

    fun broadcastChatMessage(context: Context, msg: Message) {
        //send the packet via Local Broadcast. Anywhere in this app can intercept this
        LocalBroadcastManager.getInstance(context)
            .sendBroadcast(
                Intent(RemoSocketComponent.REMO_CHAT_MESSAGE_WITH_NAME_BROADCAST)
                    .also { intent ->
                        intent.putExtra("json", msg)
                    })
    }

    fun sendToSiteChat(eventDispatcher : ComponentEventListener?, message : String){
        RemoSocketComponent.RemoSocketChatPacket(message).also {
            eventDispatcher?.handleMessage(
                ComponentEventObject(
                    ComponentType.CUSTOM, Component.EVENT_MAIN,
                    it, this)
            )
        }
    }
}