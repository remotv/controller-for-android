package tv.remo.android.controller.utils

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import tv.remo.android.controller.sdk.utils.ValueUtil
import tv.remo.android.controller.utils.v16.Camera1Util
import tv.remo.android.controller.utils.v21.Camera2Util
import java.util.*

/**
 * Created by Brendon on 2/27/2020.
 */
object CameraUtil{
    fun getCameraSizes(requireContext: Context, cameraIndex: Int): ArrayList<Pair<Int, Int>> {
        val sizes =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
            && isFullyCamera2Compatible(requireContext, cameraIndex)) {
            Camera2Util.getCameraSizes(requireContext, cameraIndex)
        } else{
            Camera1Util.getCameraSizes(requireContext, cameraIndex)
        }
        return filterSizes(sizes)
    }

    private fun filterSizes(sizes: ArrayList<Pair<Int, Int>>): ArrayList<Pair<Int, Int>> {
        val finalList = ArrayList<Pair<Int, Int>>()
        val ratio16by9 = 16f / 9f
        val ratio4by3 = 4f / 3f
        val ratio3by2 = 3f / 2f
        sizes.forEach {
            val height = it.first
            val width = it.second
            val ratio = (width * 1f / height * 1f)
            val outsideOfAllowedSizes = width < 640 || height < 480
                    || width > 1920 || height > 1080
            val isAllowedRatio = ratio == ratio16by9
                    || ratio == ratio4by3
                    || ratio == ratio3by2
            if (isAllowedRatio && !outsideOfAllowedSizes) {
                finalList.add(Pair(width, height))
            }

            //logging only
            val gcm = ValueUtil.gcm(width, height)
            Log.d(
                "RemoApplication",
                "PictureSize: ${width}x${height} : ${width / gcm}:${height / gcm}"
            )
        }
        return finalList
    }

    fun getCameraCount(context: Context) : Int{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Camera2Util.getCameras(context)
        }else{
            Camera1Util.getCameras()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun isFullyCamera2Compatible(context: Context, cameraIndex: Int): Boolean {
        return Camera2Util.checkFullyCompatible(context, cameraIndex);
    }
}