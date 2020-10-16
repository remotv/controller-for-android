package org.btelman.controlsdk.streaming.video.retrievers.api21

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import org.btelman.controlsdk.enums.ComponentStatus
import org.btelman.controlsdk.streaming.models.ImageDataPacket
import org.btelman.controlsdk.streaming.video.retrievers.BaseVideoRetriever


/**
 * Camera retrieval via Camera2 API and an offscreen SurfaceTexture for rendering the preview
 */
@Suppress("MemberVisibilityCanBePrivate")
@RequiresApi(21)
open class Camera2Component : BaseVideoRetriever(), ImageReader.OnImageAvailableListener {

    private var data: ByteArray? = null
    protected var width = 0
    protected var height = 0

    var reader : ImageReader? = null

    protected var mPreviewBuilder: CaptureRequest.Builder? = null
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    protected var mBackgroundThread: HandlerThread? = null

    /**
     * A [Handler] for running tasks in the background.
     */
    protected var mBackgroundHandler: Handler? = null

    /**
     * [CameraDevice.StateCallback] is called when [CameraDevice] changes its status.
     */
    protected val mStateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(@NonNull cameraDevice: CameraDevice) {
            mCameraDevice = cameraDevice
            status = ComponentStatus.STABLE
            startPreview()
        }

        override fun onDisconnected(@NonNull cameraDevice: CameraDevice) {
            closePreviewSession()
            cameraDevice.close()
            status = ComponentStatus.DISABLED
            mCameraDevice = null
        }

        override fun onError(@NonNull cameraDevice: CameraDevice, error: Int) {
            closePreviewSession()
            cameraDevice.close()
            log.e("onError $error")
            status = ComponentStatus.ERROR
            mCameraDevice = null
        }

    }

    override fun enableInternal() {
        super.enableInternal()
        setupCamera()
    }

    override fun disableInternal() {
        super.disableInternal()
        releaseCamera()
    }

    @SuppressLint("MissingPermission") //Already handled. No way to call this
    protected open fun setupCamera() {
        startBackgroundThread()
        width = streamInfo?.width ?: 640
        height = streamInfo?.height ?: 640
        reader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 2)
        reader?.setOnImageAvailableListener(this, mBackgroundHandler)
        val manager = context!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val list = manager.cameraIdList
            val requestedId = streamInfo?.deviceInfo?.getCameraId()?:0
            if(requestedId+1 > list.size){
                val e = java.lang.Exception("Attempted to open camera $requestedId. Only ${list.size} cameras exist! 0 is first camera")
                log.e("error starting camera", e)
                throw e
            }
            manager.openCamera(list[requestedId], mStateCallback, null)
        } catch (e: CameraAccessException) {
            status = ComponentStatus.ERROR
            log.e("error starting camera", e)
            //TODO throw error and kill service?
        } catch (e: NullPointerException) {
            status = ComponentStatus.ERROR
            log.e("error starting camera", e)
            //TODO throw error and kill service?
        } catch (e: InterruptedException) {
            status = ComponentStatus.ERROR
            log.e("error starting camera", e)
            //TODO throw error and kill service?
            throw RuntimeException("Interrupted while trying to lock camera opening.")
        }
    }

    protected open fun releaseCamera() {
        stopBackgroundThread()
        reader?.close()
        closePreviewSession()
        mCameraDevice?.close()
    }

    private var latestPackage : ImageDataPacket? = null

    override fun grabImageData(): ImageDataPacket? {
        return latestPackage
    }

    /**
     * A reference to the opened [android.hardware.camera2.CameraDevice].
     */
    protected var mCameraDevice: CameraDevice? = null

    /**
     * A reference to the current [android.hardware.camera2.CameraCaptureSession] for
     * preview.
     */
    protected var mPreviewSession: CameraCaptureSession? = null

    override fun onImageAvailable(reader: ImageReader?) {
        var image: Image? = null
        try {
            image = reader?.acquireLatestImage()
            image?.let {
                latestPackage = ImageDataPacket(convertYuv420888ToYuv(image), ImageFormat.YUV_420_888)
                notifyFrameUpdated()
            }
        } finally {
            image?.close()
        }
    }

    private fun convertYuv420888ToYuv(image: Image): ByteArray {
        val yPlane = image.planes[0]
        val ySize = yPlane.buffer.remaining()

        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        // be aware that this size does not include the padding at the end, if there is any
        // (e.g. if pixel stride is 2 the size is ySize / 2 - 1)
        val uSize = uPlane.buffer.remaining()
        val vSize = vPlane.buffer.remaining()

        if(data?.size != ySize + ySize / 2)
            data = ByteArray(ySize + ySize / 2)

        yPlane.buffer.get(data, 0, ySize)

        val ub = uPlane.buffer
        val vb = vPlane.buffer

        val uvPixelStride = uPlane.pixelStride //stride guaranteed to be the same for u and v planes
        if (uvPixelStride == 1) {
            uPlane.buffer.get(data, ySize, uSize)
            vPlane.buffer.get(data, ySize + uSize, vSize)
        }
        else{
            // if pixel stride is 2 there is padding between each pixel
            // converting it to NV21 by filling the gaps of the v plane with the u values
            vb.get(data, ySize, vSize)
            var i = 0
            while (i < uSize) {
                data!![ySize + i + 1] = ub.get(i)
                i += 2
            }
        }
        return data!!
    }

    /**
     * Start the camera preview.
     */
    protected open fun startPreview() {
        if (null == mCameraDevice) {
            status = ComponentStatus.ERROR
            log.e("camera null!")
            return
        }
        try {
            closePreviewSession()
            mPreviewBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreviewBuilder!!.addTarget(reader?.surface!!)

            mCameraDevice!!.createCaptureSession(listOf(/*previewSurface, */reader?.surface),
                    object : CameraCaptureSession.StateCallback() {

                        override fun onConfigured(@NonNull session: CameraCaptureSession) {
                            mPreviewSession = session
                            updatePreview()
                        }

                        override fun onConfigureFailed(@NonNull session: CameraCaptureSession) {
                            log.e("camera ConfigureFailed")
                            status = ComponentStatus.ERROR
                        }
                    }, mBackgroundHandler)
        } catch (e: Exception) {
            log.e("error starting preview", e)
        }
    }

    /**
     * Update the camera preview. [.startPreview] needs to be called in advance.
     */
    protected open fun updatePreview() {
        if (null == mCameraDevice) {
            return
        }
        try {
            mPreviewBuilder?.let { setUpCaptureRequestBuilder(it) }
            val thread = HandlerThread("CameraPreview")
            thread.start()
            mPreviewSession!!.setRepeatingRequest(mPreviewBuilder!!.build()
                    ,null
                    , mBackgroundHandler)
        } catch (e: Exception) {
            log.e("error updating preview", e)
        }
    }

    protected open fun setUpCaptureRequestBuilder(builder: CaptureRequest.Builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
    }

    protected open fun closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession?.close()
            mPreviewSession = null
        }
    }

    /**
     * Starts a background thread and its [Handler].
     */
    protected open fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread?.start()
        mBackgroundHandler = Handler(mBackgroundThread?.looper)
    }

    /**
     * Stops the background thread and its [Handler].
     */
    protected open fun stopBackgroundThread() {
        mBackgroundThread?.quitSafely()
        try {
            mBackgroundThread?.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            log.e("error stopping background thread", e)
        }
    }

    companion object{
        //checks for if this class can be run on this device
        fun isSupported() : Boolean{
            return Build.VERSION.SDK_INT >= 21
        }
    }
}