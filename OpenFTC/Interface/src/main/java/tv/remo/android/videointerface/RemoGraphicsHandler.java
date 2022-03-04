package tv.remo.android.videointerface;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class RemoGraphicsHandler extends Service
{
    private final IBinder mBinder = new RemoteServiceImpl();

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        System.out.println("tv.remo.android.videointerface.RemoGraphicsHandler::onBind");

        // Return the interface
        return mBinder;
    }
}