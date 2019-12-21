package org.btelman.controlsdk.streaming.video.retrievers.api21

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.Surface
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import org.btelman.controlsdk.streaming.models.ImageDataPacket
import org.btelman.controlsdk.streaming.models.StreamInfo
import org.btelman.controlsdk.streaming.video.retrievers.SurfaceTextureVideoRetriever


/**
 * Camera retrieval via Camera2 API and an offscreen SurfaceTexture for rendering the preview
 */
@RequiresApi(21)
class MediaRecorderCameraRetriever : SurfaceTextureVideoRetriever(){

    private var data: ByteArray? = null
    private var width = 0
    private var height = 0

    private var mPreviewBuilder: CaptureRequest.Builder? = null
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private var mBackgroundThread: HandlerThread? = null

    /**
     * A [Handler] for running tasks in the background.
     */
    private var mBackgroundHandler: Handler? = null

    /**
     * [CameraDevice.StateCallback] is called when [CameraDevice] changes its status.
     */
    private val mStateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(@NonNull cameraDevice: CameraDevice) {
            mCameraDevice = cameraDevice
            startPreview()
        }

        override fun onDisconnected(@NonNull cameraDevice: CameraDevice) {
            cameraDevice.close()
            mCameraDevice = null
        }

        override fun onError(@NonNull cameraDevice: CameraDevice, error: Int) {
            cameraDevice.close()
            mCameraDevice = null
        }

    }

    @SuppressLint("MissingPermission") //Already handled. No way to call this
    override fun setupCamera(streamInfo : StreamInfo?) {
        startBackgroundThread()
        width = streamInfo?.width ?: 640
        height = streamInfo?.height ?: 640
        val manager = context!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val list = manager.cameraIdList
            val requestedId = streamInfo?.deviceInfo?.getCameraId()?:0
            if(requestedId+1 > list.size){
                throw java.lang.Exception("Attempted to open camera $requestedId. Only ${list.size} cameras exist! 0 is first camera")
            }
            manager.openCamera(list[requestedId], mStateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            //TODO throw error and kill service?
        } catch (e: NullPointerException) {
            e.printStackTrace()
            //TODO throw error and kill service?
        } catch (e: InterruptedException) {
            e.printStackTrace()
            //TODO throw error and kill service?
            throw RuntimeException("Interrupted while trying to lock camera opening.")
        }
    }

    override fun releaseCamera() {
        stopBackgroundThread()
    }

    private var mMediaRecorder: MediaRecorder? = null

    override fun enable(context: Context, streamInfo: StreamInfo) {
        try {
            Companion.fdPair = ParcelFileDescriptor.createPipe()
        } catch (e: java.io.IOException) {
            e.printStackTrace()
        }
        super.enable(context, streamInfo)
    }

    override fun disable() {
        super.disable()
//        try {
//            val readFD = Companion.fdPair[0]
//            val writeFD = Companion.fdPair[1]
//            readFD?.close()
//            writeFD?.close()
//        } catch (e: Exception) {
//        }
    }

    private fun setupStreaming(){
        val writeFD = Companion.fdPair[1]
        if(mMediaRecorder == null) mMediaRecorder = MediaRecorder()
        val recorder = mMediaRecorder ?: return
        recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        recorder.setOutputFormat(8) //MPEG_TS
        recorder.setOutputFile(writeFD!!.fileDescriptor)
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        recorder.setVideoSize(640, 480)
        recorder.setVideoEncodingBitRate(500000)
        //recorder.setAudioEncodingBitRate(44100)
        recorder.setPreviewDisplay(Surface(mStManager?.surfaceTexture))
        recorder.setVideoFrameRate(30)
        recorder.setMaxDuration(-1)
        recorder.prepare()
    }

    private var latestPackage : ImageDataPacket? = null

    override fun grabImageData(): ImageDataPacket? {
        return latestPackage
    }

    /**
     * A reference to the opened [android.hardware.camera2.CameraDevice].
     */
    private var mCameraDevice: CameraDevice? = null

    /**
     * A reference to the current [android.hardware.camera2.CameraCaptureSession] for
     * preview.
     */
    private var mPreviewSession: CameraCaptureSession? = null

    /**
     * Start the camera preview.
     */
    private fun startPreview() {
        if (null == mCameraDevice) {
            return
        }
        try {
            closePreviewSession()
            setupStreaming()
            //texture.setDefaultBufferSize(height, width)
            mPreviewBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
            mPreviewBuilder!!.addTarget(mMediaRecorder?.surface!!)

            mCameraDevice!!.createCaptureSession(listOf(mMediaRecorder?.surface),
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(@NonNull session: CameraCaptureSession) {
                        mPreviewSession = session
                        updatePreview()
                        mMediaRecorder?.start()
                    }

                    override fun onConfigureFailed(@NonNull session: CameraCaptureSession) {
                        Log.d("aaaa", "awdwadad")
                    }
                }, mBackgroundHandler)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * Update the camera preview. [.startPreview] needs to be called in advance.
     */
    private fun updatePreview() {
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
            e.printStackTrace()
        }

    }

    private fun setUpCaptureRequestBuilder(builder: CaptureRequest.Builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
    }

    private fun closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession?.close()
            mPreviewSession = null
        }
        mMediaRecorder?.release()
        mMediaRecorder = null
    }

    /**
     * Starts a background thread and its [Handler].
     */
    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread?.start()
        mBackgroundHandler = Handler(mBackgroundThread?.looper)
    }

    /**
     * Stops the background thread and its [Handler].
     */
    private fun stopBackgroundThread() {
        mBackgroundThread?.quitSafely()
        try {
            mBackgroundThread?.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    companion object{
        //checks for if this class can be run on this device
        fun isSupported() : Boolean{
            return Build.VERSION.SDK_INT >= 21
        }

        //make a pipe containing a read and a write parcelfd
        var fdPair = arrayOfNulls<ParcelFileDescriptor>(0)
    }
}