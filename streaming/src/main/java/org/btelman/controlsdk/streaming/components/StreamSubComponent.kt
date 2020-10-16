package org.btelman.controlsdk.streaming.components

import org.btelman.controlsdk.enums.ComponentStatus
import org.btelman.controlsdk.enums.ComponentType
import org.btelman.controlsdk.models.Component
import org.btelman.controlsdk.streaming.models.StreamInfo

/**
 * Class that all video or audio sub-components will use
 */
abstract class StreamSubComponent : Component() {
    protected var streamInfo: StreamInfo? = null
    open fun updateStreamInfo(streamInfo: StreamInfo){
        log.d{"updateStreamInfo"}
        this.streamInfo = streamInfo
    }

    override fun getType(): ComponentType {
        return ComponentType.STREAMING
    }

    override fun getInitialStatus(): ComponentStatus {
        return ComponentStatus.CONNECTING
    }

    override fun enableInternal() {
        //intentionally empty
    }

    override fun disableInternal() {
        //intentionally empty
    }
}