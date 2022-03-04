package org.btelman.controlsdk.streaming.video.retrievers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.os.IBinder
import android.util.Log
import org.btelman.controlsdk.streaming.models.ImageDataPacket
import tv.remo.android.videointerface.RemoGraphicsHandler
import tv.remo.android.videointerface.RemoteServiceImpl

class AshmemRetriever : BaseVideoRetriever() {
    private var bitmap: Bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888)


    override fun enableInternal() {
        super.enableInternal()
        val intent = Intent(context!!, RemoGraphicsHandler::class.java)
        context!!.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    override fun disableInternal() {
        super.disableInternal()
        context!!.unbindService(mConnection)
    }

    private var remoteService: RemoteServiceImpl? = null
    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val impl: RemoteServiceImpl = service as RemoteServiceImpl
            Log.d("AshmemRetriever", "onServiceConnected()")
            remoteService = impl
        }

        // Called when the connection with the service disconnects unexpectedly
        override fun onServiceDisconnected(className: ComponentName) {
            remoteService = null
            Log.e("client", "Service has unexpectedly disconnected")
        }
    }

    override fun grabImageData(): ImageDataPacket {
        remoteService?.captureActiveBufferToBmp(bitmap)
        return ImageDataPacket(bitmap, ImageFormat.JPEG)
    }
}