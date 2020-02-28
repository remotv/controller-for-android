package tv.remo.android.controller.utils.v16

import android.content.Context
import android.hardware.Camera
import java.util.*

/**
 * Created by Brendon on 2/27/2020.
 */
object Camera1Util {
    fun getCameraSizes(requireContext: Context, cameraIndex: Int): ArrayList<Pair<Int, Int>> {
        val list = ArrayList<Pair<Int, Int>>()
        kotlin.runCatching {
            val camera = Camera.open(cameraIndex)
            camera.runCatching {
                parameters.supportedPreviewSizes.forEach{
                    list.add(Pair(it.height, it.width))
                }
            }
            camera.release()
        }
        return list
    }
}