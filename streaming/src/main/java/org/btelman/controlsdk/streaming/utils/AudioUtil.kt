package org.btelman.controlsdk.streaming.utils

import java.nio.ByteBuffer

/**
 * Audio helper functions
 */
object AudioUtil {
    fun ShortToByte_ByteBuffer_Method(input: ShortArray): ByteArray {
        var index= 0
        val iterations = input.size

        val bb = ByteBuffer.allocate(input.size * 2)

        while (index != iterations) {
            bb.putShort(input[index])
            ++index
        }

        return bb.array()
    }
}