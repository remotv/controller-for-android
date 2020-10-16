package org.btelman.controlsdk.streaming.video.retrievers

import android.annotation.TargetApi
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import kotlinx.coroutines.runBlocking
import org.btelman.controlsdk.enums.ComponentStatus
import org.btelman.controlsdk.streaming.models.ImageDataPacket
import org.btelman.controlsdk.streaming.video.retrievers.api16.Camera1SurfaceTextureComponent
import org.btelman.controlsdk.streaming.video.retrievers.api21.Camera2Component

/**
 * Handle compatibility between camera1 and camera2 usage, since some api21 devices are
 * not compatible, which makes frame grabbing really slow. Usage of Camera1 or Camera2 classes are
 * still supported, but may not work on every device
 * ex. Samsung Galaxy S4
 */
open class CameraCompatRetriever : BaseVideoRetriever(){
    private var retriever : BaseVideoRetriever? = null
    private var bundle : Bundle? = null

    override fun onInitializeComponent(applicationContext: Context, bundle: Bundle?) {
        super.onInitializeComponent(applicationContext, bundle)
        this.bundle = bundle
    }

    override fun grabImageData(): ImageDataPacket? {
        return retriever?.grabImageData()
    }

    override fun enableInternal() {
        super.enableInternal()
        instantiateReceiver()
        runBlocking {
            retriever?.setEventListener(eventDispatcher)
            streamInfo?.let {
                retriever?.updateStreamInfo(it)
            }?:log.e("streamInfo is null!")
            retriever?.enable()?.await()
        }
        status = ComponentStatus.STABLE
    }

    private fun instantiateReceiver() {
        val cameraInfo = streamInfo!!.deviceInfo
        val cameraId = cameraInfo.getCameraId()
        retriever = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
            && validateCamera2Support(context!!, cameraId)){
            log.d("Using Camera2 API")
            createCamera2()
        } else{
            log.d("Using Camera1 API. Device API too low or LIMITED capabilities")
            createCamera1()
        }
        retriever?.onInitializeComponent(context!!, bundle)
    }

    protected open fun createCamera1(): BaseVideoRetriever? {
        return Camera1SurfaceTextureComponent()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    protected open fun createCamera2(): BaseVideoRetriever? {
        return Camera2Component()
    }

    override fun listenForFrame(func: () -> Unit) {
        retriever?.listenForFrame(func)
    }

    override fun removeListenerForFrame() {
        retriever?.removeListenerForFrame()
    }

    override fun disableInternal() {
        super.disableInternal()
        runBlocking {
            retriever?.disable()?.await()
            retriever?.setEventListener(null)
        }
        retriever = null
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun validateCamera2Support(context: Context, cameraId: Int): Boolean {
        try {
            val cm = (context.getSystemService(Context.CAMERA_SERVICE) as CameraManager)
            val hardwareLevel = cm.getCameraCharacteristics(
                cm.cameraIdList[cameraId]
            )[CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL]
            return hardwareLevel != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY
                    && hardwareLevel != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED
        } catch (_: Exception) {

        }
        return false
    }
}