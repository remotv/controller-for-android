package org.btelman.controlsdk.streaming.factories

import android.os.Bundle
import org.btelman.controlsdk.streaming.audio.processors.BaseAudioProcessor
import org.btelman.controlsdk.streaming.audio.processors.FFmpegAudioProcessor
import org.btelman.controlsdk.utils.BundleUtil

/**
 * Handles creating the BaseVideoProcessor instance or putting the class in the main bundle
 */
object AudioProcessorFactory {
    fun findProcessor(bundle: Bundle): BaseAudioProcessor? {
        BundleUtil.checkForAndInitClass(getClassFromBundle(bundle), BaseAudioProcessor::class.java)?.let {
            return it
        }
        return DEFAULT.newInstance()
    }

    fun <T : BaseAudioProcessor> putClassInBundle(clazz: Class<T>, bundle: Bundle){
        BundleUtil.intoBundle(BUNDLE_ID, clazz, bundle)
    }

    fun getClassFromBundle(bundle: Bundle) : Class<*>?{
        return BundleUtil.getClassFromBundle(bundle, BUNDLE_ID)
    }

    val DEFAULT = FFmpegAudioProcessor::class.java
    const val BUNDLE_ID = "audioProcessorClass"
}
