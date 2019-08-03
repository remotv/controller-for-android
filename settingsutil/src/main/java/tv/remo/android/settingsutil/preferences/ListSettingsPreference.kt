package tv.remo.android.settingsutil.preferences

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.preference.PreferenceViewHolder
import tv.remo.android.settingsutil.R

class ListSettingsPreference : TwoTargetListPreference {

    constructor(context: Context, attrs: AttributeSet,
                defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context) : super(context)

    var hideSecondTarget: Boolean = false

    override fun getSecondTargetResId(): Int {
        return R.layout.preference_widget_master_settings
    }

    override fun shouldHideSecondTarget(): Boolean {
        return hideSecondTarget || super.shouldHideSecondTarget()
    }

    private var widgetView: View? = null

    private var imageView: View? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        widgetView = holder.findViewById(android.R.id.widget_frame)
        imageView = holder.findViewById(R.id.imageWidget)
        widgetView?.setOnClickListener{
            click?.invoke()
        }
    }

    var click : (() -> Unit)? = null

    fun setOnClickListener(onClick:() -> Unit) {
        click = onClick
    }
}
