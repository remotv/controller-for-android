package org.btelman.controlsdk.streaming.video.retrievers

import org.btelman.controlsdk.streaming.models.ImageDataPacket

/**
 * Return the same packet. Nothing actually gets done here
 */
class DummyRetriever : BaseVideoRetriever() {
    private val imageDataPacket = ImageDataPacket(null)
    override fun grabImageData(): ImageDataPacket? {
        imageDataPacket.timecode = System.currentTimeMillis()
        return imageDataPacket
    }
}