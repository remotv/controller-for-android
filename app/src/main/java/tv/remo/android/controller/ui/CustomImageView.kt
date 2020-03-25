package tv.remo.android.controller.ui

import android.content.Context
import android.util.AttributeSet

/**
 * Image View that follows other UI behavior if disabled
 */
open class CustomImageView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        imageAlpha = if(enabled) 0xFF else 0x3F
    }
}