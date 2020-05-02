package tv.remo.android.controller.suite

import org.junit.runner.RunWith
import org.junit.runners.Suite
import tv.remo.android.controller.DemoBotAndroidTest
import tv.remo.android.controller.ExampleInstrumentedTest

// Runs all unit tests.
@RunWith(Suite::class)
@Suite.SuiteClasses(
        DemoBotAndroidTest::class,
        ExampleInstrumentedTest::class)
class InstrumentationTestSuite