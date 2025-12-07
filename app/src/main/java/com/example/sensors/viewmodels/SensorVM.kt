package com.example.sensors.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sensors.SensorApplication
import com.example.sensors.core.AngleCalculator
import com.example.sensors.core.MeasuredResult
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
    fun saveCsvToUri(context: Context, uri: Uri)
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

    private val recordedData = mutableListOf<MeasuredResult>()
    private var measurementJob: Job? = null

    override fun startMeasurement() {
        if (measurementJob != null) return
        recordedData.clear()
        val durationLimitSeconds = when (_interval.value) {
            TimeSet.SHORT -> 1f
            TimeSet.LONG -> 10f
        }

        _state.value = _state.value.copy(
            isMeasuring = true,
            graphMaxSeconds = durationLimitSeconds,
            currentAlgo1Angle = 0f,
            currentAlgo2Angle = 0f,
            elapsedSeconds = 0f,
            graphPoints = emptyList()
        )

        measurementJob = viewModelScope.launch {
            val rawFlow = sensorRepository.getSensorEvents()

            var sessionStartTime = 0L

            val durationLimit = when (_interval.value) {
                TimeSet.SHORT -> 1f
                TimeSet.LONG -> 10f
            }

            angleCalculator.process(rawFlow).collect { result ->
                if (sessionStartTime == 0L) {
                    sessionStartTime = result.timestamp
                }

                val elapsed = (result.timestamp - sessionStartTime) / 1_000f

                recordedData.add(result)

                if (elapsed >= durationLimit) {
                    _state.value = _state.value.copy(elapsedSeconds = durationLimit)
                    stopMeasurement()
                    return@collect
                }

                _state.value = _state.value.copy(
                    currentAlgo1Angle = result.algo1Angle,
                    currentAlgo2Angle = result.algo2Angle,
                    elapsedSeconds = elapsed,
                    graphPoints = _state.value.graphPoints + Pair(elapsed, result.algo2Angle)
                )
            }
        }
    }

    override fun stopMeasurement() {
        _state.value = _state.value.copy(isMeasuring = false)
        measurementJob?.cancel()
        measurementJob = null
    }

    override fun saveCsvToUri(context: Context, uri: Uri) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val writer = outputStream.bufferedWriter()

                writer.write("timestamp_ms,algo1_angle,algo2_angle\n")

                val startTime = recordedData.firstOrNull()?.timestamp ?: 0L

                recordedData.forEach { data ->
                    val t = data.timestamp - startTime
                    writer.write("$t,${data.algo1Angle},${data.algo2Angle}\n")
                }
                writer.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
}

data class SensorUiState(
    val isMeasuring: Boolean = false,
    val graphMaxSeconds: Float = 1f,
    val currentAlgo1Angle: Float = 0f,
    val currentAlgo2Angle: Float = 0f,
    val elapsedSeconds: Float = 0f,
    val timeSet: TimeSet = TimeSet.SHORT,
    val graphPoints: List<Pair<Float, Float>> = emptyList()
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

    override fun saveCsvToUri(context: Context, uri: Uri){

    }

}