package tv.remo.android.controller.sdk.components.video

import androidx.annotation.RequiresApi
import org.btelman.controlsdk.models.ComponentEventObject
import org.btelman.controlsdk.streaming.video.retrievers.BaseVideoRetriever
import org.btelman.controlsdk.streaming.video.retrievers.CameraCompatRetriever

/**
 * Override to use different camera classes
 */
class CameraCompatOverride : CameraCompatRetriever(){
    var impl : BaseVideoRetriever? = null
    @RequiresApi(21)
    override fun createCamera2(): BaseVideoRetriever? {
        return Camera2Override().also {
            impl = it
        }
    }

    override fun createCamera1(): BaseVideoRetriever? {
        return Camera1Override().also {
            impl = it
        }
    }

    override fun handleExternalMessage(message: ComponentEventObject): Boolean {
        return impl?.handleExternalMessage(message)?:super.handleExternalMessage(message)
    }
}