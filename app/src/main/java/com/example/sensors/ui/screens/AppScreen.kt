package com.example.sensors.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sensors.core.TimeSet
import com.example.sensors.viewmodels.SensorViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    vm: SensorViewModel
) {
    val state by vm.state.collectAsState()
    val interval by vm.interval.collectAsState()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { vm.saveCsvToUri(context, it) }
    }

    var showSaveDialog by remember { mutableStateOf(false) }

    var wasMeasuring by remember { mutableStateOf(false) }

    if (wasMeasuring && !state.isMeasuring && state.graphPoints.isNotEmpty()) {
        showSaveDialog = true
    }
    wasMeasuring = state.isMeasuring

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Shoulder Abduction Adduction Test") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Select Duration", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TimeSet.entries.forEach { mode ->
                    val isSelected = (interval == mode)
                    val label = if (mode == TimeSet.SHORT) "Short (1s)" else "Long (10s)"
                    val enabled = !state.isMeasuring

                    OutlinedButton(
                        onClick = { vm.setTimeSet(mode) },
                        modifier = Modifier.weight(1f).padding(4.dp),
                        enabled = enabled,
                        colors = if (isSelected) ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text(label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Angle", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(
                            "%.1f°".format(state.currentAlgo2Angle),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .border(1.dp, Color.LightGray)
                            .background(Color.Black.copy(alpha = 0.03f))
                    ) {
                        LiveGraph(
                            points = state.graphPoints,
                            maxTime = state.graphMaxSeconds,
                            maxAngle = 90f
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Time: %.2fs / %.0fs".format(state.elapsedSeconds, state.graphMaxSeconds),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { if (state.isMeasuring) vm.stopMeasurement() else vm.startMeasurement() },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isMeasuring) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (state.isMeasuring) "STOP" else "START", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = { Text("Recording Finished") },
                text = { Text("Do you want to save the sensor data to a CSV file?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSaveDialog = false
                            exportLauncher.launch("sensor_data_${System.currentTimeMillis()}.csv")
                        }
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun LiveGraph(
    points: List<Pair<Float, Float>>,
    maxTime: Float,
    maxAngle: Float
) {
    val lineColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        val width = size.width
        val height = size.height

        val ySteps = 3
        for (i in 0..ySteps) {
            val y = height - (i * height / ySteps)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (points.isNotEmpty()) {
            val path = Path()

            points.forEachIndexed { index, point ->
                val (t, angle) = point

                val x = (t / maxTime) * width

                val safeAngle = angle.coerceIn(0f, maxAngle)
                val y = height - (safeAngle / maxAngle) * height

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}
