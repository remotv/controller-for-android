package org.btelman.controlsdk.streaming
import android.os.Bundle
import androidx.test.runner.AndroidJUnit4
import org.btelman.controlsdk.streaming.audio.retrievers.BaseAudioRetriever
import org.btelman.controlsdk.streaming.audio.retrievers.BasicMicrophoneAudioRetriever
import org.btelman.controlsdk.streaming.factories.AudioRetrieverFactory
import org.btelman.controlsdk.streaming.models.AudioPacket
import org.btelman.controlsdk.streaming.models.StreamInfo
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class AudioRetrieverFactoryAndroidTest {
    @Test
    fun findRetrieverTest() {
        Assert.assertTrue(AudioRetrieverFactory.findRetriever(Bundle()) is BasicMicrophoneAudioRetriever)

        //testing default parameters. Should use eithe
        val streamInfo = StreamInfo("http://example.com:3000", "audioHttpWhatever")
        val retriever = AudioRetrieverFactory.findRetriever(streamInfo.toBundle())
        Assert.assertTrue(retriever is BasicMicrophoneAudioRetriever)
        //test custom class
        val bundle = Bundle()
        streamInfo.addToExistingBundle(bundle)
        AudioRetrieverFactory.putClassInBundle(MockAudioRetriever::class.java, bundle)
        Assert.assertTrue(AudioRetrieverFactory.findRetriever(bundle) is MockAudioRetriever)
    }

    @Test
    fun testAddClazzToBundle(){
        val bundle = Bundle()
        val clazz = MockAudioRetriever::class.java
        AudioRetrieverFactory.putClassInBundle(clazz, bundle)
        Assert.assertEquals(clazz, AudioRetrieverFactory.getClassFromBundle(bundle))
    }

    class MockAudioRetriever : BaseAudioRetriever() {
        override fun retrieveAudioByteArray(): AudioPacket? {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}
