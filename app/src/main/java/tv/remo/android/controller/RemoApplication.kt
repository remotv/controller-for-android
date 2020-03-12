package tv.remo.android.controller

import android.util.Log
import androidx.multidex.MultiDexApplication
import org.btelman.controlsdk.services.ControlSDKService
import org.btelman.logutil.kotlin.LogLevel
import org.btelman.logutil.kotlin.LogUtil
import org.btelman.logutil.kotlin.LogUtilInstance

/**
 * Created by Brendon on 7/28/2019.
 */
class RemoApplication : MultiDexApplication() {
    private val log = LogUtil("RemoApplication", logID)

    override fun onCreate() {
        super.onCreate()
        LogUtilInstance(ControlSDKService.CONTROL_SERVICE, LogLevel.VERBOSE).also {
            Log.d("RemoApplication", "Setup ControlSDK logger")
            LogUtil.addCustomLogUtilInstance(ControlSDKService::class.java.name, it)
        }

        log.d{
            "Remo.TV ${BuildConfig.VERSION_NAME} onCreate..."
        }

        Instance = this
    }

    companion object{
        var Instance : RemoApplication? = null
        val logID = "Remo.TV".also {name->
            LogUtilInstance(ControlSDKService.CONTROL_SERVICE, LogLevel.VERBOSE).also {
                Log.d("RemoApplication", "Setup Remo.TV logger")
                LogUtil.addCustomLogUtilInstance(name, it)
            }
        }

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
    }
}