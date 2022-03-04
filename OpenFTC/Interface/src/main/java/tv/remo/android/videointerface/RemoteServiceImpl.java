package tv.remo.android.videointerface;

import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import org.firstinspires.ftc.smem.common.SharedMemoryBitmap;
import org.firstinspires.smemserver.IpcImgStreamerCallback;
import org.firstinspires.smemserver.IpcImgStreamerInterface;

public class RemoteServiceImpl extends IpcImgStreamerInterface.Stub
{
    SharedMemoryBitmap[] sharedMemoryBitmaps = new SharedMemoryBitmap[2];
    IpcImgStreamerCallback callback;
    boolean buffersConfigured = false;

    private volatile int activeBuf = -1;

    @Override
    public synchronized ParcelFileDescriptor[] configureBuffers(int width, int height)
    {
        System.out.println("Configuring buffers");

        sharedMemoryBitmaps[0] = new SharedMemoryBitmap(width, height, -1, "bmp0");
        sharedMemoryBitmaps[1] = new SharedMemoryBitmap(width, height, -1, "bmp1");

        System.out.println("Buffers configured!");
        buffersConfigured = true;

        return new ParcelFileDescriptor[] {sharedMemoryBitmaps[0].getParcelFd(), sharedMemoryBitmaps[1].getParcelFd()};
    }

    @Override
    public synchronized void notifyActiveBuffer(int buf) throws RemoteException
    {
        activeBuf = buf;
    }

    public synchronized boolean captureActiveBufferToBmp(Bitmap bitmap)
    {
        if(!buffersConfigured || activeBuf < 0)
        {
            return false;
        }

        sharedMemoryBitmaps[activeBuf].fillAndroidBmp(bitmap);


        try
        {
            callback.notifyBufferReturn(activeBuf);
            return true;
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void registerCallback(IpcImgStreamerCallback callback) throws RemoteException
    {
        this.callback = callback;
    }
}
