package tv.remo.android.controller.sdk.components.video

import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.util.Range
import androidx.annotation.RequiresApi
import org.btelman.controlsdk.streaming.video.retrievers.api21.Camera2Component
import tv.remo.android.controller.sdk.RemoSettingsUtil

/**
 * Copied from org.btelman.controlsdk.streaming.video.retrievers.api21.Camera2SurfaceTextureComponent
 */
@RequiresApi(21)
class Camera2Override : Camera2Component(), ImageReader.OnImageAvailableListener {

    private var focusMode = "video"

    override fun enableInternal() {
        super.enableInternal()
        focusMode = RemoSettingsUtil.with(context!!).cameraFocus.getPref()
    }

    override fun setUpCaptureRequestBuilder(builder: CaptureRequest.Builder) {
        builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(15, 30))
        val focusMode = when(focusMode){
            "video" -> {
                CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO
            }
            "picture" -> {
                CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            }
            "auto" -> {
                null
            }
            "off" -> {
                CameraMetadata.CONTROL_MODE_OFF
            }
            else -> {
                CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO
            }
        }
        focusMode?.let {
            builder.set(CaptureRequest.CONTROL_AF_MODE, focusMode)
            if(it == CameraMetadata.CONTROL_MODE_OFF)
                builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, 10f)
        } ?: run{
            builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        }
    }
}