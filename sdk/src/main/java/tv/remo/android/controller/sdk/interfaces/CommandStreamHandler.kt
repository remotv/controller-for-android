package tv.remo.android.controller.sdk.interfaces

import org.btelman.controlsdk.streaming.components.StreamSubComponent
import org.btelman.controlsdk.streaming.models.StreamInfo
import tv.remo.android.controller.sdk.models.CommandSubscriptionData

/**
 * List of functions needed for the StreamCommandHandler to operate successfully
 */
interface CommandStreamHandler{
    fun resetComponents()

    /**
     * Acquire the retriever. Would be easier just to use StreamComponent if this variable was not protected
     */
    fun acquireRetriever() : StreamSubComponent
    /**
     * Acquire the processor. Would be easier just to use StreamComponent if this variable was not protected
     */
    fun acquireProcessor() : StreamSubComponent

    /**
     * Acquire the StreamInfo, usually modifying it and sending it back using pushStreamInfo
     */
    fun pullStreamInfo() : StreamInfo

    /**
     * Set new StreamInfo to the StreamComponent we are attached to, at least we assume it is that
     */
    fun pushStreamInfo(streamInfo: StreamInfo)

    /**
     * Return an array of commands we want to be made aware of if mentioned
     */
    fun onRegisterCustomCommands() : ArrayList<CommandSubscriptionData>?
}