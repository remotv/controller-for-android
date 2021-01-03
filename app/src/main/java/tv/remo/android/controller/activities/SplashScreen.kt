package tv.remo.android.controller.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import org.btelman.controlsdk.hardware.drivers.BluetoothClassicDriver
import org.btelman.controlsdk.hardware.interfaces.DriverComponent
import org.btelman.controlsdk.hardware.interfaces.HardwareDriver
import org.btelman.controlsdk.services.ControlSDKService
import org.btelman.controlsdk.utils.ClassScanner
import tv.remo.android.controller.R
import tv.remo.android.controller.databinding.ActivitySplashScreenBinding
import tv.remo.android.controller.sdk.RemoSettingsUtil

class SplashScreen : FragmentActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private var permissionsAlreadyRequested = false
    private var timeAtStart = System.currentTimeMillis()
    var classScanComplete = false
    val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.remoSettingsSplashButton.setOnClickListener(this::startSetup)
        binding.managePermissionsSplashButton.setOnClickListener(this::launchPermissions)

        detectIntentUpdateSettings(intent)
        runOnUiThread{
            var needsSetup = false
            RemoSettingsUtil.with(this){ settings ->
                needsSetup = settings.apiKey.let {
                    it.defaultValue == it.getPref()
                }
            }
            if(needsSetup){
                Toast.makeText(this,
                    "APIKey required to run", Toast.LENGTH_SHORT).show()
                startSetup()
            }
            else
                next()
        }
    }

    private fun detectIntentUpdateSettings(intent: Intent) {
        val tokenSettingsKey = getString(R.string.connectionApiTokenKey)
        val channelIdSettingsKey = getString(R.string.connectionChannelIdKey)
        val token = intent.getStringExtra(tokenSettingsKey)
        val channelId = intent.getStringExtra(channelIdSettingsKey)
        if(token != null && channelId != null){
            PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().apply {
                putString(channelIdSettingsKey, channelId)
                putString(tokenSettingsKey, token)
            }.apply()
            Toast.makeText(this,
                "APIKey and Channel ID updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startSetup(view : View? = null) {
        startActivity(SettingsActivity.getIntent(this))
        finish()
    }

    private fun launchPermissions(view : View? = null){
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        })
    }

    private fun next() {
        binding.progressSplash.visibility = View.VISIBLE
        binding.permissionsRequest.visibility = View.GONE
        if(!classScanComplete){
            Thread{
                ClassScanner.getClasses(this)
                runOnUiThread {
                    @Suppress("DeferredResultUnused")
                    classScanComplete = true
                    next()
                }
            }.start()
            return
        }

        //Check permissions. break out if that returns false
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkPermissions()){
            return
        }
        //Setup device. break out if not setup, or if error occurred
        setupDevice()?.let {
            if(!it){
                //setup not complete
                return
            }
        } ?: run{
            //Something really bad happened here. Not sure how we continue
            setupError()
            return
        }

        val timeDiffSinceStart = System.currentTimeMillis()-timeAtStart
        if(timeDiffSinceStart in 1..499) {
            handler.postDelayed({
                next()
            }, timeDiffSinceStart)
            return
        }

        //All checks are done. Lets startup the activity!
        ContextCompat.startForegroundService(applicationContext, Intent(applicationContext, ControlSDKService::class.java))
        hasInitialized = true
        startActivity(MainActivity.getIntent(this))
        finish()
    }

    /**
     * Show some setup error message. Allow the user to attempt setup again
     */
    private fun setupError() {
        Toast.makeText(this
            , "Something happened while trying to setup. Please try again"
            , Toast.LENGTH_LONG).show()
        startSetup()
    }

    private var pendingDeviceSetup: HardwareDriver? = null

    private var pendingRequestCode: Int = -1

    private fun setupDevice(): Boolean? {
        var setupDone = true
        RemoSettingsUtil.with(this){
            if(!it.robotSettingsEnable.getPref()) return@with //don't attempt if not even enabled
            it.robotCommunicationDriver.getPref().also {driver ->
                if(driver.getAnnotation(DriverComponent::class.java)?.requiresSetup == false)
                    return@with
                val clazz = driver.newInstance() as HardwareDriver
                clazz.let { driverClass ->
                    if(driverClass.needsSetup(this)){
                        val tmpCode = driverClass.setupComponent(this)
                        //Sometimes we still need setup without a UI. Will return -1 if that is the case
                        if(tmpCode != -1){
                            pendingRequestCode = tmpCode
                            pendingDeviceSetup = driverClass
                            setupDone = false
                        }
                    }
                }
            }
        }
        //TODO translator setup
        return setupDone
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsAlreadyRequested = true
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkPermissions()){
            next()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Check if result was due to a pending interface setup
        pendingDeviceSetup?.takeIf { pendingRequestCode == requestCode}?.let {
            //relay info to interface
            if(resultCode != Activity.RESULT_OK) {
                startSetup() //not ok, exit to setup
            }
            else{
                it.receivedComponentSetupDetails(this, data)
                next()
            }
            pendingDeviceSetup = null
            pendingRequestCode = -1
        }
    }

    private val requestCode = 1002

    @RequiresApi(23)
    private fun checkPermissions() : Boolean{
        val permissionsToAccept = ArrayList<String>()
        for (perm in getCurrentRequiredPermissions()){
            if(ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED){
                permissionsToAccept.add(perm)
            }
        }

        if(permissionsToAccept.size > 0 && permissionsAlreadyRequested){
            handlePermissionDenied(permissionsToAccept)
            return false
        }

        return if(permissionsToAccept.isNotEmpty()){
            requestPermissions(
                permissionsToAccept.toArray(Array(0) {""}),
                requestCode
            )
            false
        }
        else{
            true
        }
    }

    @RequiresApi(23)
    private fun handlePermissionDenied(permissionsToAccept: ArrayList<String>) {
        binding.apply {
            progressSplash.visibility = View.GONE
            permissionsRequest.visibility = View.VISIBLE
            permissionsToAccept.forEach { permission ->
                when(permission){
                    Manifest.permission.ACCESS_COARSE_LOCATION -> {
                        locationSplashTitle.visibility = View.VISIBLE
                    }
                    Manifest.permission.ACCESS_FINE_LOCATION -> {
                        locationSplashTitle.visibility = View.VISIBLE
                    }
                    Manifest.permission.CAMERA -> {
                        cameraSplashTitle.visibility = View.VISIBLE
                    }
                    Manifest.permission.RECORD_AUDIO -> {
                        micSplashTitle.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun getCurrentRequiredPermissions() : ArrayList<String> {
        val list = ArrayList<String>()
        RemoSettingsUtil.with(this){
            if(it.microphoneEnabled.getPref()){
                list.add(Manifest.permission.RECORD_AUDIO)
            }
            if(it.cameraEnabled.getPref()){
                list.add(Manifest.permission.CAMERA)
            }

            //location permission required to scan for bluetooth device
            if(it.robotSettingsEnable.getPref() && it.robotCommunicationDriver.getPref() == BluetoothClassicDriver::class.java){
                list.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                list.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
        return list
    }

    companion object{
        var hasInitialized = false
    }
}