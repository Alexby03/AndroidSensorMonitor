package com.example.sensors.core.sensors

import com.example.sensors.core.ReadSensorEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SensorRepository(
    private val accelerometerReader: AccelerometerReader,
    private val gyroscopeReader: GyroscopeReader
) {

    fun getSensorEvents(): Flow<ReadSensorEvent> {
        val accelFlow = accelerometerReader.getAcceleration()
        val gyroFlow  = gyroscopeReader.getRotation()

        return combine(accelFlow, gyroFlow) { accel, gyro ->
            val ts = maxOf(accel.timestamp, gyro.timestamp)

            ReadSensorEvent(
                accelerometerValues = accel.values,
                gyroscopeValues = gyro.values,
                timestamp = ts
            )
        }
    }
}
