package tv.remo.android.controller.sdk.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.btelman.controlsdk.enums.ComponentStatus
import org.btelman.controlsdk.enums.ServiceStatus
import org.btelman.controlsdk.interfaces.IListener
import org.btelman.controlsdk.services.ControlSDKService
import org.btelman.logutil.kotlin.LogUtil

/**
 * Broadcast the statuses of each component, and some service level events
 */
class StatusBroadcasterComponent : IListener {
    private val cachedStatus = HashMap<String, ComponentStatus>()
    var localBroadcastManager : LocalBroadcastManager? = null
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action == ACTION_UPDATE){ //something wants us to send all events
                log.d{
                    "status update request received by BroadcastReceiver"
                }
                //loop through cached statuses and send them
                val statusValues = cachedStatus.entries //make sure to store it locally to prevent race conditions...
                statusValues.forEach{
                    handleStatusBroadcast(it.key, it.value)
                }
            }
        }
    }

    override fun onInitializeComponent(applicationContext: Context, bundle: Bundle?) {
        localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)
        localBroadcastManager?.registerReceiver(receiver, IntentFilter(ACTION_UPDATE))
    }

    override fun onRemoved() {
        cachedStatus.clear()
        localBroadcastManager?.unregisterReceiver(receiver)
        localBroadcastManager = null
    }

    override fun onServiceStateChange(status: ServiceStatus) {
        super.onServiceStateChange(status)
        setServiceStatus(status)
    }

    fun setServiceStatus(status : ServiceStatus){
        val intent = Intent(ACTION_SERVICE_STATUS).apply{
            putExtra(STATUS_NAME, status)
        }
        localBroadcastManager?.sendBroadcast(intent)
    }

    override fun onComponentStatus(clazz: Class<*>, componentStatus: ComponentStatus) {
        super.onComponentStatus(clazz, componentStatus)
        cachedStatus[clazz.name] = componentStatus //cache it for use if we get a broadcast to update immediately
        handleStatusBroadcast(clazz.name, componentStatus)
    }

    private fun handleStatusBroadcast(name: String, componentStatus: ComponentStatus) {
        log.d{
            "STATUS_EVENT $componentStatus from $name sending"
        }
        val intent = Intent(ACTION_COMPONENT_STATUS).apply {
            putExtra(CLASS_NAME, name)
            putExtra(STATUS_NAME, componentStatus)
        }
        //create an intent that will only contain a single class of statuses
        val intentClassLevel = Intent(generateComponentStatusAction(name)).apply {
            putExtra(STATUS_NAME, componentStatus)
        }
        //now send both of them
        localBroadcastManager?.sendBroadcast(intent)
        localBroadcastManager?.sendBroadcast(intentClassLevel)
    }

    companion object{
        private val log = LogUtil("StatusBroadcasterComponent", ControlSDKService.loggerID)
        fun generateComponentStatusAction(clazzName : String) : String{
            return "${clazzName}.${ACTION_COMPONENT_STATUS}"
        }

        /**
         * Send an update broadcast to trigger this class to send status events again.
         * Useful if the activity is destroyed, but not the service
         */
        fun sendUpdateBroadcast(context: Context){
            log.d{
                "Request status update"
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(ACTION_UPDATE))
        }
        const val ACTION_SERVICE_STATUS = "control.sdk.ACTION_SERVICE_STATUS"
        const val ACTION_COMPONENT_STATUS = "ACTION_COMPONENT_STATUS"
        const val ACTION_UPDATE = "tv.remo.android.controller.sdk.components.StatusBroadcasterComponent.Refresh"
        const val CLASS_NAME = "component.class"
        const val STATUS_NAME = "component.status.name"
    }
}