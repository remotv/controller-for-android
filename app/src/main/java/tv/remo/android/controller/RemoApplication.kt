package tv.remo.android.controller

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.multidex.MultiDexApplication
import org.btelman.android.shellutil.Executor
import org.btelman.controlsdk.services.ControlSDKService
import org.btelman.logutil.kotlin.LogLevel
import org.btelman.logutil.kotlin.LogUtil
import org.btelman.logutil.kotlin.LogUtilInstance
import tv.remo.android.controller.sdk.RemoSettingsUtil
import kotlin.system.exitProcess

/**
 * Created by Brendon on 7/28/2019.
 */
class RemoApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        setupLogging(this)

        val log = LogUtil("RemoApplication", logID)

        log.d{
            "Remo.TV ${BuildConfig.VERSION_NAME} onCreate..."
        }

        Instance = this
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

        fun setupLogging(context: Context) {
            RemoSettingsUtil.with(context){
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

        fun restart(context: Context) {
            context.apply {
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                exitProcess(0)
            }
        }
    }
}