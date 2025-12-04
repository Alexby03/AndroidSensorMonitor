package com.example.sensors

import android.app.Application
import com.example.sensors.core.SensorReader

// 1. Import your helper classes
// import com.example.shoulderapp.data.InternalSensorProvider
// import com.example.shoulderapp.data.PolarSensorProvider
// import com.example.shoulderapp.domain.AngleDataProcessor

class SensorApplication : Application() {

    // This ONE class manages both Accelerometer and Gyroscope
    lateinit var internalSensorProvider: SensorReader

    //I am not going to use the polar reader so Im removing it here

    // This is the logic class we discussed earlier
    lateinit var angleDataProcessor: AngleDataProcessor //Do we really need the angle processor to know the application context?

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize Internal Sensors
        // We pass 'this' (Context) so it can access the System SensorManager
        internalSensorProvider = SensorReader(this)

        // 3. Initialize the Processor
        // This is just logic, no Context needed, but we create it here
        // so we can pass the same instance to the ViewModel Factory
        angleDataProcessor = AngleDataProcessor() //ionce again, needed????
    }
}
