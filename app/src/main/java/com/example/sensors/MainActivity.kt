package com.example.sensors

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sensors.ui.screens.AppScreen
import com.example.sensors.ui.theme.SensorsTheme
import com.example.sensors.viewmodels.SensorVM

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val sensorViewModel: SensorVM = viewModel(factory = SensorVM.Factory)

            SensorsTheme {
                AppScreen(
                    vm = sensorViewModel,
                )
            }
        }
    }
}