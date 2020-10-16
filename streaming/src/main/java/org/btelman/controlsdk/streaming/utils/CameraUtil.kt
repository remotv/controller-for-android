package org.btelman.controlsdk.streaming.utils

import android.annotation.TargetApi
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build

object CameraUtil {
    fun supportsNDKCamera(context: Context, cameraId: Int) : Boolean{
        return Build.VERSION.SDK_INT >= 27 && validateCamera2Support(context, cameraId)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun validateCamera2Support(context: Context, cameraId: Int): Boolean {
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