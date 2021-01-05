package org.btelman.controlsdk.streaming

/**
 * Created by Brendon on 8/31/2019.
 */
import org.junit.runner.RunWith
import org.junit.runners.Suite

// Runs all unit tests.
@RunWith(Suite::class)
@Suite.SuiteClasses(
    AudioProcessorFactoryAndroidTest::class,
    AudioRetrieverFactoryAndroidTest::class,
    VideoProcessorFactoryAndroidTest::class,
    VideoRetrieverFactoryAndroidTest::class)
class UnitTestSuite