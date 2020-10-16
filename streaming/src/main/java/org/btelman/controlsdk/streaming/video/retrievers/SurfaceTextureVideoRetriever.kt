package org.btelman.controlsdk.streaming.video.retrievers

import org.btelman.controlsdk.streaming.utils.EglCore
import org.btelman.controlsdk.streaming.utils.SurfaceTextureUtils
import javax.microedition.khronos.egl.EGLSurface

/**
 * Creates an off screen surface texture for rendering the camera preview
 *
 * Compatible down to API16
 */
abstract class SurfaceTextureVideoRetriever : BaseVideoRetriever() {
    protected abstract fun setupCamera()
    protected abstract fun releaseCamera()
    protected var eglCore: EglCore? = null
    protected var eglSurface: EGLSurface? = null
    protected var mStManager : SurfaceTextureUtils.SurfaceTextureManager? = null

    init {
        eglCore = EglCore()
    }

    override fun enableInternal() {
        super.enableInternal()
        eglSurface = eglCore?.createOffscreenSurface(streamInfo!!.width, streamInfo!!.height)
        eglCore?.makeCurrent(eglSurface)
        mStManager = SurfaceTextureUtils.SurfaceTextureManager()
        setupCamera()
    }

    override fun disableInternal() {
        super.disableInternal()
        releaseCamera()
        mStManager?.surfaceTexture?.release()
        eglCore?.releaseSurface(eglSurface)
        eglSurface = null
        mStManager = null
    }
}