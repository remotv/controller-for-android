package tv.letsrobot.controller.android.utils

import android.widget.EditText

fun EditText.string() : String{
    return text.toString()
}

fun EditText.toIntOrZero() : Int{
    return string().toIntOrNull()?.let { it } ?: 0
}