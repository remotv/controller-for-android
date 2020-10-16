package org.btelman.controlsdk.streaming.factories

import android.os.Bundle
import org.btelman.controlsdk.streaming.models.StreamInfo
import org.btelman.controlsdk.streaming.video.retrievers.BaseVideoRetriever
import org.btelman.controlsdk.streaming.video.retrievers.CameraCompatRetriever
import org.btelman.controlsdk.utils.BundleUtil

object VideoRetrieverFactory {
    fun findRetriever(bundle: Bundle): BaseVideoRetriever? {
        BundleUtil.checkForAndInitClass(getClassFromBundle(bundle), BaseVideoRetriever::class.java)?.let {
            return it
        }
        StreamInfo.fromBundle(bundle)?.also {streamInfo ->
            when {
                streamInfo.deviceInfo.camera.contains("/dev/video") ->
                    TODO("USB Camera retriever class")
                streamInfo.deviceInfo.camera.contains("/dev/camera") ->
                    return CameraCompatRetriever()
                streamInfo.deviceInfo.camera.contains("http") ->
                    TODO("Camera stream from other device")
            }
        }
        return DEFAULT.newInstance()
    }

    fun <T : BaseVideoRetriever> putClassInBundle(clazz: Class<T>, bundle: Bundle){
        BundleUtil.intoBundle(BUNDLE_ID, clazz, bundle)
    }

    fun getClassFromBundle(bundle: Bundle) : Class<*>?{
        return BundleUtil.getClassFromBundle(bundle, BUNDLE_ID)
    }

    val DEFAULT = CameraCompatRetriever::class.java
    const val BUNDLE_ID = "videoRetrieverClass"
}
