package org.btelman.controlsdk.streaming.models

data class AudioPacket(val b : ByteArray,
                       val timecode : Long = System.currentTimeMillis()) {
    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            javaClass != other?.javaClass -> false
            else -> {
                other as AudioPacket
                b.contentEquals(other.b) && timecode == other.timecode
            }
        }
    }

    override fun hashCode(): Int {
        var result = b.contentHashCode()
        result = 31 * result + timecode.hashCode()
        return result
    }

}