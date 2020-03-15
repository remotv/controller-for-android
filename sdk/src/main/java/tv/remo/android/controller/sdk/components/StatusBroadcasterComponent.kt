package tv.remo.android.controller.sdk.components

import android.content.Context
import android.content.Intent
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
    private val log = LogUtil("StatusBroadcasterComponent", ControlSDKService.loggerID)
    var localBroadcastManager : LocalBroadcastManager? = null
    override fun onInitializeComponent(applicationContext: Context, bundle: Bundle?) {
        localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)
    }

    override fun onRemoved() {
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
        log.d{
            "STATUS_EVENT $componentStatus from ${clazz.name}"
        }
        super.onComponentStatus(clazz, componentStatus)
        val intent = Intent(ACTION_COMPONENT_STATUS).apply {
            putExtra(CLASS_NAME, clazz.name)
            putExtra(STATUS_NAME, componentStatus)
        }
        //create an intent that will only contain a single class of statuses
        val intentClassLevel = Intent(generateComponentStatusAction(clazz)).apply {
            putExtra(STATUS_NAME, componentStatus)
        }
        //now send both of them
        localBroadcastManager?.sendBroadcast(intent)
        localBroadcastManager?.sendBroadcast(intentClassLevel)
    }

    companion object{
        fun <T> generateComponentStatusAction(clazz : Class<T>) : String{
            return "${clazz.name}.${ACTION_COMPONENT_STATUS}"
        }
        const val ACTION_SERVICE_STATUS = "control.sdk.ACTION_SERVICE_STATUS"
        const val ACTION_COMPONENT_STATUS = "ACTION_COMPONENT_STATUS"
        const val CLASS_NAME = "component.class"
        const val STATUS_NAME = "component.status.name"
    }
}