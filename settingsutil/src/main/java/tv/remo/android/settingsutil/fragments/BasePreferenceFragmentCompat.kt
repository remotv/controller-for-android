package tv.remo.android.settingsutil.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.annotation.XmlRes
import androidx.preference.PreferenceFragmentCompat
import tv.remo.android.settingsutil.R
import tv.remo.android.settingsutil.interfaces.SwitchBarCapableActivity
import tv.remo.android.settingsutil.views.SwitchBar

/**
 * Fragment that will handle preferences logic.
 * Giving it a second parameter will make it automatically handle a master switch
 */
abstract class BasePreferenceFragmentCompat(
        @XmlRes val preferencesXmlId : Int,
        @StringRes val switchBarKeyStringId : Int = -1
) : PreferenceFragmentCompat() {

    open fun getSwitchBarOnText() : Int{
        return R.string.switch_on_text
    }

    open fun getSwitchBarOffText() : Int{
        return R.string.switch_off_text
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(preferencesXmlId, rootKey)
    }

    private lateinit var switchBar: SwitchBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        switchBar = (activity as SwitchBarCapableActivity).getSwitchBar()
        evalSwitchBar()
    }

    private fun evalSwitchBar(){
        if(switchBarKeyStringId == -1){
            switchBar.hide()
            return
        }
        switchBar.setSwitchBarText(
                getSwitchBarOnText(),
                getSwitchBarOffText())
        switchBar.setPrefsKey(getString(switchBarKeyStringId))
        switchBar.show()
        setupSwitchBarListener()
    }

    private fun setupSwitchBarListener() {
        switchBar.addOnSwitchChangeListener { _, isChecked ->
            evaluateSwitchState(isChecked)
        }
        evaluateSwitchState(switchBar.isChecked)
    }

    private fun evaluateSwitchState(checked: Boolean) {
        preferenceManager.preferenceScreen.isEnabled = checked
    }
}