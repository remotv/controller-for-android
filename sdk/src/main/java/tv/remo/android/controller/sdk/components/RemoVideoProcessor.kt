package tv.remo.android.controller.sdk.components

import org.btelman.controlsdk.streaming.models.StreamInfo
import org.btelman.controlsdk.streaming.video.processors.FFmpegVideoProcessor
import tv.remo.android.controller.sdk.RemoSettingsUtil

/**
 * More customized video processor that ties into the remo app better
 */
class RemoVideoProcessor : FFmpegVideoProcessor() {

    override fun getFilterOptions(props: StreamInfo): String {
        var filter = ""
        context?.let {
            RemoSettingsUtil.with(it){ settings ->
                filter = settings.cameraFFmpegFilterOptions.getPref()
            }
        }
        return super.getFilterOptions(props)+filter //intentionally no space added.
    }
}