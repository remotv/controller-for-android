package org.btelman.controlsdk.streaming.factories

import android.os.Bundle
import org.btelman.controlsdk.models.ComponentHolder
import org.btelman.controlsdk.streaming.models.StreamInfo
import org.btelman.controlsdk.streaming.video.retrievers.BaseVideoRetriever
import org.btelman.controlsdk.streaming.video.retrievers.api16.Camera1SurfaceTextureComponent
import org.btelman.controlsdk.utils.BundleUtil

object VideoRetrieverFactory {
    fun <T : BaseVideoRetriever> putClassInBundle(clazz: Class<T>, bundle: Bundle){
        BundleUtil.intoBundle(BUNDLE_ID, clazz, bundle)
    }

    fun getClassFromBundle(bundle: Bundle) : Class<BaseVideoRetriever>?{
        @Suppress("UNCHECKED_CAST")
        (return try{
            BundleUtil.getClassFromBundle(bundle, BUNDLE_ID) as Class<BaseVideoRetriever>
        } catch (e : Exception){
            e.printStackTrace()
            null
        })
    }

    val DEFAULT = Camera1SurfaceTextureComponent::class.java
    const val BUNDLE_ID = "videoRetrieverClass"

    fun findRetriever(bundle: Bundle): ComponentHolder<*>? {
        val clazz = getClassFromBundle(bundle)
        clazz?.let {
            return ComponentHolder(clazz, bundle)
        }
        StreamInfo.fromBundle(bundle)?.also {streamInfo ->
            when {
                streamInfo.deviceInfo.camera.contains("/dev/video") ->
                    TODO("USB Camera retriever class")
                streamInfo.deviceInfo.camera.contains("/dev/camera") ->
                    return ComponentHolder(Camera1SurfaceTextureComponent::class.java, bundle)
                streamInfo.deviceInfo.camera.contains("http") ->
                    TODO("Camera stream from other device")
            }
        }
        return ComponentHolder(DEFAULT, bundle)
    }
}
