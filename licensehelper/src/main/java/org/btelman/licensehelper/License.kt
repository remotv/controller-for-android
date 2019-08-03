package org.btelman.licensehelper

/**
 * License Info for a single dependency
 */
data class License(val name : String,
                   val licenseType: LicenseType,
                   val licenseLink : String = licenseType.getDefaultLink())