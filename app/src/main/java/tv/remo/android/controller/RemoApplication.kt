package tv.remo.android.controller

import android.app.Application
import android.os.Build


/**
 * Created by Brendon on 7/28/2019.
 */
class RemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Instance = this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Camera2Util.GetCameraSizes(this)
        }
    }

    companion object{
        var Instance : RemoApplication? = null
    }
}