package com.example.sensors.core

data class ReadSensorEvent(
    // 1. X, Y, Z from the Accelerometer
    val accelerometerValues: FloatArray,

    // 2. X, Y, Z from the Gyroscope (nullable because Algorithm 1 might not use it,
    // or you might receive them at different times, but usually we try to sync them)
    val gyroscopeValues: FloatArray,

    // 3. The time this reading happened (crucial for integration/algorithms)
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
