package org.btelman.licensehelper

/**
 * License Types
 */
enum class LicenseType {
    APACHE2_0,
    MIT,
    GPL3,
    LGPL3,
    BSD3Clause;

    fun getDefaultLink(): String {
        return when(this){
            APACHE2_0 -> "https://www.apache.org/licenses/LICENSE-2.0.txt"
            MIT -> "https://opensource.org/licenses/MIT"
            GPL3 -> "https://www.gnu.org/licenses/gpl-3.0.txt"
            LGPL3 -> "https://www.gnu.org/licenses/lgpl-3.0.txt"
            BSD3Clause -> "https://opensource.org/licenses/BSD-3-Clause"
        }
    }
}