package com.example.sensors.core

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SensorReader(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private var lastAccelerometerValues = FloatArray(3)
    private var lastGyroscopeValues = FloatArray(3)

    fun getSensorData(): Flow<ReadSensorEvent> = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event ?: return

                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        event.values.copyInto(lastAccelerometerValues)
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        event.values.copyInto(lastGyroscopeValues)
                    }
                }

                trySend(
                    ReadSensorEvent(
                        accelerometerValues = lastAccelerometerValues.clone(),
                        gyroscopeValues = lastGyroscopeValues.clone(),
                        timestamp = System.currentTimeMillis()
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }
        }

        accelerometer?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
        }
        gyroscope?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
        }

        // close coroutine / stop flow
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}