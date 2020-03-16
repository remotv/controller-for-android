package tv.remo.android.controller

import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_web_server_settings_page.*
import tv.remo.android.controller.sdk.components.RemoWebServer


/**
 * A simple [Fragment] subclass.
 */
class WebServerSettingsPage : Fragment() {

    lateinit var server : RemoWebServer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_web_server_settings_page, container, false)
    }

    private val onSettingsUpdated : ()->Unit = {
        view?.post {
            responseTextView.visibility = View.VISIBLE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        server = RemoWebServer(context!!, onSettingsUpdated)
        val ip: String? = try {
            val wm =
                context!!.getSystemService(WIFI_SERVICE) as WifiManager?
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
            server.open()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            server.close()
        } catch (e: Exception) {
        }
    }
}
