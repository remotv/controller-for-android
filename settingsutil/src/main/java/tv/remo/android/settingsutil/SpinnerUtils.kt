package tv.remo.android.settingsutil

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Spinner
import java.util.*

/**
 * Created by Brendon on 12/14/2018.
 */
/**
 * Set the selection of Spinner given a string that it should contain
 */
fun Spinner.setPositionGivenText(value : String){
    var pos : Int? = null
    for(i in 0 until adapter.count){
        if(adapter.getItem(i) == value)
            pos = i
    }
    pos?.let {
        setSelection(it)
    } ?: setSelection(0)
}

/**
 * Sets up a spinner using enum values from RobotConfig
 */
fun <T : Enum<T>> Spinner.setupSpinnerWithSetting(value : T){
    adapter = SpinnerUtils.createEnumArrayAdapter(context, value.declaringClass.enumConstants)
    setSelection(value.ordinal)
}

object SpinnerUtils{
    /**
     * Create an arrayAdapter given array
     */
    internal fun <T : Enum<T>> createEnumArrayAdapter(context: Context, list : Array<T>) : ArrayAdapter<Any> {
        val arrList = ArrayList<String>()
        list.forEach {
            arrList.add(it.name)
        }
        // Creating adapter for spinner
        return ArrayAdapter(context, android.R.layout.simple_spinner_item, list)
    }
}
