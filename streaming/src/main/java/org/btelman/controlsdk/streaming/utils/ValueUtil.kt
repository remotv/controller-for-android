package org.btelman.controlsdk.streaming.utils

import java.util.*

object ValueUtil {
    fun randomFloatRange(range: Pair<Float, Float>, random : Random = Random()) : Float{
        return map(random.nextFloat(), Pair(0f, 1f), range)
    }

    fun map(input: Float, inRange: Pair<Float, Float>, outRange: Pair<Float, Float>): Float {
        val boundedInput = range(input, inRange.first, inRange.second)
        val output = (boundedInput - inRange.first) /
                (inRange.second - inRange.first) *
                (outRange.second - outRange.first) + outRange.first
        return range(output, outRange.first, outRange.second)
    }

    fun range(input: Float, min: Float, max: Float): Float {
        var output = input
        if (input < min) {
            output = min
        } else if (input > max) {
            output = max
        }
        return output
    }

    //https://stackoverflow.com/questions/6618994/simplifying-fractions-in-java/6619098
    fun gcm(a: Int, b: Int): Int {
        return if (b == 0) a else gcm(b, a % b) // Not bad for one line of code :)
    }
}