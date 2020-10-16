package org.btelman.controlsdk.streaming
import android.os.Bundle
import androidx.test.runner.AndroidJUnit4
import org.btelman.controlsdk.streaming.factories.VideoProcessorFactory
import org.btelman.controlsdk.streaming.models.ImageDataPacket
import org.btelman.controlsdk.streaming.models.StreamInfo
import org.btelman.controlsdk.streaming.video.processors.BaseVideoProcessor
import org.btelman.controlsdk.streaming.video.processors.FFmpegVideoProcessor
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class VideoProcessorFactoryAndroidTest {
    @Test
    fun findProcessorTest() {
        Assert.assertTrue(VideoProcessorFactory.findProcessor(Bundle()) is FFmpegVideoProcessor)

        val streamInfo = StreamInfo("http://example.com:3000") //testing default parameters. Should use either Camera1SurfaceTextureComponent or Camera2SurfaceTextureComponent
        //test custom class
        val bundle = Bundle()
        streamInfo.addToExistingBundle(bundle)
        VideoProcessorFactory.putClassInBundle(MockVideoProcessor::class.java, bundle)
        Assert.assertTrue(VideoProcessorFactory.findProcessor(bundle) is MockVideoProcessor)
    }

    @Test
    fun testAddClazzToBundle(){
        val bundle = Bundle()
        val clazz = MockVideoProcessor::class.java
        VideoProcessorFactory.putClassInBundle(clazz, bundle)
        Assert.assertEquals(clazz, VideoProcessorFactory.getClassFromBundle(bundle))
    }

    class MockVideoProcessor : BaseVideoProcessor() {
        override fun processData(packet: ImageDataPacket) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}
