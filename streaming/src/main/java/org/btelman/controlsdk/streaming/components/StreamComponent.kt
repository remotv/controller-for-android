package org.btelman.controlsdk.streaming.components

import android.content.Context
import android.os.Bundle
import android.os.Message
import kotlinx.coroutines.runBlocking
import org.btelman.controlsdk.enums.ComponentStatus
import org.btelman.controlsdk.enums.ComponentType
import org.btelman.controlsdk.models.Component
import org.btelman.controlsdk.streaming.models.StreamInfo


/**
 * Created by Brendon on 7/14/2019.
 */
abstract class StreamComponent<R : StreamSubComponent,P : StreamSubComponent> : Component() {
    protected lateinit var streamInfo : StreamInfo
    protected lateinit var processor : P
    protected lateinit var retriever : R
    protected var shouldAutoUpdateLoop = true

    override fun onInitializeComponent(applicationContext: Context, bundle: Bundle?) {
        super.onInitializeComponent(applicationContext, bundle)
        bundle?.let {
            streamInfo = StreamInfo.fromBundle(it) ?: throw IllegalArgumentException("Must use StreamInfo Bundle")
        } ?: throw IllegalArgumentException("Must use StreamInfo Bundle")
    }

    override fun enableInternal() {
        log.d{"enableInternal"}
        runBlocking {
            processor.setEventListener(eventDispatcher)
            processor.updateStreamInfo(streamInfo)
            processor.enable().await()

            retriever.updateStreamInfo(streamInfo)
            retriever.setEventListener(eventDispatcher)
            retriever.enable().await()
        }
        push(DO_FRAME)
    }

    override fun disableInternal() {
        runBlocking {
            retriever.disable().await()
            retriever.setEventListener(null)
            processor.disable().await()
            processor.setEventListener(null)
        }
    }

    override fun handleMessage(message: Message): Boolean {
        when(message.what){
            DO_FRAME -> updateLoop()
            FRAME_FETCH -> fetchFrame()
        }
        return super.handleMessage(message)
    }

    fun fetchFrame() {
        if(shouldAutoUpdateLoop) push(DO_FRAME)
        doWorkLoop()
    }

    abstract fun doWorkLoop()

    fun updateLoop(){
        push(FRAME_FETCH)
    }

    override fun getInitialStatus(): ComponentStatus {
        return ComponentStatus.STABLE
    }

    override fun getType(): ComponentType {
        return ComponentType.STREAMING
    }

    fun push(what : Int, obj : Any? = null){
        if(!handler.hasMessages(what)) {
            val message = obj?.let {
                handler.obtainMessage(what, it)
            } ?: handler.obtainMessage(what)
            message.sendToTarget()
        }
    }

    companion object{
        const val FRAME_FETCH = 0
        const val FRAME_PUSH = 1
        const val DO_FRAME = 2
    }
}