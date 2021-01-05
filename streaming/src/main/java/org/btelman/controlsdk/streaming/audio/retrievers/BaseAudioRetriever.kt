package org.btelman.controlsdk.streaming.audio.retrievers

import org.btelman.controlsdk.streaming.components.StreamSubComponent
import org.btelman.controlsdk.streaming.models.AudioPacket

/**
 * Created by Brendon on 7/14/2019.
 */
abstract class BaseAudioRetriever : StreamSubComponent(){
    abstract fun retrieveAudioByteArray() : AudioPacket?
}