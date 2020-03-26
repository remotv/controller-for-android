package tv.remo.android.controller

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import androidx.test.runner.AndroidJUnit4
import org.btelman.controlsdk.enums.Operation
import org.btelman.controlsdk.interfaces.ControlSdkServiceWrapper
import org.btelman.controlsdk.models.ComponentHolder
import org.btelman.controlsdk.services.ControlSDKService
import org.btelman.controlsdk.services.ControlSDKServiceConnection
import org.btelman.controlsdk.services.observeAutoCreate
import org.btelman.controlsdk.streaming.components.VideoComponent
import org.btelman.controlsdk.streaming.factories.VideoRetrieverFactory
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.components.StatusBroadcasterComponent
import tv.remo.android.controller.sdk.components.video.RemoVideoComponent
import tv.remo.android.controller.sdk.utils.ComponentBuilderUtil
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

    private val listenerControllerList = ArrayList<ComponentHolder<*>>()
    private val arrayList = ArrayList<ComponentHolder<*>>()

    private lateinit var controlSDKServiceApi: ControlSdkServiceWrapper
    private val lifecycleOwner: LifecycleOwner = let{
        val owner: LifecycleOwner =
            mock(LifecycleOwner::class.java)
        val lifecycle = LifecycleRegistry(owner)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        `when`(owner.lifecycle).thenReturn(lifecycle)
        return@let owner
    }
    val appContext = InstrumentationRegistry.getTargetContext()

    @Test
    fun TestAndroidStream() {
        serviceRule.startService(
            Intent(InstrumentationRegistry.getTargetContext(), ControlSDKService::class.java)
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

        controlSDKServiceApi = ControlSDKServiceConnection.getNewInstance(appContext)
        val serviceStatusLatch = CountDownLatch(1)
        handler.postDelayed({
            controlSDKServiceApi.getServiceStateObserver().observeAutoCreate(lifecycleOwner){ serviceStatus ->
                if(serviceStatus == Operation.OK)
                    serviceStatusLatch.countDown()
            }
            controlSDKServiceApi.getServiceBoundObserver().observeAutoCreate(lifecycleOwner){ connected ->
                handleListenerAddOrRemove(connected)
                if(connected == Operation.OK){
                    changeStreamState(Operation.OK)
                }
            }
            controlSDKServiceApi.connectToService()
            createComponentHolders()
        }, 1000)
        serviceStatusLatch.await(1, TimeUnit.MINUTES) //probably too long, but after this it is long dead
        Thread.sleep(60000)
        val serviceStatusCloseLatch = CountDownLatch(1)
        handler.post {
            controlSDKServiceApi.getServiceStateObserver()
                .observeAutoCreate(lifecycleOwner) { serviceStatus ->
                    if (serviceStatus == Operation.OK)
                        serviceStatusCloseLatch.countDown()
                }
            changeStreamState(Operation.NOT_OK)
        }
        serviceStatusCloseLatch.await(1, TimeUnit.MINUTES)
    }

    private fun createComponentHolders() {
        RemoSettingsUtil.with(appContext){ settings ->
            arrayList.add(ComponentBuilderUtil.createSocketComponent(settings))
            arrayList.addAll(ComponentBuilderUtil.createTTSComponents(settings))
            arrayList.addAll(ComponentBuilderUtil.createStreamingComponents(settings))
            arrayList.addAll(ComponentBuilderUtil.createHardwareComponents(settings))
            listenerControllerList.add(ComponentHolder(StatusBroadcasterComponent::class.java, null))
            injectDemoVideoRetriever(arrayList)
        }
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
    private fun injectDemoVideoRetriever(arrayList: ArrayList<ComponentHolder<*>>) {
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

    private fun handleListenerAddOrRemove(connected : Operation) {
        if(connected == Operation.OK){
            listenerControllerList.forEach {
                controlSDKServiceApi?.addListenerOrController(it)
            }
        }
    }

    private fun changeStreamState(desiredState : Operation) {
        if(controlSDKServiceApi?.getServiceStateObserver()?.value == desiredState) return //already active
        when(desiredState){
            Operation.OK -> {
                arrayList.forEach {
                    controlSDKServiceApi?.attachToLifecycle(it)
                }
                controlSDKServiceApi?.enable()
            }
            Operation.LOADING -> {} //do nothing
            Operation.NOT_OK -> {
                //disable the service
                controlSDKServiceApi?.disable()
                // remove all components that happen to be left over. We may not know what got added
                // if the activity was lost due to the Android system
                // Listeners and controllers will still stay, and will not be overridden by the same name
                controlSDKServiceApi?.reset()
            }
        }
    }
}
