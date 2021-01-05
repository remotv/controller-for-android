package org.btelman.controlsdk.streaming.factories

import android.os.Bundle
import org.btelman.controlsdk.interfaces.IControlSDKElement
import org.btelman.controlsdk.models.ComponentHolder
import org.btelman.controlsdk.streaming.video.processors.BaseVideoProcessor
import org.btelman.controlsdk.streaming.video.processors.LegacyFFmpegVideoProcessor
import org.btelman.controlsdk.utils.BundleUtil

/**
 * Handles creating the BaseVideoProcessor instance or putting the class in the main bundle
 */
object VideoProcessorFactory {
    fun findProcessor(bundle: Bundle): ComponentHolder<*>? {
        val clazz = getClassFromBundle(bundle)
        clazz?.let {
            return ComponentHolder(clazz, bundle)
        }
        return ComponentHolder(DEFAULT, bundle)
    }

    fun <T : BaseVideoProcessor> putClassInBundle(clazz: Class<T>, bundle: Bundle){
        BundleUtil.intoBundle(BUNDLE_ID, clazz, bundle)
    }

    fun getClassFromBundle(bundle: Bundle) : Class<IControlSDKElement>?{
        @Suppress("UNCHECKED_CAST")
        (return try{
            BundleUtil.getClassFromBundle(bundle, BUNDLE_ID) as Class<IControlSDKElement>
        } catch (e : Exception){
            e.printStackTrace()
            null
        })
    }

    val DEFAULT = LegacyFFmpegVideoProcessor::class.java
    const val BUNDLE_ID = "videoProcessorClass"
}
