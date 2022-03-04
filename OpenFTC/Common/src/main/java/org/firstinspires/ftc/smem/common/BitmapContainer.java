package org.firstinspires.ftc.smem.common;

import android.graphics.Bitmap;

public class BitmapContainer
{
    private long n_obj;

    public BitmapContainer(int width, int height, int format, long ptrBuf)
    {
        n_obj = n_BitmapContainer(width, height, format, ptrBuf);
    }

    public void fillAndroidBmp(Bitmap bitmap)
    {
        fillAndroidBmpFromContainer(bitmap, n_obj);
    }

    public long nObj()
    {
        return n_obj;
    }

    @Override
    public void finalize()
    {
        n_del_BitmapContainer(n_obj);
    }

    private static native long n_BitmapContainer(int width, int height, int format, long ptrBuf);
    private static native void n_del_BitmapContainer(long ptr);
    private static native void fillAndroidBmpFromContainer(Bitmap bitmap, long ptrContainer);

    public static native void copyAndroidBitmap(Bitmap src, Bitmap dst);

    static
    {
        System.loadLibrary("common");
    }
}
