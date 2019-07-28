package tv.remo.android.controller.models

import org.btelman.licensehelper.License
import org.btelman.licensehelper.LicenseType

/**
 * All of the licenses used in Remo Main app
 */
object Licenses{
    val licenses = arrayListOf(
            License("Android Multi Dex Library", LicenseType.APACHE2_0),
            License("Android ConstraintLayout", LicenseType.APACHE2_0),
            License("Material Components For Android", LicenseType.APACHE2_0),
            License("Android Support Library V4", LicenseType.APACHE2_0),
            License("Android AppCompat Library V7", LicenseType.APACHE2_0),
            License("Kotlin Standard Library JDK 7", LicenseType.APACHE2_0),
            License("Kotlinx Coroutines Core", LicenseType.APACHE2_0),
            License("Kotlinx Coroutines Android", LicenseType.APACHE2_0),
            License("Android Navigation Fragment Kotlin Extensions", LicenseType.APACHE2_0),
            License("Android Navigation UI Kotlin Extensions", LicenseType.APACHE2_0),
            License("Android Preferences KTX", LicenseType.APACHE2_0),
            License("ZXing Core", LicenseType.APACHE2_0),
            License("Remo.TV Core", LicenseType.APACHE2_0)
    ).also {
        for(license in tv.remo.android.controller.sdk.models.Licenses.licenses){
            if(!it.contains(license)){
                it.add(license)
            }
        }
    }
}