package org.btelman.controlsdk.streaming.models

import android.os.Bundle
import org.btelman.controlsdk.streaming.enums.Orientation

/**
 * Data class that will store info for the stream
 */
data class StreamInfo(val endpoint : String,
                      val audioEndpoint : String? = null,
                      val width : Int = 640,
                      val height : Int = 480,
                      val bitrate : Int = 512,
                      val framerate : Int = 30,
                      val orientation : Orientation = Orientation.DIR_90,
                      val deviceInfo: CameraDeviceInfo = CameraDeviceInfo.fromCamera(0)){

    fun toBundle() : Bundle{
        return addToExistingBundle(Bundle())
    }

    fun addToExistingBundle(bundle : Bundle) : Bundle{
        bundle.apply {
            putString("endpoint", endpoint)
            audioEndpoint ?.let { putString("audioEndpoint", it) }
            putInt("width", width)
            putInt("height", height)
            putInt("framerate", framerate)
            putInt("orientation", orientation.value)
            putBundle("deviceInfo", deviceInfo.toBundle())
            bundle.putInt("bitrate", bitrate)
        }
        return bundle
    }

    companion object{
        @Throws(NullPointerException::class)
        fun fromBundle(bundle : Bundle) : StreamInfo?{
            val endpoint = bundle.getString("endpoint") ?: return null
            val audioEndpoint = bundle.getString("audioEndpoint") ?: null //Not needed
            val width = bundle.getInt("width")
            val height = bundle.getInt("height")
            val bitrate = bundle.getInt("bitrate")
            val framerate = bundle.getInt("framerate")
            val orientation = Orientation.forValue(bundle.getInt("orientation")) ?: return null
            val cameraDeviceInfo = bundle.getBundle("deviceInfo")?.let {
                CameraDeviceInfo.fromBundle(it)
            } ?: return null

            return StreamInfo(endpoint, audioEndpoint, width, height, bitrate, framerate, orientation, cameraDeviceInfo)
        }

        fun validateFields(vararg vals : Any?){

        }
    }
}