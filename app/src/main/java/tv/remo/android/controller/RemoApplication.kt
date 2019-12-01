package tv.remo.android.controller

import androidx.multidex.MultiDexApplication

/**
 * Created by Brendon on 7/28/2019.
 */
class RemoApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        Instance = this
    }

    companion object{
        var Instance : RemoApplication? = null
    }
}