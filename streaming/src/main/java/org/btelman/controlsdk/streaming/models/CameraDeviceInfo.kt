package org.btelman.controlsdk.streaming.models

import android.media.MediaRecorder
import android.os.Bundle

/**
 * Store basic device info for camera and microphone
 *
 * @param camera the video source.
 * @param audio the recording source.
 *   See {@link MediaRecorder.AudioSource}
 */
data class CameraDeviceInfo (var camera: String, var audio : Int = MediaRecorder.AudioSource.DEFAULT){
    fun toBundle() : Bundle{
        return addToExistingBundle(Bundle())
    }

    fun addToExistingBundle(bundle: Bundle) : Bundle{
        bundle.putString("camera", camera)
        bundle.putInt("audio", audio)
        return bundle
    }

    fun getCameraId() : Int{
        return camera.substringAfter("/dev/camera", "0").toInt()
    }

    companion object{
        /**
         * Use the same DeviceInfo constructor by using /dev/camera#,
         * and the component will just use the number for the camera type
         * Does not use /dev/video# since those are reserved for the web cam component
         */
        fun fromCamera(id : Int = 0) : CameraDeviceInfo{
            return CameraDeviceInfo("/dev/camera$id")
        }

        @Throws(NullPointerException::class)
        fun fromBundle(bundle: Bundle): CameraDeviceInfo? {
            return bundle.getString("camera")?.let {camera ->
                CameraDeviceInfo(camera,
                    bundle.getInt("audio"))
            } //?: null
        }
    }
}