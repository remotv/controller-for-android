package org.btelman.controlsdk.streaming.audio.processors

import org.btelman.controlsdk.streaming.components.StreamSubComponent
import org.btelman.controlsdk.streaming.models.AudioPacket

/**
 * Class that will abstract the functions for processing audio locally or to stream it
 */
abstract class BaseAudioProcessor : StreamSubComponent(){
    abstract fun processAudioByteArray(data : AudioPacket)
}