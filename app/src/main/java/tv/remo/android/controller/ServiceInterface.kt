package tv.remo.android.controller

import android.content.Context
import org.btelman.controlsdk.enums.Operation
import org.btelman.controlsdk.models.ComponentHolder
import org.btelman.controlsdk.services.ControlSDKServiceConnection
import tv.remo.android.controller.activities.ExternalSocketControl
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.components.StatusBroadcasterComponent
import tv.remo.android.controller.sdk.utils.ComponentBuilderUtil

/**
 * Interface for Activity to communicate with service
 */
open class ServiceInterface(
    private val context: Context,
    val onServiceBind : (Operation)->Unit,
    val onServiceStateChange : (Operation)->Unit
){
    /**
     * Listeners or controllers that are able to persist beyond the activity lifecycle
     * IListener or IController
     */
    protected val persistentListenerControllerList = ArrayList<ComponentHolder<*>>()

    /**
     * Listeners or controllers that should be removed when the activity unbinds
     * IListener or IController
     */
    protected val listenerControllerList = ArrayList<ComponentHolder<*>>()

    /**
     * Generic ControlSDK ComponentHolders
     * IComponent
     */
    protected val arrayList = ArrayList<ComponentHolder<*>>()
    protected var controlSDKServiceApi =
        ControlSDKServiceConnection.getNewInstance(context)

    var handleServiceBoundEvent = fun(connected: Operation){
        onServiceBind(connected)
        handleListenerAddOrRemove(connected)
    }

    open fun setup(createComponents : Boolean = true) {
        controlSDKServiceApi.getServiceStateObserver().observeForever(onServiceStateChange)
        controlSDKServiceApi.getServiceBoundObserver().observeForever(handleServiceBoundEvent)
        controlSDKServiceApi.connectToService()
        if(createComponents){
            createComponentHolders()
        }
    }

    open fun destroy(){
        controlSDKServiceApi.disconnectFromService()
        controlSDKServiceApi.getServiceStateObserver().removeObserver(onServiceStateChange)
        controlSDKServiceApi.getServiceBoundObserver().removeObserver(handleServiceBoundEvent)
    }

    protected open fun createComponentHolders() {
        RemoSettingsUtil.with(context){ settings ->
            arrayList.add(ComponentBuilderUtil.createSocketComponent(settings))
            arrayList.addAll(ComponentBuilderUtil.createTTSComponents(settings))
            arrayList.addAll(ComponentBuilderUtil.createStreamingComponents(context, settings))
            arrayList.addAll(ComponentBuilderUtil.createHardwareComponents(settings))
            persistentListenerControllerList.add(ComponentHolder(StatusBroadcasterComponent::class.java, null, async = false))
            persistentListenerControllerList.add(ComponentHolder(ExternalSocketControl::class.java,null, async = false))
        }
    }

    fun changeStreamState(desiredState : Operation) {
        if(controlSDKServiceApi.getServiceBoundObserver().value != Operation.OK)
            throw IllegalStateException() //not connected to socket
        if(controlSDKServiceApi.getServiceStateObserver().value == desiredState)
            return //already active
        when(desiredState){
            Operation.OK -> {
                arrayList.forEach {
                    controlSDKServiceApi.attachToLifecycle(it)
                }
                controlSDKServiceApi.enable()
            }
            Operation.LOADING -> {} //do nothing
            Operation.NOT_OK -> {
                //disable the service
                controlSDKServiceApi.disable()
                // remove all components that happen to be left over. We may not know what got added
                // if the activity was lost due to the Android system
                // Listeners and controllers will still stay, and will not be overridden by the same name
                controlSDKServiceApi.reset()
            }
        }
    }

    private fun handleListenerAddOrRemove(connected : Operation) {
        if(connected == Operation.OK){
            listenerControllerList.forEach {
                controlSDKServiceApi.addListenerOrController(it)
            }
            persistentListenerControllerList.forEach {
                controlSDKServiceApi.addListenerOrController(it)
            }
        }
        else if(connected == Operation.NOT_OK){
            listenerControllerList.forEach {
                controlSDKServiceApi.removeListenerOrController(it)
            }
            //persistent list gets to stay when service is not bound
        }
    }
}