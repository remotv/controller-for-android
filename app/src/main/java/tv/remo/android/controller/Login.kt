package tv.remo.android.controller


import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_login.*
import tv.remo.android.controller.sdk.utils.EndpointBuilder

/**
 * A simple [Fragment] subclass.
 */
class Login : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //TODO ONLY ALLOW ON API 19 AND ABOVE
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.login_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.shareRemoTokenToApp){
            var alert : AlertDialog? = null
            alert = AlertDialog.Builder(requireContext()).apply {
                setTitle("Allow Remo Controller for Android to access remo.tv token?")
                setMessage("This allows the controller to help with getting robot token and setting up the channel.")
                setCancelable(true)
                setNegativeButton("Deny"){ _, _ ->
                    alert?.dismiss()
                }
                setPositiveButton("Allow"){ _, _ ->
                    saveAPIToken()
                    alert?.dismiss()
                }
            }.show()
        }
        return true
    }

    fun saveAPIToken(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webViewRemoLogin.evaluateJavascript("(function() { return localStorage.token; })();"){
                Navigation.findNavController(view!!).navigateUp()
            }
        }
        else{
            Navigation.findNavController(view!!).navigateUp()
        }
    }
}
