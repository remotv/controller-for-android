package org.btelman.controlsdk.streaming.components

import android.content.Context
import android.os.Bundle
import org.btelman.controlsdk.streaming.factories.VideoProcessorFactory
import org.btelman.controlsdk.streaming.factories.VideoRetrieverFactory
import org.btelman.controlsdk.streaming.models.ImageDataPacket
import org.btelman.controlsdk.streaming.video.processors.BaseVideoProcessor
import org.btelman.controlsdk.streaming.video.retrievers.BaseVideoRetriever

/**
 * Component that will handle the core of the video streaming.
 *
 * Other classes will extend this for connectivity with specific integrations
 */
open class VideoComponent : StreamComponent<BaseVideoRetriever, BaseVideoProcessor>()  {
    private var sendStaleFramesWhenStarved = false
    private var sendStaleFramesDelay = 0
    private var targetFPS = 30
    private var lastTimeCode = 0L

    /**
     * Sets the loopMode. Defaults to false. To change this, please override it
     */
    open fun setLoopMode(){
        shouldAutoUpdateLoop = false
    }

    override fun onInitializeComponent(applicationContext: Context, bundle: Bundle?) {
        super.onInitializeComponent(applicationContext, bundle)
        bundle!!
        processor = VideoProcessorFactory.findProcessor(bundle) ?: throw IllegalArgumentException("unable to resolve video processor")
        processor.onInitializeComponent(applicationContext, bundle)

        retriever = VideoRetrieverFactory.findRetriever(bundle) ?: throw IllegalArgumentException("unable to resolve video retriever")
        retriever.onInitializeComponent(applicationContext, bundle)
        setLoopMode()
        targetFPS = bundle.getInt(VIDEO_FRAMERATE_LOOP, 30)
        sendStaleFramesWhenStarved = bundle.getBoolean(VIDEO_SEND_STALE_FRAMES_WHEN_STARVED, false)
        if(sendStaleFramesWhenStarved)
            sendStaleFramesDelay = bundle.getInt(VIDEO_STALE_FRAME_DELAY, 1000)
    }

    override fun enableInternal() {
        super.enableInternal()
        retriever.listenForFrame{
            onFrameUpdate()
        }
    }

    fun onFrameUpdate(){
        push(DO_FRAME)
    }

    override fun disableInternal() {
        super.disableInternal()
        retriever.removeListenerForFrame()
    }

    override fun doWorkLoop() {
        retriever.grabImageData()?.let {
            //Don't send more than one frame twice
            //Don't allow data to update more than desired frames per second
            if(!shouldProcessData(it, lastTimeCode)) return
            lastTimeCode = it.timecode
            processor.processData(it)
        }
    }

    private fun shouldProcessData(packet : ImageDataPacket, lastTimeCode : Long) : Boolean{
        val timeCodeDiff = System.currentTimeMillis()-lastTimeCode
        if (lastTimeCode == packet.timecode) {
            /*only time this evaluates to true is when starved for more than delay.
                Once it reaches this point, the check will always succeed until a new frame is retrieved.
                It will still be limited by the framerate check*/
            return sendStaleFramesWhenStarved
                    && timeCodeDiff > sendStaleFramesDelay
        }
        if (timeCodeDiff < 1000/targetFPS) return false
        return true
    }

    companion object{
        /**
         * How fast the class will try to process frames as Integer
         *
         * DEFAULT: 30
         */
        const val VIDEO_FRAMERATE_LOOP = "VideoComponent.FrameRate"
        /**
         * Whether or not stale frames are allowed to send at all as Boolean
         *
         * DEFAULT: false
         */
        const val VIDEO_SEND_STALE_FRAMES_WHEN_STARVED = "VideoComponent.SendStaleFrames"
        /**
         * Delay that is needed before stale frames will start being processed.
         * Stale frames do not normally get processed right away
         *
         * DEFAULT: 1000 milliseconds
         */
        const val VIDEO_STALE_FRAME_DELAY = "VideoComponent.FreshToStaleDelay"
    }
}