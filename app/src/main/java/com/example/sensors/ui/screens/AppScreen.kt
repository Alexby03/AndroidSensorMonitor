package com.example.sensors.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sensors.core.TimeSet
import com.example.sensors.viewmodels.FakeVM
import com.example.sensors.viewmodels.SensorViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    vm: SensorViewModel
) {
    // Collect flows from the ViewModel
    val state by vm.state.collectAsState()
    val interval by vm.interval.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sensor Home") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ---------------------------------------------------
            // TITLE
            // ---------------------------------------------------
            Text(
                text = "Arm Angle Trainer",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ---------------------------------------------------
            // TIME WINDOW SELECTOR
            // ---------------------------------------------------
            Text(
                text = "Select Duration",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TimeSet.values().forEach { mode ->
                    val isSelected = (interval == mode)
                    val label = if (mode == TimeSet.SHORT) "Short (1s)" else "Long (10s)"

                    // Changing logic: only allow changing mode if NOT measuring
                    val enabled = !state.isMeasuring

                    OutlinedButton(
                        onClick = { vm.setTimeSet(mode) }, // Make sure you added setTimeSet to interface!
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                        enabled = enabled,
                        colors = if (isSelected) {
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            ButtonDefaults.outlinedButtonColors()
                        }
                    ) {
                        Text(label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ---------------------------------------------------
            // LIVE DATA DISPLAY (Angle 1, Angle 2, Timestamp, Status)
            // ---------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Live Sensor Data",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DataField("Algo 1 (Accel)", "%.1f°".format(state.currentAlgo1Angle))
                        DataField("Algo 2 (Fusion)", "%.1f°".format(state.currentAlgo2Angle))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Simple Timestamp (ns -> seconds for display)
                        val seconds = state.currentTimeStamp / 1_000_000_000.0
                        // Just showing raw seconds for debugging/visual feedback
                        DataField("Timestamp", "%.2fs".format(seconds))

                        // Status Badge
                        StatusBadge(isMeasuring = state.isMeasuring)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Push button to bottom area

            // ---------------------------------------------------
            // START / STOP BUTTON
            // ---------------------------------------------------
            Button(
                onClick = {
                    if (state.isMeasuring) {
                        vm.stopMeasurement()
                    } else {
                        vm.startMeasurement()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isMeasuring) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (state.isMeasuring) "STOP MEASUREMENT" else "START RECORDING",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Helper Composable for Data Fields
@Composable
fun DataField(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Helper Composable for Status Badge
@Composable
fun StatusBadge(isMeasuring: Boolean) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isMeasuring) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (isMeasuring) Color.Green else Color.Red,
                        shape = RoundedCornerShape(50)
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isMeasuring) "Running" else "Stopped",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppScreenPreview() {
    AppScreen(vm = FakeVM())
}
