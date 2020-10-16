package org.btelman.controlsdk.streaming.models

import android.graphics.ImageFormat
import android.graphics.Rect

data class ImageDataPacket(val b : Any?,
                           val format : Int = ImageFormat.JPEG,
                           val r : Rect? = null,
                           val timecode : Long = System.currentTimeMillis())