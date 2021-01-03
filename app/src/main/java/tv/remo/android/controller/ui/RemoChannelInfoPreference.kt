package tv.remo.android.controller.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.preference.PreferenceViewHolder
import org.btelman.logutil.kotlin.LogUtil
import tv.remo.android.controller.R
import tv.remo.android.controller.databinding.RemoChannelPrefSecondTargetBinding
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.models.api.Channel
import tv.remo.android.controller.sdk.utils.RemoAPI
import tv.remo.android.settingsutil.preferences.TwoTargetPreference

class RemoChannelInfoPreference : TwoTargetPreference {

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?) : super(context)

    private var bound = false
    private val log = LogUtil(RemoChannelInfoPreference::class.java.simpleName)

    private val remoAPI = RemoAPI(context)
    private val handler = Handler(Looper.getMainLooper())
    private var viewHolder : PreferenceViewHolder? = null

    init {
        setOnPreferenceChangeListener{ _, _ ->
            viewHolder?.let{
                onBindViewHolder(it)
            }
            true
        }
    }

    override fun getSecondTargetResId(): Int {
        return R.layout.remo_channel_pref_second_target
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        viewHolder = holder
        bound = true
        val binding =
            RemoChannelPrefSecondTargetBinding.bind(holder.findViewById(R.id.remo_channel_pref_second_target))
        binding.requestSuccess.visibility = View.GONE
        binding.requestError.visibility = View.GONE
        binding.requestProgress.visibility = View.VISIBLE
        remoAPI.authRobot(RemoSettingsUtil.with(context).apiKey.getPref()) { channel: Channel?, error: Exception? ->
            if(!bound) return@authRobot
            handler.post{
                handleResult(holder, channel, error)
            }
        }
    }

    private fun handleResult(holder: PreferenceViewHolder, channel: Channel?, error: Exception?) {
        error?.let { log.e("Error validating API Key",error) }
        val binding = RemoChannelPrefSecondTargetBinding.bind(holder.findViewById(R.id.remo_channel_pref_second_target))
        binding.requestProgress.visibility = View.GONE
        val channelName: String
        if (channel?.name != null) {
            channelName = channel.name
            binding.requestSuccess.visibility = View.VISIBLE
        } else {
            log.w("The provided API key does not appear to be valid")
            channelName = "none (invalid API key)"
            binding.requestError.visibility = View.VISIBLE
        }
        (holder.findViewById(android.R.id.summary) as TextView).text = channelName
    }

    override fun onDetached() {
        super.onDetached()
        viewHolder = null
        bound = false
        remoAPI.cancelRequests()
    }
}