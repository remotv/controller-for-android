package tv.remo.android.videointerface;/*
 * Copyright (c) 2018 OpenFTC Team
 *
 * Note: credit where credit is due - some parts of OpenCv's
 *       JavaCameraView were used as a reference
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.firstinspires.ftc.robotcore.external.android.util.Size;
import org.firstinspires.ftc.robotcore.external.function.Consumer;
import org.firstinspires.ftc.robotcore.internal.collections.EvictingBlockingQueue;
import org.firstinspires.ftc.smem.common.BitmapContainer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class BitmapViewport extends SurfaceView implements SurfaceHolder.Callback
{
    private Size size;
    private RenderThread renderThread;
    private Canvas canvas = null;
    private double aspectRatio;
    private static final int VISION_PREVIEW_FRAME_QUEUE_CAPACITY = 2;
    private static final int FRAMEBUFFER_RECYCLER_CAPACITY = VISION_PREVIEW_FRAME_QUEUE_CAPACITY + 2; //So that the evicting queue can be full, and the render thread has one checked out (+1) and post() can still take one (+1).
    private EvictingBlockingQueue<BitmapRecycler.RecyclableBitmap> visionPreviewFrameQueue = new EvictingBlockingQueue<>(new ArrayBlockingQueue<BitmapRecycler.RecyclableBitmap>(VISION_PREVIEW_FRAME_QUEUE_CAPACITY));
    private  BitmapRecycler framebufferRecycler;
    private volatile RenderingState internalRenderingState = RenderingState.STOPPED;
    private final Object syncObj = new Object();
    private volatile boolean userRequestedActive = false;
    private volatile boolean userRequestedPause = false;
    private boolean needToDeactivateRegardlessOfUser = false;
    private boolean surfaceExistsAndIsReady = false;
    private Paint paintBlackBackground;
    private String TAG = "tv.remo.android.videointerface.BitmapViewport";
    private ReentrantLock renderThreadAliveLock = new ReentrantLock();

    public BitmapViewport(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        paintBlackBackground = new Paint();
        paintBlackBackground.setColor(Color.BLACK);
        paintBlackBackground.setStyle(Paint.Style.FILL);

        getHolder().addCallback(this);

        visionPreviewFrameQueue.setEvictAction(new Consumer<BitmapRecycler.RecyclableBitmap>()
        {
            @Override
            public void accept(BitmapRecycler.RecyclableBitmap value)
            {
                /*
                 * If a Mat is evicted from the queue, we need
                 * to make sure to return it to the Mat recycler
                 */
                framebufferRecycler.returnBmp(value);
            }
        });
    }

    public BitmapViewport(Context context, OnClickListener onClickListener)
    {
        super(context);

        paintBlackBackground = new Paint();
        paintBlackBackground.setColor(Color.BLACK);
        paintBlackBackground.setStyle(Paint.Style.FILL);

        getHolder().addCallback(this);

        visionPreviewFrameQueue.setEvictAction(new Consumer<BitmapRecycler.RecyclableBitmap>()
        {
            @Override
            public void accept(BitmapRecycler.RecyclableBitmap value)
            {
                /*
                 * If a Mat is evicted from the queue, we need
                 * to make sure to return it to the Mat recycler
                 */
                framebufferRecycler.returnBmp(value);
            }
        });

        setOnClickListener(onClickListener);
    }

    private enum RenderingState
    {
        STOPPED,
        ACTIVE,
        PAUSED,
    }

    public void setSize(Size size)
    {
        synchronized (syncObj)
        {
            if(internalRenderingState != RenderingState.STOPPED)
            {
                throw new IllegalStateException("Cannot set size while renderer is active!");
            }

            //did they give us null?
            if(size == null)
            {
                //ugh, they did
                throw new IllegalArgumentException("size cannot be null!");
            }

            this.size = size;
            this.aspectRatio = (double)size.getWidth()/ (double)size.getHeight();

            //Make sure we don't have any mats hanging around
            //from when we might have been running before
            visionPreviewFrameQueue.clear();

            framebufferRecycler = new BitmapRecycler(FRAMEBUFFER_RECYCLER_CAPACITY, size.getWidth(), size.getHeight(), Bitmap.Config.ARGB_8888);
        }
    }

    public void post(Bitmap bmp)
    {
        synchronized (syncObj)
        {
            //did they give us null?
            if(bmp == null)
            {
                //ugh, they did
                throw new IllegalArgumentException("cannot post null bmp!");
            }

            //Are we actually rendering to the display right now? If not,
            //no need to waste time doing a memcpy
            if(/*internalRenderingState == RenderingState.ACTIVE*/ true)
            {
                /*
                 * We need to copy this mat before adding it to the queue,
                 * because the pointer that was passed in here is only known
                 * to be pointing to a certain frame while we're executing.
                 */
                try
                {
                    /*
                     * Grab a framebuffer Mat from the recycler
                     * instead of doing a new alloc and then having
                     * to free it after rendering/eviction from queue
                     */
                    BitmapRecycler.RecyclableBitmap bmpToCopyTo = framebufferRecycler.takeBmp();
                    BitmapContainer.copyAndroidBitmap(bmp, bmpToCopyTo.getBitmap());
                    visionPreviewFrameQueue.offer(bmpToCopyTo);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /*
     * Called with syncObj held
     */
    public void checkState()
    {
        /*
         * If the surface isn't ready, don't do anything
         */
        if(!surfaceExistsAndIsReady)
        {
            Log.d(TAG, "CheckState(): surface not ready or doesn't exist");
            return;
        }

        /*
         * Does the user want us to stop?
         */
        if(!userRequestedActive || needToDeactivateRegardlessOfUser)
        {
            if(needToDeactivateRegardlessOfUser)
            {
                Log.d(TAG, "CheckState(): lifecycle mandates deactivation regardless of user");
            }
            else
            {
                Log.d(TAG, "CheckState(): user requested that we deactivate");
            }

            /*
             * We only need to stop the render thread if it's not
             * already stopped
             */
            if(internalRenderingState != RenderingState.STOPPED)
            {
                Log.d(TAG, "CheckState(): deactivating viewport");

                /*
                 * Interrupt him so he's not stuck looking at his
                 * frame queue.
                 */
                renderThread.notifyExitRequested();
                renderThread.interrupt();

                /*
                 * Wait for him to die non-interuptibly
                 */
                renderThreadAliveLock.lock();
                renderThreadAliveLock.unlock();

//                try
//                {
//                    /*
//                     * Wait for him to die
//                     */
//                    renderThread.join();
//                }
//                catch (InterruptedException e)
//                {
//                    e.printStackTrace();
//                }

                internalRenderingState = RenderingState.STOPPED;
            }
            else
            {
                Log.d(TAG, "CheckState(): already deactivated");
            }
        }

        /*
         * Does the user want us to start?
         */
        else if(userRequestedActive)
        {
            Log.d(TAG, "CheckState(): user requested that we activate");

            /*
             * We only need to start the render thread if it's
             * stopped.
             */
            if(internalRenderingState == RenderingState.STOPPED)
            {
                Log.d(TAG, "CheckState(): activating viewport");

                internalRenderingState = RenderingState.PAUSED;

                if(userRequestedPause)
                {
                    internalRenderingState = RenderingState.PAUSED;
                }
                else
                {
                    internalRenderingState = RenderingState.ACTIVE;
                }

                renderThread = new RenderThread();
                renderThread.start();
            }
            else
            {
                Log.d(TAG, "CheckState(): already activated");
            }
        }

        if(internalRenderingState != RenderingState.STOPPED)
        {
            if(userRequestedPause && internalRenderingState != RenderingState.PAUSED
                    || !userRequestedPause && internalRenderingState != RenderingState.ACTIVE)
            {
                if(userRequestedPause)
                {
                    Log.d(TAG, "CheckState(): pausing viewport");
                    internalRenderingState = RenderingState.PAUSED;
                }
                else
                {
                    Log.d(TAG, "CheckState(): resuming viewport");
                    internalRenderingState = RenderingState.ACTIVE;
                }

                /*
                 * Interrupt him so that he's not stuck looking at his frame queue.
                 * (We stop filling the frame queue if the user requested pause so
                 * we aren't doing pointless memcpys)
                 */
                renderThread.interrupt();
            }
        }
    }

    /***
     * Activate the render thread
     */
    public synchronized void activate()
    {
        synchronized (syncObj)
        {
            userRequestedActive = true;
            checkState();
        }
    }

    /***
     * Deactivate the render thread
     */
    public void deactivate()
    {
        synchronized (syncObj)
        {
            userRequestedActive = false;
            checkState();
        }
    }

    public void resume()
    {
        synchronized (syncObj)
        {
            userRequestedPause = false;
            checkState();
        }
    }

    public void pause()
    {
        synchronized (syncObj)
        {
            userRequestedPause = true;
            checkState();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        Log.d(TAG, "surfaceCreated()");
        Log.d(TAG, "...surfaceCreated()");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        Log.d(TAG, "surfaceChanged()");

        synchronized (syncObj)
        {
            needToDeactivateRegardlessOfUser = false;
            surfaceExistsAndIsReady = true;

            checkState();
        }

        Log.d(TAG, "...surfaceChanged()");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        /*
         * NOTE: The docs for this method speak thusly:
         *     if you have a rendering thread that directly accesses the surface,
         *     you must ensure that thread is no longer touching the Surface before
         *     returning from this function.
         *
         * We handle this by waiting UNINTERRUPTIBLY for the render thread to exit
         * in checkState().
         */

        Log.d(TAG, "surfaceDestroyed()");

        synchronized (syncObj)
        {
            needToDeactivateRegardlessOfUser = true;
            checkState();
            surfaceExistsAndIsReady = false;
        }

        Log.d(TAG, "...surfaceDestroyed()");
    }

    class RenderThread extends Thread
    {
        boolean shouldPaintOrange = true;
        volatile boolean exitRequested = false;
        private String TAG = "OpenCvViewportRenderThread";

        public void notifyExitRequested()
        {
            exitRequested = true;
        }

        private Canvas lockCanvas()
        {
            return getHolder().lockCanvas();
        }

        private void swapBuffer(Canvas canvas)
        {
            getHolder().unlockCanvasAndPost(canvas);
        }

        @Override
        public void run()
        {
            renderThreadAliveLock.lock();

            //Make sure we don't have any mats hanging around
            //from when we might have been running before
            //hold 'syncObj' mutex to synchronize with post()!
            synchronized (syncObj)
            {
                for(BitmapRecycler.RecyclableBitmap bmp : visionPreviewFrameQueue)
                {
                    framebufferRecycler.returnBmp(bmp);
                }

                visionPreviewFrameQueue.clear();
            }

            Log.d(TAG, "Render thread is up!");

            canvas = lockCanvas();
            canvas.drawColor(Color.BLUE);
            swapBuffer(canvas);

            while (!exitRequested)
            {
                switch (internalRenderingState)
                {
                    case ACTIVE:
                    {
                        shouldPaintOrange = true;

                        BitmapRecycler.RecyclableBitmap bmp;

                        try
                        {
                            //Grab a Mat from the frame queue
                            bmp = visionPreviewFrameQueue.take();
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                            //Note: we actually don't re-interrupt ourselves here, because interrupts are also
                            //used to simply make sure we properly pick up a transition to the PAUSED state, not
                            //just when we're trying to close. If we're trying to close, then exitRequested will
                            //be set, and since we break immediately right here, the close will be handled cleanly.
                            //Thread.currentThread().interrupt();
                            break;
                        }

                        //Get canvas object for rendering on
                        canvas = lockCanvas();

                        /*
                         * For some reason, the canvas will very occasionally be null upon closing.
                         * Stack Overflow seems to suggest this means the canvas has been destroyed.
                         * However, surfaceDestroyed(), which is called right before the surface is
                         * destroyed, calls checkState(), which *SHOULD* block until we die. This
                         * works most of the time, but not always? We don't yet understand...
                         */
                        if(canvas != null)
                        {
                            //Draw the background each time to prevent double buffering problems
                            canvas.drawColor(Color.rgb(239,239,239)); // RC activity background color

                            drawOptimizingEfficiency(canvas, bmp.getBitmap());

                            swapBuffer(canvas);
                        }
                        else
                        {
                            Log.d(TAG, "Canvas was null");
                        }

                        //We're done with that Mat object; return it to the Mat recycler so it can be used again later
                        framebufferRecycler.returnBmp(bmp);

                        break;
                    }

                    case PAUSED:
                    {
                        if(shouldPaintOrange)
                        {
                            shouldPaintOrange = false;

                            canvas = lockCanvas();

                            /*
                             * For some reason, the canvas will very occasionally be null upon closing.
                             * Stack Overflow seems to suggest this means the canvas has been destroyed.
                             * However, surfaceDestroyed(), which is called right before the surface is
                             * destroyed, calls checkState(), which *SHOULD* block until we die. This
                             * works most of the time, but not always? We don't yet understand...
                             */
                            if(canvas != null)
                            {
                                canvas.drawColor(Color.rgb(255, 166, 0));
                                swapBuffer(canvas);
                            }
                        }

                        try
                        {
                            Thread.sleep(50);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                            //Note: we actually don't re-interrupt ourselves here, because interrupts are also
                            //used to simply make sure we properly pick up a transition to the PAUSED state, not
                            //just when we're trying to close. If we're trying to close, then exitRequested will
                            //be set, and since we break immediately right here, the close will be handled cleanly.
                            //Thread.currentThread().interrupt();
                        }
                        break;
                    }
                }
            }

            Log.d(TAG, "About to exit");
            renderThreadAliveLock.unlock();
        }

        void drawOptimizingEfficiency(Canvas canvas, Bitmap bmp)
        {
            /*
             * We need to draw minding the HEIGHT we have to work with; width is not an issue
             */
            if((canvas.getHeight() * aspectRatio) < canvas.getWidth())
            {
                // Height: we use the max we have, since vertical bounds are hit before horizontal bounds
                int scaledHeight = canvas.getHeight();

                // Width: calculate a scaled width assuming height is maxed for the canvas
                int scaledWidth = (int) Math.round(canvas.getHeight() * aspectRatio);

                // We want to center the image in the viewport
                int topLeftY = 0;
                int topLeftX = 0 + Math.abs(canvas.getWidth()-scaledWidth)/2;

                //Draw the bitmap, scaling it to the maximum size that will fit in the viewport
                Rect rect = createRect(
                        topLeftX,
                        topLeftY,
                        scaledWidth,
                        scaledHeight);

                // Draw black behind the bitmap to avoid alpha issues if usercode tries to draw
                // annotations and doesn't specify alpha 255. This wasn't an issue when we just
                // painted black behind the entire view, but now that we paint the RC background
                // color, it is an issue...
                canvas.drawRect(rect, paintBlackBackground);

                canvas.drawBitmap(
                        bmp,
                        null,
                        rect,
                        null
                );
            }

            /*
             * We need to draw minding the WIDTH we have to work with; height is not an issue
             */
            else
            {
                // Width: we use the max we have, since horizontal bounds are hit before vertical bounds
                int scaledWidth = canvas.getWidth();

                // Height: calculate a scaled height assuming width is maxed for the canvas
                int scaledHeight = (int) Math.round(canvas.getWidth() / aspectRatio);

                // We want to center the image in the viewport
                int topLeftY = Math.abs(canvas.getHeight()-scaledHeight)/2;
                int topLeftX = 0;

                //Draw the bitmap, scaling it to the maximum size that will fit in the viewport
                Rect rect = createRect(
                        topLeftX,
                        topLeftY,
                        scaledWidth,
                        scaledHeight);

                // Draw black behind the bitmap to avoid alpha issues if usercode tries to draw
                // annotations and doesn't specify alpha 255. This wasn't an issue when we just
                // painted black behind the entire view, but now that we paint the RC background
                // color, it is an issue...
                canvas.drawRect(rect, paintBlackBackground);

                canvas.drawBitmap(
                        bmp,
                        null,
                        rect,
                        null
                );
            }
        }
    }

    Rect createRect(int tlx, int tly, int w, int h)
    {
        return new Rect(tlx, tly, tlx+w, tly+h);
    }
}