package org.firstinspires.smemserver;

import org.firstinspires.smemserver.IpcImgStreamerCallback;

interface IpcImgStreamerInterface
{
    ParcelFileDescriptor[] configureBuffers(int width, int height);
    void notifyActiveBuffer(int buf);
    void registerCallback(IpcImgStreamerCallback callback);
}
