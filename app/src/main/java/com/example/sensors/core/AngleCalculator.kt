package com.example.sensors.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.acos
import kotlin.math.sqrt

class AngleCalculator {
    private var lastFilteredAcceleratorAngle = 0f
    private var currentFusionAngle = 0f
    private var lastTimestamp: Long = 0

    private val aEWMA = 0.05f
    private val aFusion = 0.75f

    fun process(rawEvents: Flow<ReadSensorEvent>): Flow<MeasuredResult> {
        reset()

        return rawEvents.map { event ->
            val dt = if (lastTimestamp == 0L) {
                0f
            } else {
                (event.timestamp - lastTimestamp) / 1_000f
            }
            lastTimestamp = event.timestamp

            val ax = event.accelerometerValues[0]
            val ay = event.accelerometerValues[1]
            val az = event.accelerometerValues[2]

            val gyroX = event.gyroscopeValues[0]

            val angle1 = accelerationAngle(ax, ay, az)
            val angle2 = fusionAngle(angle1, gyroX, dt)

            MeasuredResult(
                timestamp = event.timestamp,
                algo1Angle = angle1,
                algo2Angle = angle2
            )
        }
    }

    // algorithm #1
    private fun accelerationAngle(ax: Float, ay: Float, az: Float): Float {
        val norm = sqrt(ax*ax + ay*ay + az*az)
        val cosTilt = ay / norm
        val angle = Math.toDegrees(acos(cosTilt).toDouble())
        if (lastFilteredAcceleratorAngle == 0f && angle != 0.0) {
            lastFilteredAcceleratorAngle = angle.toFloat()
            return lastFilteredAcceleratorAngle
        }

        val filteredAngle = (aEWMA * angle + (1 - aEWMA) * lastFilteredAcceleratorAngle).toFloat()
        lastFilteredAcceleratorAngle = filteredAngle
        return filteredAngle
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
