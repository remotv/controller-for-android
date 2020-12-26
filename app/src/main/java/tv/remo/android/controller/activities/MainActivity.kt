package tv.remo.android.controller.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.btelman.controlsdk.enums.Operation
import org.btelman.controlsdk.hardware.components.CommunicationDriverComponent
import org.btelman.controlsdk.tts.SystemDefaultTTSComponent
import tv.remo.android.controller.R
import tv.remo.android.controller.ServiceInterface
import tv.remo.android.controller.databinding.ActivityMainBinding
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.components.RemoSocketComponent
import tv.remo.android.controller.sdk.components.StatusBroadcasterComponent
import tv.remo.android.controller.sdk.components.audio.RemoAudioProcessor
import tv.remo.android.controller.sdk.components.video.RemoVideoComponent

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var handler : Handler
    private lateinit var binding: ActivityMainBinding
    private var recording = false
    private var serviceInterface : ServiceInterface? = null

    private val onServiceStatus : (Operation) -> Unit = { serviceStatus ->
        binding.powerButton.let {
            binding.powerButton.setTextColor(parseColorForOperation(serviceStatus))
            val isLoading = serviceStatus == Operation.LOADING
            binding.powerButton.isEnabled = !isLoading
            if(isLoading) return@let //processing command. Disable button
            recording = serviceStatus == Operation.OK
            if(recording) {
                handleSleepLayoutTouch()
            }
            else{
                binding.remoChatView.keepScreenOn = false //go ahead and remove the flag
            }
        }
    }

    private val onServiceBind : (Operation) -> Unit = { serviceBoundState ->
        binding.powerButton.isEnabled = serviceBoundState == Operation.OK
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(Looper.getMainLooper())
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupControlSDK()
        setupUI()
        window.decorView.post {
            buildStatusList()
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.powerButton -> {
                if(recording)
                    serviceInterface?.changeStreamState(Operation.NOT_OK)
                else
                    serviceInterface?.changeStreamState(Operation.OK)
            }
            R.id.settingsButton -> launchSettings()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && recording) hideSystemUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceInterface?.destroy()
        serviceInterface = null
    }

    private fun setupControlSDK() {
        serviceInterface = ServiceInterface(this, onServiceBind, onServiceStatus)
        serviceInterface?.setup()
    }

    private fun setupUI() {
        binding.remoChatView.setOnTouchListener { _, _ ->
            handleSleepLayoutTouch()
            return@setOnTouchListener false
        }
        binding.settingsButton.setOnClickListener(this)
        binding.powerButton?.setOnClickListener(this)
    }

    private fun buildStatusList() {
        binding.websiteConnectionStatusView.registerStatusEvents(RemoSocketComponent::class.java)
        binding.hardwareConnectionStatusView.registerStatusEvents(CommunicationDriverComponent::class.java)
        binding.audioConnectionStatusView.registerStatusEvents(RemoAudioProcessor::class.java)
        binding.videoConnectionStatusView.registerStatusEvents(RemoVideoComponent::class.java)
        binding.ttsConnectionStatusView.registerStatusEvents(SystemDefaultTTSComponent::class.java)
        StatusBroadcasterComponent.sendUpdateBroadcast(applicationContext)
    }

    private fun handleSleepLayoutTouch(): Boolean {
        showSystemUI()
        RemoSettingsUtil.with(this){
            if(it.keepScreenOn.getPref()){
                binding.remoChatView.keepScreenOn = true //Could be attached to any view, but this is fine
            }
            if(it.hideScreenControls.getPref()){
                startSleepDelayed()
            }
        }
        return false
    }

    private fun startSleepDelayed() {
        binding.buttonGroupMainActivity.visibility = View.VISIBLE
        handler.removeCallbacks(hideScreenRunnable)
        handler.postDelayed(hideScreenRunnable, 10000) //10 second delay
    }

    private val hideScreenRunnable = Runnable {
        if (recording){
            binding.buttonGroupMainActivity.visibility = View.GONE
            hideSystemUI()
        }
    }

    private fun launchSettings() {
        serviceInterface?.changeStreamState(Operation.NOT_OK)
        startActivity(SettingsActivity.getIntent(this))
        finish()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }


    @Suppress("DEPRECATION")
    fun parseColorForOperation(state : Operation?) : Int{
        return when(state){
            Operation.OK -> resources.getColor(R.color.powerIndicatorOn)
            Operation.NOT_OK -> resources.getColor(R.color.powerIndicatorOff)
            Operation.LOADING -> resources.getColor(R.color.powerIndicatorInProgress)
            null -> resources.getColor(R.color.powerIndicatorError)
            else -> Color.BLACK
        }
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
    }

    companion object{
        fun getIntent(context: Context) : Intent {
            return Intent(context, MainActivity::class.java)
        }
    }
}
