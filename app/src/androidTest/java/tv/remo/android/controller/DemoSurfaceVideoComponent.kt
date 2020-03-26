package tv.remo.android.controller

import android.graphics.*
import android.view.Surface
import org.btelman.controlsdk.streaming.models.ImageDataPacket
import org.btelman.controlsdk.streaming.video.retrievers.BaseVideoRetriever
import org.btelman.controlsdk.streaming.video.retrievers.SurfaceTextureVideoRetriever
import org.btelman.controlsdk.streaming.video.retrievers.api16.Camera1SurfaceTextureComponent
import kotlin.math.cos
import kotlin.math.sin

/**
 * Created by Brendon on 3/25/2020.
 *
 * Run the video demo since we will not be able to use a camera
 */
class DemoSurfaceVideoComponent : BaseVideoRetriever() {

    private var bitmap: Bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.RGB_565)
    private var canvas = Canvas(bitmap)
    private var x = 0f
    private var t = 0f

    override fun grabImageData(): ImageDataPacket? {
        canvas.drawColor(Color.RED)
        canvas.drawCircle(x, 240*sin(t/160f)+240, 10f, Paint(Color.BLUE))
        canvas.drawCircle(x, 20f, 10f, Paint(Color.GREEN))
        x += .1f
        t += .1f
        if(x > 640)
            x = 0f
        return ImageDataPacket(
            bitmap,
            ImageFormat.JPEG
        )
    }
}