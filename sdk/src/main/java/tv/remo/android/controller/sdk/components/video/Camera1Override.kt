package tv.remo.android.controller.sdk.components.video

import android.hardware.Camera
import org.btelman.controlsdk.streaming.video.retrievers.api16.Camera1SurfaceTextureComponent

@Suppress("DEPRECATION")
class Camera1Override : Camera1SurfaceTextureComponent() {
    override fun updateCameraParams(parameters: Camera.Parameters): Camera.Parameters {
        return super.updateCameraParams(parameters)
    }
}