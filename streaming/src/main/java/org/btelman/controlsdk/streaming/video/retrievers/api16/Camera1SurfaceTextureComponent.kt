package org.btelman.controlsdk.streaming.video.retrievers.api16

import android.graphics.ImageFormat
import android.graphics.Rect
import android.hardware.Camera
import org.btelman.controlsdk.enums.ComponentStatus
import org.btelman.controlsdk.streaming.models.ImageDataPacket
import org.btelman.controlsdk.streaming.video.retrievers.SurfaceTextureVideoRetriever

/**
 * Class that contains only the camera components for streaming
 *
 * To make this functional, pass in cameraId and a valid SurfaceHolder to a Core.Builder instance
 *
 * This will grab the camera password automatically from config file
 *
 * Does not support USB webcams
 */
@Suppress("DEPRECATION")
open class Camera1SurfaceTextureComponent : SurfaceTextureVideoRetriever(), Camera.PreviewCallback{
    private var supportedPreviewSizes: MutableList<Camera.Size>? = null
    private var r: Rect? = null
    private var camera : Camera? = null
    private var _widthV1 = 0
    private var _heightV1 = 0

    private var latestPackage : ImageDataPacket? = null

    override fun grabImageData(): ImageDataPacket? {
        return latestPackage
    }

    override fun onPreviewFrame(b: ByteArray?, camera: Camera?) {
        if (_widthV1 == 0 || _heightV1 == 0) {
            camera?.parameters?.let {
                val size = it.previewSize
                _widthV1 = size.width
                _heightV1 = size.height
                r = Rect(0, 0, _widthV1, _heightV1)
            }
        }
        notifyFrameUpdated()
        latestPackage = ImageDataPacket(b, ImageFormat.NV21, r)
    }

    override fun releaseCamera() {
        camera?.stopPreview()
        camera?.setPreviewCallback (null)
        camera?.setPreviewTexture(null)
        camera?.release()
        camera = null
    }

    override fun setupCamera(){
        val cameraId = streamInfo?.deviceInfo?.getCameraId() ?: 0
        camera ?: run {
            if(cameraId+1 > Camera.getNumberOfCameras()){
                val e = Exception("Attempted to open camera $cameraId. Only ${Camera.getNumberOfCameras()} cameras exist! 0 is first camera")
                log.e("Error opening camera", e)
                status = ComponentStatus.ERROR
                throw e
            }
            camera = Camera.open(cameraId)
            camera?.setDisplayOrientation(90)
        }
        camera?.let {
            it.parameters = updateCameraParams(it.parameters)
            it.setPreviewTexture(mStManager?.surfaceTexture)
            it.setPreviewCallback(this)
            it.startPreview()
        }
        status = ComponentStatus.STABLE
    }

    open fun updateCameraParams(parameters : Camera.Parameters) : Camera.Parameters{
        val cameraWidth = streamInfo?.width ?: 640
        val cameraHeight = streamInfo?.height ?: 480
        if(!validateSizeSupported(parameters, cameraWidth, cameraHeight)){
            val e = java.lang.Exception("Camera size " +
                    "${cameraWidth}x$cameraHeight not supported by this camera!")
            log.e("Failed to use width=$cameraWidth and height=$cameraHeight for camera!", e)
            status = ComponentStatus.ERROR
            throw e
        }
        parameters.setPreviewSize(cameraWidth, cameraHeight)
        parameters.setRecordingHint(true)
        return parameters
    }

    private fun validateSizeSupported(p: Camera.Parameters?, cameraWidth: Int, cameraHeight: Int) : Boolean{
        supportedPreviewSizes = p?.supportedPreviewSizes
        var supportsSize = false
        supportedPreviewSizes?.forEach { size ->
            if(size.height == cameraHeight && size.width == cameraWidth){
                supportsSize = true
            }
        }
        return supportsSize
    }
}
