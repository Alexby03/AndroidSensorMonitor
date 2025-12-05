package com.example.sensors.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sensors.SensorApplication
import com.example.sensors.core.AngleCalculator
import com.example.sensors.core.MeasuredResult
import com.example.sensors.core.SensorReader
import com.example.sensors.core.TimeSet
import com.example.sensors.core.sensors.SensorRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface SensorViewModel {
    // MeasuredResult
    // Enum shortMeasure / longMeasure
    // isRunning bool
    //val uiState: StateFlow<SensorUiState>

    //fun start()
    //fun stop()
    //fun setTimeSet(mode: TimeSet)
}

class SensorVM (
    private val sensorRepository: SensorRepository,
    private val angleCalculator: AngleCalculator
): SensorViewModel, ViewModel() {

    //private StateFlow, uiState, history(for

    private val _algo1Angle = MutableStateFlow(0f)
    val algo1Angle: StateFlow<Float> = _algo1Angle

    private val _algo2Angle = MutableStateFlow(0f)
    val algo2Angle: StateFlow<Float> = _algo2Angle

    private var measurementJob: Job? = null

    fun startMeasurement() {
        if (measurementJob != null) return

        measurementJob = viewModelScope.launch {
            val rawFlow = sensorRepository.getSensorEvents()
            angleCalculator.process(rawFlow).collect { result ->
                _algo1Angle.value = result.algo1Angle
                _algo2Angle.value = result.algo2Angle
                // also append result to a list for CSV export if needed
            }
        }
    }

    fun stopMeasurement() {
        measurementJob?.cancel()
        measurementJob = null
    }

    //fun cutOff cut the user of when timer limit is hit

    //fun collectStates

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as SensorApplication)

                SensorVM (
                    sensorRepository = app.sensorRepository,
                    angleCalculator  = app.angleCalculator
                )
            }
        }
    }

    init {
        viewModelScope.launch { }
        //viewModelScope.launch { }
        //viewModelScope.launch { }
    }


}

data class SensorUiState(
    val isMeasuring: Boolean = false,
    val currentAngle: Float? = null,
    val graphPoints: List<Float> = emptyList(),
    val timeSet: TimeSet = TimeSet.SHORT,
    val timeWindowSeconds: Int = 1,
    val hasData: Boolean = false
)

class FakeVM (

) : SensorViewModel {


    /*override fun start() {

    }

    override fun stop() {

    }

    override fun setTimeSet(mode: TimeSet) {

    }*/

}