package org.btelman.controlsdk.streaming.utils

import android.content.Context
import org.btelman.android.ffmpeg.FFmpegRunner
import org.btelman.android.shellutil.BinaryUpdateChecker
import java.io.File
import java.io.PrintStream
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Utility functions for FFmpeg
 */
object FFmpegUtil {

    private val ffmpegResult = AtomicInteger(-1)

    /**
     * Kill FFmpeg by sending garbage data to the outputStream since it does not close on its own
     * correctly when closing the outputStream
     */
    fun killFFmpeg(process: Process?) {
        process?.let {
            val printStream = PrintStream(it.outputStream)
            printStream.print("die") //does not matter what is here. Any garbage data should do the trick
            printStream.flush()
            printStream.close()
        }
    }

    // blocking ffmpeg initialize that will only initialize once,
    // and block until result on the other threads that called this same function instead of that
    // one initializing as well
    @Synchronized
    fun initFFmpeg(context: Context, secondaryFile : File? = null, onResult : (Boolean)->Unit){
        if(ffmpegResult.get() == -1){
            try {
                val installPath = BinaryUpdateChecker.GetPreferredInstallPath(context, "ffmpeg")
                if(!installPath.exists()){
                    // Only want to run this part if the install path has not been written yet.
                    // Otherwise, we run the risk of overwriting a custom ffmpeg version
                    if(secondaryFile == null){
                        if(!FFmpegRunner.checkIfUpToDate(context, installPath))
                            FFmpegRunner.update(context, binaryPath = installPath)
                    }
                    else{
                        if(secondaryFile.exists()){
                            val fileStream = secondaryFile.inputStream()
                            if(!BinaryUpdateChecker.CheckBinaryCorrectVersion(fileStream, installPath)){
                                FFmpegRunner.update(context, fileStream, installPath)
                            }
                        }
                    }
                }
                ffmpegResult.set(1)
                onResult(true)
            } catch (e: Exception) {
                e.printStackTrace()
                ffmpegResult.set(0)
                onResult(false)
            }
        }
        else{
            onResult(ffmpegResult.get() == 1)
        }
    }

    fun execute(context: Context, uuid: String, command: String, responseHandler: FFmpegExecuteResponseHandler) {
        val builder = FFmpegRunner.Builder(context).also {
            responseHandler.bind(it)
            it.command = command
        }
        FFmpegRunner.startProcess(builder, UUID.fromString(uuid))
    }

    abstract class FFmpegExecuteResponseHandler{
        var OnStart : () -> Unit = {
            onStart()
        }
        var OnProcess : (Process) -> Unit = {
            onProcess(it)
        }
        var OnProgress : (String) -> Unit = {
            onProgress(it)
        }
        var OnError : (String) -> Unit = {
            onError(it)
        }
        var OnComplete : (Int?)->Unit = {
            onComplete(it)
        }

        open fun onStart(){}
        open fun onProcess(process: Process){}
        open fun onProgress(message : String){}
        open fun onError(message : String){}
        open fun onComplete(statusCode : Int?){}

        fun bind(builder: FFmpegRunner.Builder) {
            builder.OnStart = OnStart
            builder.OnProcess = OnProcess
            builder.OnProgress = OnProgress
            builder.OnError = OnError
            builder.OnComplete = OnComplete
        }
    }
}