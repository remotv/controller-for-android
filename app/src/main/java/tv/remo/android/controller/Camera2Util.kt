package tv.remo.android.controller

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import tv.remo.android.controller.sdk.utils.ValueUtil

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
object Camera2Util {
    @Throws(ArrayIndexOutOfBoundsException::class)
    fun GetCameraSizes(context : Context, cameraIndex : Int = 0) : ArrayList<Pair<Int, Int>> {
        val list = ArrayList<Pair<Int, Int>>()
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = manager.cameraIdList[cameraIndex]
            val characteristics = manager.getCameraCharacteristics(cameraId)

            val map =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            // Images supported size by device
            val pictureSizes = map!!.getOutputSizes(ImageFormat.YUV_420_888)

            val ratio16by9 = 16f / 9f
            val ratio4by3 = 4f / 3f
            val ratio3by2 = 3f / 2f
            pictureSizes.forEach {
                val height = it.height
                val width = it.width
                val ratio = (width * 1f / height * 1f)
                val outsideOfAllowedSizes = width < 640 || height < 480
                        || width > 1920 || height > 1080
                val isAllowedRatio = ratio == ratio16by9
                        || ratio == ratio4by3
                        || ratio == ratio3by2
                if (isAllowedRatio && !outsideOfAllowedSizes) {
                    list.add(Pair(width, height))
                }

                //logging only
                val gcm = ValueUtil.gcm(width, height)
                Log.d(
                    "RemoApplication",
                    "PictureSize: ${width}x${height} : ${width / gcm}:${height / gcm}"
                )
            }
            /*TODO switch to video recording // Video supported size by device
            val videoSizes = map.getOutputSizes(MediaRecorder::class.java)
            videoSizes.forEach {
                Log.d("RemoApplication", "VideoSize: ${it.width}x${it.height}")
            }*/
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            Log.e("RemoApplication", "setUpCameraOutputs: catch: " + e.message)
        } catch (e: NullPointerException) {
            Log.e("RemoApplication", "setUpCameraOutputs: catch: " + e.message)
        }
        return list
    }
}
