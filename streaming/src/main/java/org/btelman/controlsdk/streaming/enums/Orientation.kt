package org.btelman.controlsdk.streaming.enums

enum class Orientation private constructor(val value: Int) {
    /**
     * Portrait
     */
    DIR_0(0),
    /**
     * Landscape Right
     */
    DIR_90(90),
    /**
     * Reverse Portrait
     */
    DIR_180(180),
    /**
     * Landscape Left
     */
    DIR_270(270);

    override fun toString(): String {
        return value.toString()
    }

    companion object{
        fun forValue(value : Int) : Orientation?{
            values().forEach {
                if(it.value == value)
                    return it
            }
            return null
        }
    }
}