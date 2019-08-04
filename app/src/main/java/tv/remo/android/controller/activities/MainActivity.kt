package tv.remo.android.controller.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.btelman.controlsdk.enums.Operation
import org.btelman.controlsdk.models.ComponentHolder
import org.btelman.controlsdk.viewModels.ControlSDKViewModel
import tv.remo.android.controller.R
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.utils.ComponentBuilderUtil

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
            arrayList.add(ComponentBuilderUtil.createSocketComponent(settings))
            arrayList.addAll(ComponentBuilderUtil.createTTSComponents(settings))
            arrayList.addAll(ComponentBuilderUtil.createStreamingComponents(settings))
            arrayList.addAll(ComponentBuilderUtil.createHardwareComponents(settings))
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
