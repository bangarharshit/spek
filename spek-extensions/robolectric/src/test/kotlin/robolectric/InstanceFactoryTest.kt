package robolectric

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.internal.SdkEnvironment
import org.robolectric.manifest.AndroidManifest
import org.robolectric.shadows.ShadowIntent
import org.spekframework.spek2.CreateWith
import org.spekframework.spek2.Spek
import org.spekframework.spek2.lifecycle.InstanceFactory
import org.spekframework.spek2.robolectric.RobolectricInstanceFactory
import org.spekframework.spek2.robolectric.RobolectricLifeCycleListener
import org.spekframework.spek2.runtime.test.AbstractSpekRuntimeTest

class RobolectricInstanceFactoryTest : AbstractSpekRuntimeTest() {

    object SampleInstanceFactoryTest : InstanceFactory by RobolectricInstanceFactory

    @Test
    fun testUsingRobolectric() {
        @CreateWith(SampleInstanceFactoryTest::class)
        @Config(sdk = intArrayOf(21))
        class RobolectricSpek(val sdkEnvironment: SdkEnvironment, val config: Config, val androidManifest: AndroidManifest ): Spek({
            lateinit var activity: TestActivity1
            registerListener(RobolectricLifeCycleListener(sdkEnvironment, config, androidManifest))
            beforeEachTest {

                activity = Robolectric.buildActivity(TestActivity1::class.java).get()
            }
            it("The activity started on setup") {

                val shadowActivity = Shadows.shadowOf(activity)

                assertFalse(shadowActivity.isFinishing)
            }
            it("The activity terminated on finish") {
                val shadowActivity = Shadows.shadowOf(activity)
                activity.finish()
                assertTrue(shadowActivity.isFinishing)
            }
            it ("starts the next activity when invoked") {
                val shadowActivity = Shadows.shadowOf(activity)
                activity.startNextActivity()
                val shadowIntent = Shadows.shadowOf(shadowActivity.nextStartedActivity)
                assertEquals(TestActivity2::class.java, shadowIntent.intentClass)
            }
        })

        val recorder = executeTestsForClass(RobolectricSpek::class)
        println(recorder.testStartedCount)
        println(recorder.testSuccessfulCount)
        assertThat(recorder.testSuccessfulCount, equalTo(3))
    }

    class TestActivity1: Activity() {

        fun startNextActivity() {
            val intent = Intent(this, TestActivity2::class.java)
            startActivity(intent)
        }
    }

    class TestActivity2: Activity() { }

}
