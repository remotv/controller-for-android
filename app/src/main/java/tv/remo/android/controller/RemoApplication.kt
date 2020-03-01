package tv.remo.android.controller

import android.app.Application

/**
 * Created by Brendon on 7/28/2019.
 */
class RemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
//        LogUtilInstance(ControlSDKService.CONTROL_SERVICE, LogLevel.VERBOSE).also {
//            Log.d("RemoApplication", "Setup ControlSDK logger")
//            LogUtil.addCustomLogUtilInstance(ControlSDKService::class.java.name, it)
//        }
        Instance = this
    }

    companion object{
        var Instance : RemoApplication? = null
    }
}