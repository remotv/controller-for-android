package tv.remo.android.controller.sdk

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import tv.remo.android.controller.sdk.utils.isUrl

/**
 * Test the StringUtilTest kotlin String extensions
 */
class StringUtilTest {
    @Test
    fun testIsUrl(){
        assertTrue("http://remo.tv".isUrl())
        assertTrue("https://remo.tv".isUrl())
        assertTrue("hehe, check this link: http://www.example.com/".isUrl())
        assertTrue("https://remo.tv:5000".isUrl())
        assertTrue("https://example.com".isUrl())
        assertTrue("http://example.com".isUrl())
        assertTrue("www.example.com".isUrl())
        assertTrue("http://www.example".isUrl())

        //ones that should not trigger
        assertFalse("remo.tv".isUrl())
        assertFalse("remo.tv:5000".isUrl())
        assertFalse("Lorem.Ipsum".isUrl())
        assertFalse("Lorem.Ipsum.A".isUrl())
        assertFalse("Lorem Ipsum".isUrl())
        assertFalse("[[aaaaaaaaaaaaaaa".isUrl())
        assertFalse("test".isUrl())
        assertFalse("lorem.ipsum".isUrl())
        assertFalse("example.com".isUrl())
        assertFalse("remo.tv:5000".isUrl())
        assertFalse("http".isUrl())
        assertFalse("https".isUrl())
        assertFalse("https://".isUrl())
        assertFalse("http://example".isUrl())
        assertFalse("http://example.".isUrl())
    }
}
/**
ReconDelta090: test
ReconDelta090: lorem.ipsum
ReconDelta090: example.com
ReconDelta090: https://example.com
ReconDelta090: www.example.com
ReconDelta090: http://example.com
ReconDelta090: [[aaaaaaaaa
ReconDelta090: remo.tv:5000
ReconDelta090: http
ReconDelta090: https
ReconDelta090: https://
ReconDelta090: http://example
ReconDelta090: http://example.
ReconDelta090: http://www.example
 */