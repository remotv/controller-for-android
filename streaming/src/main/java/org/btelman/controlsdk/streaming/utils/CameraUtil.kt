package org.btelman.controlsdk.streaming.utils

import android.content.Context
import android.os.Build
import org.btelman.controlsdk.streaming.utils.v16.Camera1Util
import org.btelman.controlsdk.streaming.utils.v24.Camera2Util
import java.util.*

/**
 * Created by Brendon on 2/27/2020.
 */
object CameraUtil{
    fun getCameraSizes(requireContext: Context, cameraIndex: Int): ArrayList<Pair<Int, Int>> {
        val sizes =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            && isFullyCamera2Compatible(requireContext, cameraIndex)
        ) {
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
        }
        return finalList
    }

    fun getCameraCount(context: Context) : Int{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Camera2Util.getCameras(context)
        }else{
            Camera1Util.getCameras()
        }
    }

    fun isFullyCamera2Compatible(context: Context, cameraIndex: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Camera2Util.checkFullyCompatible(context, cameraIndex)
        } else {
            false
        }
    }
}