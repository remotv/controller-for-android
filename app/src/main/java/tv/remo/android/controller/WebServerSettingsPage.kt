package tv.remo.android.controller

import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import tv.remo.android.controller.databinding.FragmentWebServerSettingsPageBinding
import tv.remo.android.controller.sdk.components.RemoWebServer


/**
 * A simple [Fragment] subclass.
 */
class WebServerSettingsPage : Fragment() {

    lateinit var server : RemoWebServer

    private var _binding: FragmentWebServerSettingsPageBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentWebServerSettingsPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val onSettingsUpdated : ()->Unit = {
        view?.post {
            binding.responseTextView.visibility = View.VISIBLE
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
        binding.ipAddrValue.text = if(ip != null && ip != "0.0.0.0"){
            "$ip:8080/config"
        }
        else{
            "This network does not support IP4 or is not a Wi-Fi network. Web page disabled"
        }
        if(ip != null)
            server.open()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            server.close()
        } catch (e: Exception) {
        }
    }
}
