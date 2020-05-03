package tv.remo.android.controller

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import org.btelman.controlsdk.enums.Operation
import org.btelman.controlsdk.models.ComponentHolder
import org.btelman.controlsdk.services.ControlSDKService
import org.btelman.controlsdk.streaming.factories.VideoRetrieverFactory
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.components.StatusBroadcasterComponent
import tv.remo.android.controller.sdk.components.video.RemoVideoComponent
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class DemoBotAndroidTest {
    @get:Rule
    val serviceRule = ServiceTestRule()
    lateinit var serviceInterface : ServiceInterface
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    var serviceConnectionListener : ((Operation)->Unit)? = null
    var serviceBoundListener : ((Operation)->Unit)? = null
    @Test
    fun testAndroidStream() {
        serviceRule.startService(
            Intent(appContext, ControlSDKService::class.java)
        )
        val handler = Handler(Looper.getMainLooper())
        RemoSettingsUtil.with(appContext){
            it.apiKey.savePref(BuildConfig.robot_test_key)
            it.channelId.savePref("API${Build.VERSION.SDK_INT}")
            it.cameraEnabled.savePref(true)
            it.microphoneEnabled.savePref(false)
            it.cameraResolution.savePref("640x480")
            it.ffmpegInputOptions.savePref("-f image2pipe -codec:v mjpeg -i -")
        }

        serviceInterface = ServiceInterfaceOverride(appContext,
            { serviceBound -> serviceBoundListener?.invoke(serviceBound)},
            { serviceConnection -> serviceConnectionListener?.invoke(serviceConnection)}
        )

        val serviceStatusLatch = CountDownLatch(1)
        handler.postDelayed({
            serviceConnectionListener = { serviceConnection ->
                if(serviceConnection == Operation.OK)
                    serviceStatusLatch.countDown()
                serviceConnectionListener = null
            }
            serviceBoundListener = { serviceBound ->
                if(serviceBound == Operation.OK){
                    serviceInterface.changeStreamState(Operation.OK)
                }
            }
            serviceInterface.setup()
        }, 1000)
        serviceStatusLatch.await(1, TimeUnit.MINUTES) //probably too long, but after this it is long dead
        Thread.sleep(5*60000) //5 minutes
        val serviceStatusCloseLatch = CountDownLatch(1)
        handler.post {
            serviceConnectionListener = { serviceStatus ->
                if (serviceStatus == Operation.NOT_OK)
                    serviceStatusCloseLatch.countDown()
                serviceConnectionListener = null
            }
            serviceInterface.changeStreamState(Operation.NOT_OK)
        }
        serviceStatusCloseLatch.await(1, TimeUnit.MINUTES)
    }

    class ServiceInterfaceOverride(
        context: Context,
        onServiceBind: (Operation) -> Unit,
        onServiceStateChange: (Operation) -> Unit
    ) : ServiceInterface(context, onServiceBind, onServiceStateChange) {
        override fun createComponentHolders() {
            super.createComponentHolders()
            listenerControllerList.add(ComponentHolder(StatusBroadcasterComponent::class.java, null))
            injectDemoVideoRetriever(arrayList)
        }

        /**
         * We are injecting our own video component. Need to replace the class with our test class,
         * but keep properties
         *
         * 1. Delete old class holder from the list, cache the holder
         * 2. Add our demo holder
         * 3. Add old properties to holder
         * 4. Add our demo retriever to holder properties
         */
        fun injectDemoVideoRetriever(arrayList: ArrayList<ComponentHolder<*>>) {
            var holder : ComponentHolder<*>? = null
            arrayList.reversed().forEach {
                if(it.clazz == RemoVideoComponent::class.java){
                    //we want to inject a custom video retriever/provider. Remove the old object
                    holder = it.data?.let { bundle ->
                        VideoRetrieverFactory.putClassInBundle(
                            DemoSurfaceVideoComponent::class.java,
                            bundle
                        )
                        arrayList.remove(it)
                        it
                    }

                }
            }

            //modify the bundle and put it back in the arrayList. A little ugly but gets the job done
            arrayList.add(
                ComponentHolder(
                    DemoVideoComponent::class.java,
                    (holder?.data ?: Bundle()).also { //it : Bundle
                        VideoRetrieverFactory.putClassInBundle(
                            DemoSurfaceVideoComponent::class.java,
                            it
                        )
                    }
                )
            )
        }
    }
}
