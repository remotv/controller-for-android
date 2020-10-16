package org.btelman.controlsdk.streaming.factories

import android.os.Bundle
import org.btelman.controlsdk.streaming.video.processors.BaseVideoProcessor
import org.btelman.controlsdk.streaming.video.processors.FFmpegVideoProcessor
import org.btelman.controlsdk.utils.BundleUtil

/**
 * Handles creating the BaseVideoProcessor instance or putting the class in the main bundle
 */
object VideoProcessorFactory {
    fun findProcessor(bundle: Bundle): BaseVideoProcessor? {
        BundleUtil.checkForAndInitClass(getClassFromBundle(bundle), BaseVideoProcessor::class.java)?.let {
            return it
        }
        return DEFAULT.newInstance()
    }

    fun <T : BaseVideoProcessor> putClassInBundle(clazz: Class<T>, bundle: Bundle){
        BundleUtil.intoBundle(BUNDLE_ID, clazz, bundle)
    }

    fun getClassFromBundle(bundle: Bundle) : Class<*>?{
        return BundleUtil.getClassFromBundle(bundle, BUNDLE_ID)
    }

    val DEFAULT = FFmpegVideoProcessor::class.java
    const val BUNDLE_ID = "videoProcessorClass"
}
