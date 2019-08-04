package tv.remo.android.controller.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.btelman.controlsdk.enums.Operation
import org.btelman.controlsdk.hardware.components.HardwareComponent
import org.btelman.controlsdk.hardware.interfaces.HardwareDriver
import org.btelman.controlsdk.models.ComponentHolder
import org.btelman.controlsdk.streaming.components.AudioComponent
import org.btelman.controlsdk.streaming.components.VideoComponent
import org.btelman.controlsdk.streaming.enums.Orientation
import org.btelman.controlsdk.streaming.models.CameraDeviceInfo
import org.btelman.controlsdk.streaming.models.StreamInfo
import org.btelman.controlsdk.tts.SystemDefaultTTSComponent
import org.btelman.controlsdk.viewModels.ControlSDKViewModel
import tv.remo.android.controller.R
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.components.HardwareWatchdogComponent
import tv.remo.android.controller.sdk.components.RemoSocketComponent

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var recording = false
    private val arrayList = ArrayList<ComponentHolder<*>>()
    private var controlSDKViewModel: ControlSDKViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settingsButton.setOnClickListener(this)

        controlSDKViewModel = ControlSDKViewModel.getObject(this)
        controlSDKViewModel?.setStatusObserver(this, operationObserver)
        controlSDKViewModel?.setServiceBoundListener(this){ connected ->
            powerButton.isEnabled = connected == Operation.OK
        }
        createComponentHolders()
        powerButton?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.powerButton -> powerCycle()
            R.id.settingsButton -> launchSettings()
        }
    }

    val operationObserver : (Operation) -> Unit = { serviceStatus ->
        powerButton?.let {
            powerButton.setTextColor(parseColorForOperation(serviceStatus))
            val isLoading = serviceStatus == Operation.LOADING
            powerButton.isEnabled = !isLoading
            if(isLoading) return@let //processing command. Disable button
            recording = serviceStatus == Operation.OK
            /*if(recording && settings.autoHideMainControls.value)
                startSleepDelayed()*/
        }
    }

    private fun launchSettings() {
        controlSDKViewModel?.api?.disable()
        startActivity(SettingsActivity.getIntent(this))
        finish()
    }

    private fun powerCycle() {
        when(controlSDKViewModel?.api?.getServiceStateObserver()?.value){
            Operation.NOT_OK -> {
                arrayList.forEach {
                    controlSDKViewModel?.api?.attachToLifecycle(it)
                }
                controlSDKViewModel?.api?.enable()
            }
            Operation.LOADING -> {} //do nothing
            Operation.OK -> {
                arrayList.forEach {
                    controlSDKViewModel?.api?.detachFromLifecycle(it)
                }
                controlSDKViewModel?.api?.disable()
            }
            null -> powerButton.setTextColor(parseColorForOperation(null))
        }
    }

    private fun createComponentHolders() {
        RemoSettingsUtil.with(this){ settings ->

            val remoSocket = ComponentHolder(RemoSocketComponent::class.java,
                RemoSocketComponent.createBundle(settings.apiKey.getPref(), settings.channelId.getPref()))
            arrayList.add(remoSocket)

            if(settings.textToSpeechEnabled.getPref()){
                val tts = ComponentHolder(SystemDefaultTTSComponent::class.java, null)
                arrayList.add(tts)
            }

            val steamingBundle = Bundle().apply {
                val channel = settings.channelId.getPref()
                val streamInfo = StreamInfo(
                    "http://dev.remo.tv:1567/transmit?name=$channel-video",
                    "http://dev.remo.tv:1567/transmit?name=$channel-audio"
                    ,deviceInfo = CameraDeviceInfo.fromCamera(0)
                    ,orientation = Orientation.valueOf(settings.cameraOrientation.getPref())
                )
                streamInfo.addToExistingBundle(this)
            }

            if(settings.cameraEnabled.getPref()){
                val videoComponent = ComponentHolder(VideoComponent::class.java, steamingBundle)
                arrayList.add(videoComponent)
            }

            if(settings.microphoneEnabled.getPref()){
                val audioComponent = ComponentHolder(AudioComponent::class.java, steamingBundle)
                arrayList.add(audioComponent)
            }

            if(settings.robotSettingsEnable.getPref()){
                val hardwareBundle = Bundle().apply {
                    putSerializable(HardwareDriver.BUNDLE_ID, settings.robotCommunicationDriver.getPref())
                    putSerializable(HardwareComponent.HARDWARE_TRANSLATOR_BUNDLE_ID, settings.robotProtocolTranslator.getPref())
                }
                val hardwareComponent = ComponentHolder(HardwareComponent::class.java, hardwareBundle)
                arrayList.add(hardwareComponent)
            }
            arrayList.add(ComponentHolder(HardwareWatchdogComponent::class.java, null))
        }
    }

    companion object{
        fun getIntent(context: Context) : Intent {
            return Intent(context, MainActivity::class.java)
        }

        fun parseColorForOperation(state : Operation?) : Int{
            val color : Int = when(state){
                Operation.OK -> Color.GREEN
                Operation.NOT_OK -> Color.RED
                Operation.LOADING -> Color.YELLOW
                null -> Color.CYAN
                else -> Color.BLACK
            }
            return color
        }
    }
}
