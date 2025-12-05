package com.example.sensors

import android.app.Application
import com.example.sensors.core.AngleCalculator
import com.example.sensors.core.SensorReader
import com.example.sensors.core.sensors.AccelerometerReader
import com.example.sensors.core.sensors.GyroscopeReader
import com.example.sensors.core.sensors.SensorRepository

class SensorApplication : Application() {

    lateinit var accelerometerReader: AccelerometerReader
    lateinit var gyroscopeReader: GyroscopeReader
    lateinit var sensorRepository: SensorRepository
    lateinit var angleCalculator: AngleCalculator

    override fun onCreate() {
        super.onCreate()

        accelerometerReader = AccelerometerReader(this)
        gyroscopeReader = GyroscopeReader(this)
        sensorRepository   = SensorRepository(accelerometerReader, gyroscopeReader)
        angleCalculator    = AngleCalculator()
    }
}