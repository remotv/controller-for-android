package tv.remo.android.controller.sdk.utils

import java.util.*
import java.util.regex.Pattern

/**
 * ^((https?|ftp)://|(www|ftp)\.)+[a-z0-9-]+(\.[a-z0-9-]+)+(:[0-9]+)?([/?].*)?$
 */
private const val URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)+[a-z0-9-]+(\\.[a-z0-9-]+)+(:[0-9]+)?([/?].*)?$"

/**
 * Filters out most url formats, including urls with ports, as long as they are passed in as lowercase
 * Is not super aggressive about filtering urls. To see all the formats, please look at the unit tests
 *
 * ```
 *
 * ^((https?|ftp)://|(www|ftp)\.)+[a-z0-9-]+(\.[a-z0-9-]+)+(:[0-9]+)?([/?].*)?$
 *
 * ```
 *
 * @see tv.remo.android.controller.sdk.StringUtilTest
 */
fun String.isUrl() : Boolean{
    val p = Pattern.compile(URL_REGEX)
    this.toLowerCase(Locale.US).split(" ").forEach {
        val m = p.matcher(it)
        if(m.find())
            return true
    }
    return false
}

fun String.startsWith(vararg prefix : String) : Boolean{
    prefix.forEach {
        if(this.startsWith(it, false)) return true
    }
    return false
}