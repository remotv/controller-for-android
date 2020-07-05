package tv.remo.android.controller.activities

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.fragment_web_server_settings_page.*
import tv.remo.android.controller.R
import tv.remo.android.controller.WebServerSettingsPage
import tv.remo.android.controller.sdk.components.RemoWebServer

class HeadlessInit : BaseActivity() {
    private val onSettingsUpdated : ()->Unit = {
        window?.decorView?.post {
            responseTextView.visibility = View.VISIBLE
        }
    }
    var server : RemoWebServer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_headless_init)
        log.d("onCreate")
        when {
            intent?.data?.toString() == "activate-headless" -> {
                setHeadless(true)
                return
            }
            intent?.data?.toString() == "deactivate-headless" -> {
                setHeadless(false)
                return
            }
        }
        launchWebServer()
    }

    private fun launchWebServer() {
        server = RemoWebServer(baseContext!!, onSettingsUpdated)
        val ip: String? = try {
            val wm =
                baseContext!!.getSystemService(Context.WIFI_SERVICE) as WifiManager?
            Formatter.formatIpAddress(wm!!.connectionInfo.ipAddress)
        } catch (e: Exception) {
            null
        }
        ipAddrValue.text = if(ip != null && ip != "0.0.0.0"){
            "$ip:8080/config"
        }
        else{
            "This network does not support IP4 or is not a Wi-Fi network. Web page disabled"
        }
        if(ip != null)
            server?.open()
    }

    private fun setHeadless(b: Boolean) {
        log.d("activate-headless $b")
        val componentName = ComponentName(applicationContext.packageName,
            applicationContext.packageName+".HeadlessSplashScreen")
        val enabledFlag = if(b) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        packageManager.setComponentEnabledSetting(componentName, enabledFlag, 0)
    }
}