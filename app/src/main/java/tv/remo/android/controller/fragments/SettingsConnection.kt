package tv.remo.android.controller.fragments
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.navigation.Navigation
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
    var refreshNeeded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        if(refreshNeeded){
            preferenceScreen = null
            addPreferencesFromResource(R.xml.settings_connection)
            refreshNeeded = false
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