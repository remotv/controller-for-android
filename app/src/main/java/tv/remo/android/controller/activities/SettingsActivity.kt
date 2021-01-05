package tv.remo.android.controller.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import tv.remo.android.controller.R
import tv.remo.android.controller.RemoApplication
import tv.remo.android.controller.databinding.ActivitySettingsBinding
import tv.remo.android.settingsutil.interfaces.SwitchBarCapableActivity
import tv.remo.android.settingsutil.views.SwitchBar

class SettingsActivity : AppCompatActivity(), NavController.OnDestinationChangedListener, SwitchBarCapableActivity {
    private lateinit var binding: ActivitySettingsBinding
    private var first = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        NavigationUI.setupActionBarWithNavController(this, Navigation.findNavController(this, R.id.nav_host_fragment))
        Navigation.findNavController(this, R.id.nav_host_fragment).addOnDestinationChangedListener (this)
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        if(destination.id == R.id.settingsEntryFragment){
            if(!first){
                //completely restart the app, killing anything that would have survived
                RemoApplication.restart(this)
            }
            first = false
        }
    }

    override fun onSupportNavigateUp() : Boolean {
        return Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp()
    }

    override fun getSwitchBar(): SwitchBar {
        return binding.switchBar
    }

    companion object{
        fun getIntent(context: Context) : Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}
