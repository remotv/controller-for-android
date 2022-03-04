package org.firstinspires.ftc.smem.common;

import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;

import org.openftc.ashmem.AshmemRegion;

public class SharedMemoryBitmap
{
    private AshmemRegion ashmemRegion;
    private BitmapContainer bitmapContainer;

    public SharedMemoryBitmap(int width, int height, int format, String dbgName)
    {
        ashmemRegion = new AshmemRegion(dbgName, width*height*4);
        bitmapContainer = new BitmapContainer(width, height ,format, ashmemRegion.getPtr());
    }

    public SharedMemoryBitmap(ParcelFileDescriptor fd, int width, int height)
    {
        ashmemRegion = new AshmemRegion(fd);
        bitmapContainer = new BitmapContainer(width, height, -1, ashmemRegion.getPtr());
    }

    public ParcelFileDescriptor getParcelFd()
    {
        return ashmemRegion.getParcelFileDescriptor();
    }

    public long nObj()
    {
        return bitmapContainer.nObj();
    }

    public void fillAndroidBmp(Bitmap bitmap)
    {
        bitmapContainer.fillAndroidBmp(bitmap);
    }

    static
    {
        System.loadLibrary("common");
    }
}
