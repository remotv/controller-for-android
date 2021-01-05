/*
     * Copyright 2013 Google Inc. All rights reserved.
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
/*
* https://github.com/google/grafika/issues/80
* */

package org.btelman.controlsdk.streaming.utils;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * Core EGL state (display, context, config).
 * <p>
 * The EGLContext must only be attached to one thread at a time.  This class is not thread-safe.
 */
public final class EglCore {
    public class EglCoreException extends Exception{
        EglCoreException(String egl_already_set_up) {
            super(egl_already_set_up);
        }
    }
    private static final String TAG = EglCore.class.getSimpleName();
    private EGL10 mEgl;
    private EGLContext mEGLContext = EGL11.EGL_NO_CONTEXT;
    private EGLDisplay mEGLDisplay = EGL11.EGL_NO_DISPLAY;
    private EGLConfig mEGLConfig = null;

    /**
     * Prepares EGL display and context.
     * <p>
     */
    public EglCore() throws EglCoreException {
        if (mEGLDisplay != EGL11.EGL_NO_DISPLAY) {
            throw new EglCoreException("EGL already set up");
        }
        mEgl = (EGL10) EGLContext.getEGL();
        mEGLDisplay = mEgl.eglGetDisplay(EGL11.EGL_DEFAULT_DISPLAY);
        if (mEGLDisplay == EGL11.EGL_NO_DISPLAY) {
            throw new EglCoreException("unable to get EGL14 display");
        }

        int[] version = new int[2];
        if (!mEgl.eglInitialize(mEGLDisplay, version)) {
            mEGLDisplay = null;
            throw new EglCoreException("unable to initialize EGL11");
        }

        EGLConfig config = getConfig();
        if (config == null) {
            throw new EglCoreException("Unable to find a suitable EGLConfig");
        }
            /*uses gles10 by default here*/
        int[] attrib2_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL11.EGL_NONE
        };

        EGLContext context = mEgl.eglCreateContext(mEGLDisplay, config, EGL11.EGL_NO_CONTEXT,
                attrib2_list);
        checkEglError("eglCreateContext");
        mEGLConfig = config;
        mEGLContext = context;

        // Confirm with query.
        int[] values = new int[1];
        mEgl.eglQueryContext(mEGLDisplay, mEGLContext, EGL14.EGL_CONTEXT_CLIENT_VERSION,
                values);
        Log.d(TAG, "EGLContext created, client version " + values[0]);
    }

    /**
     * Finds a suitable EGLConfig.
     */
    private EGLConfig getConfig() {
        int renderableType = EGL14.EGL_OPENGL_ES2_BIT;

        // The actual surface is generally RGBA or RGBX, so situationally omitting alpha
        // doesn't really help.  It can also lead to a huge performance hit on glReadPixels()
        // when reading into a GL_RGBA buffer.
        int[] attribList = {
                EGL11.EGL_RED_SIZE, 8,
                EGL11.EGL_GREEN_SIZE, 8,
                EGL11.EGL_BLUE_SIZE, 8,
                EGL11.EGL_ALPHA_SIZE, 8,
                //EGL14.EGL_DEPTH_SIZE, 16,
                //EGL14.EGL_STENCIL_SIZE, 8,
                EGL11.EGL_RENDERABLE_TYPE, renderableType,
                EGL11.EGL_NONE, 0,      // placeholder for recordable [@-3]
                EGL11.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!mEgl.eglChooseConfig(mEGLDisplay, attribList, configs, configs.length,
                numConfigs)) {
            Log.w(TAG, "unable to find RGB8888 / EGLConfig");
            return null;
        }
        return configs[0];
    }

    /**
     * Writes the current display, context, and surface to the log.
     */
    public void logCurrent(String msg) {
        EGLDisplay display;
        EGLContext context;
        EGLSurface surface;

        display = mEGLDisplay;
        context = mEgl.eglGetCurrentContext();
        surface = mEgl.eglGetCurrentSurface(EGL11.EGL_DRAW);
        Log.i(TAG, "Current EGL (" + msg + "): display=" + display + ", context=" + context +
                ", surface=" + surface);
    }

    /**
     * Destroys the specified surface.  Note the EGLSurface won't actually be destroyed if it's
     * still current in a context.
     */
    public void releaseSurface(EGLSurface eglSurface) {
        mEgl.eglDestroySurface(mEGLDisplay, eglSurface);
    }

    /**
     * Creates an EGL surface associated with a Surface.
     * <p>
     * If this is destined for MediaCodec, the EGLConfig should have the "recordable" attribute.
     */
    public EGLSurface createWindowSurface(Object surface) throws EglCoreException {
        if (!(surface instanceof Surface) && !(surface instanceof SurfaceTexture) &&
                !(surface instanceof SurfaceHolder)) {
            throw new EglCoreException("invalid surface: " + surface);
        }

        // Create a window surface, and attach it to the Surface we received.
        int[] surfaceAttribs = {
                EGL10.EGL_NONE
        };
        EGLSurface eglSurface = mEgl.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surface,
                surfaceAttribs);
        checkEglError("eglCreateWindowSurface");
        if (eglSurface == null) {
            throw new EglCoreException("surface was null");
        }
        return eglSurface;
    }

    /**
     * Creates an EGL surface associated with an offscreen buffer.
     */
    public EGLSurface createOffscreenSurface(int width, int height) throws EglCoreException {
        int[] surfaceAttribs = {
                EGL11.EGL_WIDTH, width,
                EGL11.EGL_HEIGHT, height,
                EGL11.EGL_NONE
        };
        EGLSurface eglSurface = mEgl.eglCreatePbufferSurface(mEGLDisplay,
                mEGLConfig, surfaceAttribs);
        checkEglError("eglCreatePbufferSurface");
        if (eglSurface == null) {
            throw new EglCoreException("surface was null");
        }
        return eglSurface;
    }

    /**
     * Makes our EGL context current, using the supplied surface for both "draw" and "read".
     */
    public void makeCurrent(EGLSurface eglSurface) throws EglCoreException {
        if (mEGLDisplay == EGL11.EGL_NO_DISPLAY) {
            // called makeCurrent() before create?
            Log.d(TAG, "NOTE: makeCurrent w/o display");
        }
        if (!mEgl.eglMakeCurrent(mEGLDisplay, eglSurface, eglSurface, mEGLContext)) {
            throw new EglCoreException("eglMakeCurrent failed");
        }
    }

    /**
     * Makes our EGL context current, using the supplied "draw" and "read" surfaces.
     */
    public void makeCurrent(EGLSurface drawSurface, EGLSurface readSurface) throws EglCoreException {
        if (mEGLDisplay == EGL11.EGL_NO_DISPLAY) {
            // called makeCurrent() before create?
            Log.d(TAG, "NOTE: makeCurrent w/o display");
        }
        if (!mEgl.eglMakeCurrent(mEGLDisplay, drawSurface, readSurface, mEGLContext)) {
            throw new EglCoreException("eglMakeCurrent(draw,read) failed");
        }
    }

    /**
     * Makes no context current.
     */
    public void makeNothingCurrent() throws EglCoreException {
        if (!mEgl.eglMakeCurrent(mEGLDisplay, EGL11.EGL_NO_SURFACE
                , EGL11.EGL_NO_SURFACE, EGL11.EGL_NO_CONTEXT)) {
            throw new EglCoreException("eglMakeCurrent failed");
        }
    }

    /**
     * Calls eglSwapBuffers.  Use this to "publish" the current frame.
     *
     * @return false on failure
     */
    public boolean swapBuffers(EGLSurface eglSurface) {
        return mEgl.eglSwapBuffers(mEGLDisplay, eglSurface);
    }

    /**
     * Returns true if our context and the specified surface are current.
     */
    public boolean isCurrent(EGLSurface eglSurface) {
        return mEGLContext.equals(mEgl.eglGetCurrentContext()) &&
                eglSurface.equals(mEgl.eglGetCurrentSurface(EGL11.EGL_DRAW));
    }

    /**
     * Performs a simple surface query.
     */
    public int querySurface(EGLSurface eglSurface, int what) {
        int[] value = new int[1];
        mEgl.eglQuerySurface(mEGLDisplay, eglSurface, what, value);
        return value[0];
    }

    /**
     * Queries a string value.
     */
    public String queryString(int what) {
        return mEgl.eglQueryString(mEGLDisplay, what);
    }

    /**
     * Returns the GLES version this context is configured for (currently 2 or 3).
     */
    public int getGlVersion() {
        return 2;
    }

    /**
     * Checks for EGL errors.  Throws an exception if an error has been raised.
     */
    private void checkEglError(String msg) throws EglCoreException {
        int error;
        if ((error = mEgl.eglGetError()) != EGL10.EGL_SUCCESS) {
            throw new EglCoreException(msg + ": EGL error: 0x" + Integer.toHexString(error));
        }
    }
}