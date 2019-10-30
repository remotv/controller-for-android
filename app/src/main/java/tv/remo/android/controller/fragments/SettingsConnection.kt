package tv.remo.android.controller.fragments
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
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
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                super.onViewCreated(view, savedInstanceState)
                setHasOptionsMenu(true)
        }

        override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
                super.onCreateOptionsMenu(menu, inflater)
                inflater.inflate(R.menu.connection_menu, menu)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
                if(item.itemId == R.id.signInOption){
                        Navigation.findNavController(view!!).navigate(R.id.loginSettingsEntry)
                }
                return true
        }
}