package tv.remo.android.controller
import tv.remo.android.controller.sdk.components.video.RemoVideoComponent

/**
 * Created by Brendon on 3/25/2020.
 */
class DemoVideoComponent : RemoVideoComponent() {
    override fun setLoopMode() {
        shouldAutoUpdateLoop = true
    }
}