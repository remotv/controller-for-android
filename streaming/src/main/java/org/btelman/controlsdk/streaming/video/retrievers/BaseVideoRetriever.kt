package org.btelman.controlsdk.streaming.video.retrievers

import org.btelman.controlsdk.streaming.components.StreamSubComponent
import org.btelman.controlsdk.streaming.models.ImageDataPacket

/**
 * Base class for retrieving frames to send to the video processor
 */
abstract class BaseVideoRetriever : StreamSubComponent(){
    abstract fun grabImageData() : ImageDataPacket?

    protected var frameListener : (()->Unit)? = null

    open fun notifyFrameUpdated(){
        frameListener?.invoke()
    }

    open fun listenForFrame(func : ()->Unit){
        frameListener = func
    }

    open fun removeListenerForFrame(){
        frameListener = null
    }
}
