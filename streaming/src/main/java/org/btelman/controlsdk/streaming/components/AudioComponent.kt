package org.btelman.controlsdk.streaming.components

import android.content.Context
import android.os.Bundle
import org.btelman.controlsdk.streaming.audio.processors.BaseAudioProcessor
import org.btelman.controlsdk.streaming.audio.retrievers.BaseAudioRetriever
import org.btelman.controlsdk.streaming.factories.AudioProcessorFactory
import org.btelman.controlsdk.streaming.factories.AudioRetrieverFactory

/**
 * Audio component to handle doing stuff with audio
 */
open class AudioComponent : StreamComponent<BaseAudioRetriever, BaseAudioProcessor>() {
    override fun onInitializeComponent(applicationContext: Context, bundle: Bundle?) {
        super.onInitializeComponent(applicationContext, bundle)
        bundle!!
        processor = AudioProcessorFactory.findProcessor(bundle) ?: throw IllegalArgumentException("unable to resolve audio processor")
        processor.onInitializeComponent(applicationContext, bundle)
        retriever = AudioRetrieverFactory.findRetriever(bundle) ?: throw IllegalArgumentException("unable to resolve audio retriever")
        retriever.onInitializeComponent(applicationContext, bundle)
    }

    override fun doWorkLoop() {
        retriever.retrieveAudioByteArray()?.let {
            processor.processAudioByteArray(it)
        }
    }
}