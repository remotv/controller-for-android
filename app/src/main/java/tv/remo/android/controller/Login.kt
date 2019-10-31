package tv.remo.android.controller


import android.app.AlertDialog
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_login.*
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.utils.EndpointBuilder

/**
 * A simple [Fragment] subclass.
 */
class Login : Fragment() {

    var lastClipboardTime = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webViewRemoLogin.settings.apply {
            javaScriptEnabled = true
            allowContentAccess = true
            databaseEnabled = true
            domStorageEnabled = true
        }
        webViewRemoLogin.loadUrl("https://${EndpointBuilder.getEndpointUrl(requireContext())}")
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        registerToClipboardEvents()
    }

    override fun onPause() {
        super.onPause()
        unregisterFromClipboardEvents()
    }

    private fun saveRobotToken(token: String) {
        RemoSettingsUtil.with(requireContext()){
            it.apiKey.sharedPreferences.edit().putString(it.apiKey.key, token).apply()
        }
    }

    private fun registerToClipboardEvents() {
        val clipService = context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipService.addPrimaryClipChangedListener(onClipEvent)
    }

    private fun unregisterFromClipboardEvents(){
        val clipService = context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipService.removePrimaryClipChangedListener(onClipEvent)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.login_menu, menu)
    }

    private val onClipEvent = ClipboardManager.OnPrimaryClipChangedListener {
        val clipService = context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        if (clipService.hasPrimaryClip()) {
            val cd = clipService.primaryClip

            if (cd!!.description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                if(System.currentTimeMillis()-lastClipboardTime > 1000)
                    onNewClipboardData(cd.getItemAt(0).text.toString())
                lastClipboardTime = System.currentTimeMillis()
            }
        }
    }

    fun onNewClipboardData(data : String){
        var alert : AlertDialog? = null
        alert = AlertDialog.Builder(requireContext()).apply {
            setTitle("Is this the robot api token?")
            setMessage("If you just copied the Remo.TV robot token, this can be set as the active robot")
            setCancelable(true)
            setNegativeButton("No"){ _, _ ->
                alert?.dismiss()
            }
            setPositiveButton("Set Active"){ _, _ ->
                saveRobotToken(data)
                alert?.dismiss()
            }
        }.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.shareRemoTokenToApp){
            saveAPIToken()
        }
        return true
    }

    fun saveAPIToken(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            RemoSettingsUtil.with(requireContext()){ settings ->
                webViewRemoLogin.url.also {
                    val finalChannel = it.split("/").last()
                    settings.channelId.sharedPreferences.edit().putString(
                        settings.channelId.key, finalChannel).apply()
                }
            }
        }
        else{
            Navigation.findNavController(view!!).navigateUp()
        }
    }
}
