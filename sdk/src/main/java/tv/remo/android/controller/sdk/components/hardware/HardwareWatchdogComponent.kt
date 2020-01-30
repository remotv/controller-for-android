package tv.remo.android.controller.sdk.components.hardware

import android.content.Context
import android.os.Bundle
import org.btelman.controlsdk.enums.ComponentType
import org.btelman.controlsdk.models.Component
import org.btelman.controlsdk.models.ComponentEventObject
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.components.RemoCommandHandler
import tv.remo.android.controller.sdk.components.RemoSocketComponent
import tv.remo.android.controller.sdk.interfaces.RemoCommandSender
import tv.remo.android.controller.sdk.utils.ChatUtil

/**
 * Watchdog component that will send a stop command
 * 200 milliseconds after a command goes through if there are no more commands.
 *
 * Not useful if non drive motors are still being controlled after timeout, so solution should really be handled on the bot
 */
class HardwareWatchdogComponent : Component(), RemoCommandSender{
    private var sleepMode = false
    /**
     * time in seconds to wait to sleep the stream
     */
    private var streamSleepTime: Long = 5*60 //default if settings fails
    private var sleepEnabled = false

    override fun onInitializeComponent(applicationContext: Context, bundle: Bundle?) {
        super.onInitializeComponent(applicationContext, bundle)
        reloadSleepSettings(applicationContext)
    }

    private fun reloadSleepSettings(maybeContext: Context?) {
        val context = maybeContext?:return
        RemoSettingsUtil.with(context){ settings ->
            sleepEnabled = settings.streamSleepMode.getPref()
            streamSleepTime = settings.streamSleepTimeOut.getPref().toLong()
        }
    }

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
                    (message.data as? String)?.let {
                        if(it != "stop"){
                            maybeStartSleepTimer()
                            resetTimeout()
                        }
                    }
                }
            }
        }
        else if(message.source is RemoCommandSender){
            (message.data as? String)?.let {
                handleStringCommand(message.data as String)
            }
        }
        return super.handleExternalMessage(message)
    }

    private fun handleStringCommand(data: String) {
        context?:return
        when (data) {
            ".stream sleep" -> {
                if(!sleepMode){
                    sleepMode = true
                    killSleepTimer()
                }
            }
            ".stream wakeup" -> {
                if(sleepMode) {
                    maybeStartSleepTimer()
                    sleepMode = false
                }
            }
            ".stream reset" -> {
                maybeStartSleepTimer()
                sleepMode = false
            }
            else -> {
                if(data.startsWith(".stream sleeptime ")){
                    handleSleepCommand(data.replace(".stream sleeptime ", "").trim())
                }
            }
        }
    }

    private fun handleSleepCommand(data: String) {
        var maybeTime : Int? = null
        kotlin.runCatching {
            maybeTime = data.toInt()
        }
        maybeTime?.let { time ->
            val chatMessage : String = if(time > 0){
                saveSleepTime(time)
                "Setting sleeptime to $maybeTime seconds"
            } else{
                saveSleepTime(-1)
                "Setting sleeptime to disabled (time < 0)"
            }
            killSleepTimer()
            maybeStartSleepTimer()
            ChatUtil.sendToSiteChat(eventDispatcher, chatMessage)
        } ?: run{
            ChatUtil.sendToSiteChat(eventDispatcher, ".stream sleeptime {seconds}")
        }
    }

    private fun saveSleepTime(time: Int){
        val context = context?:return
        RemoSettingsUtil.with(context){ settings ->
            settings.streamSleepMode.savePref(time > 0)
            settings.streamSleepTimeOut.savePref(time)
            reloadSleepSettings(context)
        }
    }


    private fun killSleepTimer(){
        handler.removeCallbacks(sleepRobot)
    }

    /**
     * Reset the sleep counter when called.
     */
    private fun maybeStartSleepTimer() {
        if(sleepEnabled && !sleepMode){
            killSleepTimer()
            handler.postDelayed(sleepRobot, streamSleepTime*1000)
        }
    }

    private val sleepRobot = Runnable {
        eventDispatcher?.handleMessage(ComponentType.HARDWARE, EVENT_MAIN, ".stream sleep", this as RemoCommandSender)
        killSleepTimer()
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