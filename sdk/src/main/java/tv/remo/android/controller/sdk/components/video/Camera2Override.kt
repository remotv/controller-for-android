package tv.remo.android.controller.sdk.components.video

import android.content.pm.PackageManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.util.Range
import androidx.annotation.RequiresApi
import org.btelman.controlsdk.models.ComponentEventObject
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.interfaces.RemoCommandSender


/**
 * Copied from org.btelman.controlsdk.streaming.video.retrievers.api21.Camera2SurfaceTextureComponent
 */
@RequiresApi(21)
class Camera2Override : Camera2Component(), ImageReader.OnImageAvailableListener {

    private var light = false
    private var focusMode = "video"

    override fun enableInternal() {
        super.enableInternal()
        focusMode = RemoSettingsUtil.with(context!!).cameraFocus.getPref()
    }

    override fun setUpCaptureRequestBuilder(builder: CaptureRequest.Builder) {
        builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(15, 30))
        builder.set(CaptureRequest.FLASH_MODE, if(light) CameraMetadata.FLASH_MODE_TORCH else CameraMetadata.FLASH_MODE_OFF)
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

    override fun handleExternalMessage(message: ComponentEventObject): Boolean {
        if(message.source is RemoCommandSender){
            (message.data as? String)?.let {
                handleCameraCommand(it)
            }
        }
        return super.handleExternalMessage(message)
    }

    private fun handleCameraCommand(data: String) {
        if(data.startsWith(".light")){ //if light command
            //we need to make sure we can run it without crashing the app
            val isFlashAvailable = context!!.packageManager
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
            if(!isFlashAvailable)
                return //nope. Ignore the command
        }

        when(data){
            ".light on" -> {
                light = true
            }
            ".light off" -> {
                light = false
            }
        }
    }
}