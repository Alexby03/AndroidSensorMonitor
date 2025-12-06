package com.example.sensors.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AngleCalculator {
    private var lastFilteredAcceleratorAngle = 0f
    private var currentFusionAngle = 0f
    private var lastTimestamp: Long = 0

    private val aEWMA = 0.2f
    private val aFusion = 0.5f

    fun process(rawEvents: Flow<ReadSensorEvent>): Flow<MeasuredResult> {
        reset()

        return rawEvents.map { event ->
            val dt = if (lastTimestamp == 0L) {
                0f
            } else {
                (event.timestamp - lastTimestamp) / 1_000f
            }
            lastTimestamp = event.timestamp

            val ay = event.accelerometerValues[1]
            val az = event.accelerometerValues[2]

            val gyroX = event.gyroscopeValues[0]

            val angle1 = accelerationAngle(ay, az)
            val angle2 = fusionAngle(angle1, gyroX, dt)

            MeasuredResult(
                timestamp = event.timestamp,
                algo1Angle = angle1,
                algo2Angle = angle2
            )
        }
    }

    // algorithm #1
    private fun accelerationAngle(ay: Float, az: Float): Float {
        val angle = Math.toDegrees(kotlin.math.atan2(az.toDouble(), ay.toDouble())).toFloat()
        lastFilteredAcceleratorAngle = if (lastFilteredAcceleratorAngle == 0f && angle > 0f) {
            angle
        } else {
            aEWMA * angle + (1 - aEWMA) * lastFilteredAcceleratorAngle
        }
        return lastFilteredAcceleratorAngle
    }

    // algorithm #2
    private fun fusionAngle(currentAccelAngle: Float, gyroXRad: Float, dt: Float): Float {
        val gyroXDeg = Math.toDegrees(gyroXRad.toDouble()).toFloat()
        currentFusionAngle = aFusion * currentAccelAngle + (1 - aFusion) * (currentFusionAngle + gyroXDeg * dt)
        return currentFusionAngle
    }

    private fun reset() {
        lastFilteredAcceleratorAngle = 0f
        currentFusionAngle = 0f
        lastTimestamp = 0
    }
}
