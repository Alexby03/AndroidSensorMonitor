package com.example.sensors.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sensors.SensorApplication
import com.example.sensors.core.AngleCalculator
import com.example.sensors.core.TimeSet
import com.example.sensors.core.sensors.SensorRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface SensorViewModel {
    val state: StateFlow<SensorUiState>
    val interval: StateFlow<TimeSet>

    fun setTimeSet(timeSet: TimeSet)
    fun startMeasurement()
    fun stopMeasurement()
}

class SensorVM (
    private val sensorRepository: SensorRepository,
    private val angleCalculator: AngleCalculator
): SensorViewModel, ViewModel() {

    private val _state = MutableStateFlow(SensorUiState())
    override val state: StateFlow<SensorUiState>
        get() = _state.asStateFlow()

    private val _interval = MutableStateFlow(TimeSet.LONG)
    override val interval: StateFlow<TimeSet>
        get() = _interval.asStateFlow()

    override fun setTimeSet(timeSet: TimeSet) {
        _interval.value = timeSet
    }

    private var measurementJob: Job? = null

    override fun startMeasurement() {
        if (measurementJob != null) return

        _state.value = _state.value.copy(
            isMeasuring = true,
            currentAlgo1Angle = 0f,
            currentAlgo2Angle = 0f
        )

        measurementJob = viewModelScope.launch {

            val rawFlow = sensorRepository.getSensorEvents()
            var startTimestamp = 0L
            val durationLimitSeconds = when (_interval.value) {
                TimeSet.SHORT -> 1f
                TimeSet.LONG -> 10f
            }

            angleCalculator.process(rawFlow).collect { result ->

                if (startTimestamp == 0L) {
                    startTimestamp = result.timestamp
                }

                val elapsedSeconds = (result.timestamp - startTimestamp) / 1_000f

                if (elapsedSeconds >= durationLimitSeconds) {
                    stopMeasurement()
                    return@collect
                }

                _state.value = _state.value.copy(
                    currentAlgo1Angle = result.algo1Angle,
                    currentAlgo2Angle = result.algo2Angle,
                    currentTimeStamp = result.timestamp
                )

                // CSV SAVE GOES HERE
            }
        }
    }

    override fun stopMeasurement() {
        _state.value = _state.value.copy(isMeasuring = false)
        measurementJob?.cancel()
        measurementJob = null
    }


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
        //viewModelScope.launch { }
    }
}

data class SensorUiState(
    val isMeasuring: Boolean = false,
    val currentAlgo1Angle: Float = 0f,
    val currentAlgo2Angle: Float = 0f,
    val currentTimeStamp: Long = 0L,
    val timeSet: TimeSet = TimeSet.SHORT,
)

class FakeVM() : SensorViewModel {

    override val state = MutableStateFlow(SensorUiState())

    override val interval = MutableStateFlow(TimeSet.LONG)

    override fun setTimeSet(timeSet: TimeSet) {

    }

    override fun startMeasurement() {

    }

    override fun stopMeasurement() {

    }

}