package tv.remo.android.controller.sdk.components.video

import androidx.annotation.RequiresApi
import org.btelman.controlsdk.streaming.models.StreamInfo
import org.btelman.controlsdk.streaming.video.processors.FFmpegVideoProcessorAPI24
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.utils.FFmpegCommandBuilder

/**
 * More customized video processor that ties into the remo app better
 */
@RequiresApi(24)
class RemoVideoProcessor : FFmpegVideoProcessorAPI24() {
    override fun getFilterOptions(props: StreamInfo): String {
        var filter = ""
        context?.let {
            filter = RemoSettingsUtil.with(it).cameraFFmpegFilterOptions.getPref()
        }
        filter = filter.replace("\${internal}", super.getFilterOptions(props))
        return filter
    }

    override fun getVideoOutputOptions(props: StreamInfo): ArrayList<String> {
        val settings = RemoSettingsUtil.with(context!!)
        return FFmpegCommandBuilder.getAndProcessPreference(settings.ffmpegOutputOptions, props, settings, legacy = false)
    }
}