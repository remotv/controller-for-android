package tv.remo.android.controller.sdk.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class LocalBroadcastReceiverExtended(
        val context: Context,
        val filter : IntentFilter,
        val action: (context: Context?, intent: Intent?) -> Unit) : BroadcastReceiver() {

        fun register(){
            LocalBroadcastManager.getInstance(context).registerReceiver(this,filter)
        }

        fun unregister(){
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this)
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            action(context, intent)
        }
    }