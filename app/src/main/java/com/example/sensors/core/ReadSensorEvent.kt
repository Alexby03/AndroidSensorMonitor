package com.example.sensors.core

data class ReadSensorEvent(

    // (X, Y, Z)
    val accelerometerValues: FloatArray,
    val gyroscopeValues: FloatArray,

    val timestamp: Long = System.currentTimeMillis()
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReadSensorEvent

        if (timestamp != other.timestamp) return false
        if (!accelerometerValues.contentEquals(other.accelerometerValues)) return false
        if (!gyroscopeValues.contentEquals(other.gyroscopeValues)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + accelerometerValues.contentHashCode()
        result = 31 * result + gyroscopeValues.contentHashCode()
        return result
    }

}
