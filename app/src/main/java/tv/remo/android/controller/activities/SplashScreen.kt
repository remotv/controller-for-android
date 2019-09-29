package tv.remo.android.controller.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import kotlinx.coroutines.runBlocking
import org.btelman.controlsdk.hardware.drivers.BluetoothClassicDriver
import org.btelman.controlsdk.hardware.interfaces.DriverComponent
import org.btelman.controlsdk.hardware.interfaces.HardwareDriver
import org.btelman.controlsdk.services.ControlSDKService
import org.btelman.controlsdk.streaming.utils.FFmpegUtil
import org.btelman.controlsdk.utils.ClassScanner
import tv.remo.android.controller.R
import tv.remo.android.controller.sdk.RemoSettingsUtil

class SplashScreen : FragmentActivity() {

    var classScanComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        detectIntentUpdateSettings(intent)
        runOnUiThread{
            var needsSetup = false
            RemoSettingsUtil.with(this){ settings ->
                needsSetup = settings.apiKey.let {
                    it.defaultValue == it.getPref()
                }
                needsSetup = needsSetup && settings.channelId.let {
                    it.defaultValue == it.getPref()
                }
            }
            if(needsSetup){
                Toast.makeText(this,
                    "APIKey and Channel ID required to run", Toast.LENGTH_SHORT).show()
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

    private fun startSetup() {
        startActivity(SettingsActivity.getIntent(this))
        finish()
    }

    private fun next() {
        if(!classScanComplete){
            Thread{
                ClassScanner.getClasses(this)
                runOnUiThread {
                    @Suppress("DeferredResultUnused")
                    runBlocking { FFmpegUtil.initFFmpeg(FFmpeg.getInstance(applicationContext)) }
                    classScanComplete = true
                    next()
                }
            }.start()
            return
        }

        //Check permissions. break out if that returns false
        if(!checkPermissions()){
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
        //All checks are done. Lets startup the activity!
        ContextCompat.startForegroundService(applicationContext, Intent(applicationContext, ControlSDKService::class.java))
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
        if(checkPermissions()){
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

    private fun checkPermissions() : Boolean{
        val permissionsToAccept = ArrayList<String>()
        for (perm in getCurrentRequiredPermissions()){
            if(ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED){
                permissionsToAccept.add(perm)
            }
        }

        return if(permissionsToAccept.isNotEmpty()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    permissionsToAccept.toArray(Array(0) {""}),
                    requestCode)
                false
            }
            else true
        }
        else{
            true
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
            if(it.robotCommunicationDriver.getPref() == BluetoothClassicDriver::class.java){
                list.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                list.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
        return list
    }
}