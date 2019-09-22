package tv.remo.android.controller.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import tv.remo.android.controller.R

class SettingsEntryFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_entry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            Navigation.findNavController(view).also {
                it.navigate(R.id.action_settingsEntryFragment_to_settingsLanding)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
