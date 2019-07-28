package tv.remo.android.controller

import android.app.Application

/**
 * Created by Brendon on 7/28/2019.
 */
class RemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Instance = this
    }

    companion object{
        var Instance : RemoApplication? = null
    }
}