package tv.remo.android.controller.sdk.components

import org.btelman.controlsdk.enums.ComponentType
import org.btelman.controlsdk.models.Component
import org.btelman.controlsdk.models.ComponentEventObject

/**
 * Watchdog component that will send a stop command
 * 200 milliseconds after a command goes through if there are no more commands.
 *
 * Not useful if non drive motors are still being controlled after timeout, so solution should really be handled on the bot
 */
class HardwareWatchdogComponent : Component() {
    override fun disableInternal() {
        //not needed
    }

    override fun enableInternal() {
        //not needed
    }

    override fun handleExternalMessage(message: ComponentEventObject): Boolean {
        if(message.type == ComponentType.HARDWARE){
            when(message.what){
                EVENT_MAIN -> {
                    resetTimeout()
                    resetTimeout()
                    resetTimeout()
                    resetTimeout()
                    resetTimeout()
                    resetTimeout()
                    resetTimeout()
                    resetTimeout()
                    resetTimeout()
                    resetTimeout()
                    resetTimeout()
                    resetTimeout()
                    resetTimeout()
                    resetTimeout()
                    resetTimeout()
                    resetTimeout()
                    resetTimeout()
                    resetTimeout()
                    resetTimeout()
                    resetTimeout()
                    resetTimeout()
                    resetTimeout()
                }
            }
        }
        return super.handleExternalMessage(message)
    }

    private val runnable = Runnable {
        eventDispatcher?.handleMessage(ComponentType.HARDWARE, EVENT_MAIN, "stop", this)
    }

    private fun resetTimeout() {
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, 200)
    }

    override fun getType(): ComponentType {
        return ComponentType.HARDWARE
    }

}