package org.btelman.controlsdk.streaming.video.processors

import org.btelman.controlsdk.streaming.components.StreamSubComponent
import org.btelman.controlsdk.streaming.models.ImageDataPacket

/**
 * Handles core logic of processing the video and usually sending it out to a service
 */
abstract class BaseVideoProcessor : StreamSubComponent() {
    abstract fun processData(packet: ImageDataPacket)
}
