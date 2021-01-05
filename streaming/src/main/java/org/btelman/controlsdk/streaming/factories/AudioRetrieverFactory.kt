package org.btelman.controlsdk.streaming.factories

import android.os.Bundle
import org.btelman.controlsdk.streaming.audio.retrievers.BaseAudioRetriever
import org.btelman.controlsdk.streaming.audio.retrievers.BasicMicrophoneAudioRetriever
import org.btelman.controlsdk.utils.BundleUtil

/**
 * Handles creating the BaseVideoProcessor instance or putting the class in the main bundle
 */
object AudioRetrieverFactory {
    fun findRetriever(bundle: Bundle): BaseAudioRetriever? {
        BundleUtil.checkForAndInitClass(getClassFromBundle(bundle), BaseAudioRetriever::class.java)?.let {
            return it
        }
        return DEFAULT.newInstance()
    }

    fun <T : BaseAudioRetriever> putClassInBundle(clazz: Class<T>, bundle: Bundle){
        BundleUtil.intoBundle(BUNDLE_ID, clazz, bundle)
    }

    fun getClassFromBundle(bundle: Bundle) : Class<*>?{
        return BundleUtil.getClassFromBundle(bundle, BUNDLE_ID)
    }

    val DEFAULT = BasicMicrophoneAudioRetriever::class.java
    const val BUNDLE_ID = "audioRetrieverClass"
}
