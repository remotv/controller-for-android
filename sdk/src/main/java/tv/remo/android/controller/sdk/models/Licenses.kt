package tv.remo.android.controller.sdk.models

import org.btelman.licensehelper.License
import org.btelman.licensehelper.LicenseType

/**
 * api licenses
 */
object Licenses {
    val licenses = arrayListOf(
            //mainly ControlSDK licences
            License("Android AppCompat Library V7",LicenseType.APACHE2_0),
            License("Android AppCompat Library V7",LicenseType.APACHE2_0),
            License("Kotlin Standard Library JDK 7", LicenseType.APACHE2_0),
            License("Kotlinx Coroutines Core", LicenseType.APACHE2_0),
            License("Kotlinx Coroutines Android", LicenseType.APACHE2_0),
            License("Android Lifecycle ViewModel", LicenseType.APACHE2_0),
            License("Android Lifecycle Extensions", LicenseType.APACHE2_0),
            License("Guava: Google Core Libraries For Java", LicenseType.APACHE2_0),
            License("ControlSDK, github.com/btelman96/ControlSDK", LicenseType.APACHE2_0),
            License("github.com/felHR85/UsbSerial", LicenseType.MIT),
            License("OkHttp", LicenseType.APACHE2_0),
            License("FFmpeg, github.com/btelman96/ffmpeg-android-java", LicenseType.GPL3),
            License("github.com/btelman96/AndroidUvcDemo", LicenseType.GPL3
                    , "https://raw.githubusercontent.com/btelman96/AndroidUvcDemo/master/LICENCE")
    )
}