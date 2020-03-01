package tv.remo.android.controller.sdk.components

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.btelman.controlsdk.enums.ComponentStatus
import org.btelman.controlsdk.enums.ComponentType
import org.btelman.controlsdk.models.Component
import org.btelman.controlsdk.models.ComponentEventObject

/**
 * Broadcast the statuses of each component, and some service level events
 */
class StatusBroadcasterComponent : Component() {
    var localBroadcastManager : LocalBroadcastManager? = null
    override fun onInitializeComponent(applicationContext: Context, bundle: Bundle?) {
        super.onInitializeComponent(applicationContext, bundle)
        localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)
    }

    override fun disableInternal() {
        setServiceStatus(ComponentStatus.DISABLED)
    }

    override fun enableInternal() {
        setServiceStatus(ComponentStatus.STABLE)
    }

    fun setServiceStatus(status : ComponentStatus){
        val intent = Intent(ACTION_SERVICE_STATUS).apply{
            putExtra(STATUS_NAME, status)
        }
        localBroadcastManager?.sendBroadcast(intent)
    }

    override fun handleExternalMessage(message: ComponentEventObject): Boolean {
        if(message.what == STATUS_EVENT){

            //create an intent that will send all classes in the same action
            val intent = Intent(ACTION_COMPONENT_STATUS).apply {
                putExtra(CLASS_NAME, message.source.javaClass.name)
                putExtra(STATUS_NAME, message.data as ComponentStatus)
            }
            //create an intent that will only contain a single class of statuses
            val intentClassLevel = Intent(generateComponentStatusAction(message.source.javaClass)).apply {
                putExtra(STATUS_NAME, message.data as ComponentStatus)
            }
            //now send both of them
            localBroadcastManager?.sendBroadcast(intent)
            localBroadcastManager?.sendBroadcast(intentClassLevel)
        }
        return super.handleExternalMessage(message)
    }

    override fun getType(): ComponentType {
        return ComponentType.CUSTOM
    }

    companion object{
        fun <T> generateComponentStatusAction(clazz : Class<T>) : String{
//            if(clazz.getAnnotation(DriverComponent::class.java) != null){
//                return generateComponentStatusAction(CommunicationDriverComponent::class.java)
//            }
            return "${clazz.name}.${ACTION_COMPONENT_STATUS}"
        }
        const val ACTION_SERVICE_STATUS = "control.sdk.ACTION_SERVICE_STATUS"
        const val ACTION_COMPONENT_STATUS = "ACTION_COMPONENT_STATUS"
        const val CLASS_NAME = "component.class"
        const val STATUS_NAME = "component.status.name"
    }
}