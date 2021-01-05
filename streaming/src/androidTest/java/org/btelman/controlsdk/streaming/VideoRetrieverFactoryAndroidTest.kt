package org.btelman.controlsdk.streaming
import android.os.Bundle
import androidx.test.runner.AndroidJUnit4
import org.btelman.controlsdk.streaming.factories.VideoRetrieverFactory
import org.btelman.controlsdk.streaming.models.CameraDeviceInfo
import org.btelman.controlsdk.streaming.models.ImageDataPacket
import org.btelman.controlsdk.streaming.models.StreamInfo
import org.btelman.controlsdk.streaming.video.retrievers.BaseVideoRetriever
import org.btelman.controlsdk.streaming.video.retrievers.CameraCompatRetriever
import org.btelman.controlsdk.streaming.video.retrievers.api16.Camera1SurfaceTextureComponent
import org.btelman.controlsdk.streaming.video.retrievers.api21.Camera2Component
import org.junit.Assert
import org.junit.Assume
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class VideoRetrieverFactoryAndroidTest {
    @Test
    fun findRetrieverTest() {
        Assert.assertTrue(VideoRetrieverFactory.findRetriever(Bundle()) is CameraCompatRetriever)

        val streamInfo = StreamInfo("http://example.com:3000") //testing default parameters. Should use either Camera1SurfaceTextureComponent or Camera2SurfaceTextureComponent
        val camera = VideoRetrieverFactory.findRetriever(streamInfo.toBundle())
        if(camera !is Camera1SurfaceTextureComponent && camera !is Camera2Component && camera !is CameraCompatRetriever)
            Assert.fail()

        //test custom class
        val bundle = Bundle()
        streamInfo.addToExistingBundle(bundle)
        VideoRetrieverFactory.putClassInBundle(MockVideoRetriever::class.java, bundle)
        Assert.assertTrue(VideoRetrieverFactory.findRetriever(bundle) is MockVideoRetriever)
    }

    @Test
    fun testAddClazzToBundle(){
        val bundle = Bundle()
        val clazz = MockVideoRetriever::class.java
        VideoRetrieverFactory.putClassInBundle(clazz, bundle)
        Assert.assertEquals(clazz, VideoRetrieverFactory.getClassFromBundle(bundle))
    }

    @Test
    fun testAPI21Camera(){
        Assume.assumeTrue(Camera2Component.isSupported())
        val streamInfo = StreamInfo("http://example.com:3000",
            deviceInfo = CameraDeviceInfo.fromCamera(0))
        val camera = VideoRetrieverFactory.findRetriever(streamInfo.toBundle())
        Assert.assertTrue(camera is CameraCompatRetriever)
    }

    class MockVideoRetriever : BaseVideoRetriever() {
        override fun grabImageData(): ImageDataPacket? {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}
