package org.btelman.controlsdk.streaming.utils

import android.graphics.Bitmap
import android.graphics.ImageFormat
import org.btelman.controlsdk.streaming.models.ImageDataPacket
import java.io.IOException
import java.io.OutputStream

/**
 * Utilities for video processors that transfer image data via a process or an OutputStream
 */
object OutputStreamUtil {
    fun sendImageDataToProcess(process : Process?, packet: ImageDataPacket) : Boolean{
        return process?.let { _process ->
            try { //send the data and catch IO exception if it fails
                sendImageToOutputStream(_process.outputStream, packet)
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        } ?: false
    }

    fun sendImageToOutputStream(outputStream: OutputStream, packet: ImageDataPacket) : Boolean{
        return (packet.b as? ByteArray)?.let { _ ->
            processByteArray(outputStream, packet)
        } ?: processBitmap(outputStream, packet)
    }

    fun handleSendByteArray(outputStream: OutputStream, b: ByteArray) {
        outputStream.write(b)
    }

    private fun processBitmap(outputStream: OutputStream, packet: ImageDataPacket): Boolean {
        return (packet.b as? Bitmap)?.compress(
            Bitmap.CompressFormat.JPEG,
            100, outputStream) ?: false //TODO change quality?
    }

    private fun processByteArray(outputStream: OutputStream, it: ImageDataPacket) : Boolean{
        when (it.format) {
            ImageFormat.JPEG -> {
                handleSendByteArray(outputStream, it.b as ByteArray)
            }
            ImageFormat.NV21 -> {
                outputStream.write(it.b as ByteArray)
            }
            ImageFormat.YUV_420_888 -> {
                outputStream.write(it.b as ByteArray)
            }
            else -> {
            }
        }
        return true
    }
}