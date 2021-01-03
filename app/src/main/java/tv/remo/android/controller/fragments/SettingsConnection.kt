package tv.remo.android.controller.fragments
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.navigation.Navigation
import androidx.preference.EditTextPreference
import tv.remo.android.controller.R
import tv.remo.android.settingsutil.fragments.BasePreferenceFragmentCompat

/**
 * Connection Settings
 *
 * Contains robotId, cameraId, streamKey
 */

class SettingsConnection : BasePreferenceFragmentCompat(
    R.xml.settings_connection
){
    private var rootKey: String? = null
    private val handler = Handler()
    var refreshNeeded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        if(refreshNeeded){
            refreshPreferences()
        }
    }

    fun refreshPreferences(){
        preferenceScreen = null
        addPreferencesFromResource(R.xml.settings_connection)
        refreshNeeded = false
        handler.postDelayed({
            onCreatePreferences(null, rootKey)
        }, 50)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        this.rootKey = rootKey
        super.onCreatePreferences(savedInstanceState, rootKey)
        findPreference<EditTextPreference>(getString(R.string.connectionApiTokenKey))?.setOnPreferenceChangeListener { _, _ ->
            handler.post {
                refreshPreferences()
            }
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.connection_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.webServerSelectMenuItem -> {
                refreshNeeded = true
                Navigation.findNavController(view!!)
                    .navigate(R.id.action_settingsConnection_to_webServerSettingsPage)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}