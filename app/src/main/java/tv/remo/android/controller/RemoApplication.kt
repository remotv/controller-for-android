package tv.remo.android.controller

import android.util.Log
import androidx.multidex.MultiDexApplication
import org.btelman.android.shellutil.Executor
import org.btelman.controlsdk.services.ControlSDKService
import org.btelman.logutil.kotlin.LogLevel
import org.btelman.logutil.kotlin.LogUtil
import org.btelman.logutil.kotlin.LogUtilInstance
import tv.remo.android.controller.sdk.RemoSettingsUtil

/**
 * Created by Brendon on 7/28/2019.
 */
class RemoApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        setupLogging()

        val log = LogUtil("RemoApplication", logID)

        log.d{
            "Remo.TV ${BuildConfig.VERSION_NAME} onCreate..."
        }

        Instance = this
    }

    private fun setupLogging() {
        RemoSettingsUtil.with(this){
            val logLevelStr = it.logLevel.getPref()
            logLevel = LogLevel.valueOf(logLevelStr)
        }
        LogUtilInstance(ControlSDKService.CONTROL_SERVICE, logLevel).also {
            Log.d("RemoApplication", "Setup ControlSDK logger...")
            LogUtil.addCustomLogUtilInstance(ControlSDKService.loggerID, it)
        }
        LogUtilInstance(ControlSDKService.CONTROL_SERVICE, logLevel).also {
            Log.d("RemoApplication", "Setup Remo.TV logger...")
            LogUtil.addCustomLogUtilInstance(logID, it)
        }
        LogUtilInstance(Executor::class.java.simpleName, logLevel).also {
            Log.d("RemoApplication", "Setup ShellUtil logger...")
            LogUtil.addCustomLogUtilInstance(Executor::class.java.simpleName, it)
            Executor.logInstance = it
        }
    }

    companion object{
        var Instance : RemoApplication? = null
        val logID = "Remo.TV"

        fun getLogger(tag : String) : LogUtil{
            return LogUtil(tag, logID)
        }

        fun getLogger(obj : Any, extra : String? = null) : LogUtil{
            var log = obj.javaClass.simpleName
            extra?.let{
                log += " : $extra"
            }
            return getLogger(log)
        }

        var logLevel : LogLevel = LogLevel.ERROR
            private set
    }
}