package tv.remo.android.controller.utils.v21

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
object Camera2Util {
    @Throws(ArrayIndexOutOfBoundsException::class)
    fun getCameraSizes(context : Context, cameraIndex : Int = 0) : ArrayList<Pair<Int, Int>> {
        val list = ArrayList<Pair<Int, Int>>()
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = manager.cameraIdList[cameraIndex]
            val characteristics = manager.getCameraCharacteristics(cameraId)

            val map =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            // Images supported size by device
            val pictureSizes = map!!.getOutputSizes(ImageFormat.YUV_420_888)
            pictureSizes.forEach {
                list.add(Pair(it.height, it.width))
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            Log.e("RemoApplication", "setUpCameraOutputs: catch: " + e.message)
        } catch (e: NullPointerException) {
            Log.e("RemoApplication", "setUpCameraOutputs: catch: " + e.message)
        }
        return list
    }

    fun checkFullyCompatible(context: Context, cameraIndex: Int): Boolean {
        kotlin.runCatching {
            val cm = (context.getSystemService(Context.CAMERA_SERVICE) as CameraManager)
            val hardwareLevel = cm.getCameraCharacteristics(
                cm.cameraIdList[cameraIndex]
            )[CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL]
            return hardwareLevel != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY
                    && hardwareLevel != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED
        }
        return false
    }

    fun getCameras(context: Context) : Int{
        val cm = (context.getSystemService(Context.CAMERA_SERVICE) as CameraManager)
        return cm.cameraIdList.size
    }
}
