package tv.remo.android.controller.sdk.components.video

import androidx.annotation.RequiresApi
import org.btelman.controlsdk.streaming.video.retrievers.BaseVideoRetriever
import org.btelman.controlsdk.streaming.video.retrievers.CameraCompatRetriever

/**
 * Override to use different camera classes
 */
class CameraCompatOverride : CameraCompatRetriever(){
    @RequiresApi(21)
    override fun createCamera2(): BaseVideoRetriever? {
        return Camera2Override()
    }

    override fun createCamera1(): BaseVideoRetriever? {
        return Camera1Override()
    }
}