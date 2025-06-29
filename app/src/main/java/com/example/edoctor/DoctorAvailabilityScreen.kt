package com.example.edoctor

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.edoctor.AvailabilityEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorAvailabilityScreen(navController: NavController, userId: Int) {
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val availabilityDao = db.availabilityDao()
    val coroutineScope = rememberCoroutineScope()

    val allDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val selectedDays = remember { mutableStateListOf<String>() }
    
    // Time state variables
    var fromHour by remember { mutableStateOf(9) }
    var fromAmPm by remember { mutableStateOf("AM") }
    var toHour by remember { mutableStateOf(5) }
    var toAmPm by remember { mutableStateOf("PM") }

    // Convert to time string format for database
    val fromTime = "${String.format("%02d", fromHour)}:00"
    val toTime = "${String.format("%02d", toHour)}:00"

    // Convert to 12-hour format for display
    val fromTimeDisplay = "${fromHour}:00 ${fromAmPm}"
    val toTimeDisplay = "${toHour}:00 ${toAmPm}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Availability") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Select Available Days:", fontSize = 16.sp)

            allDays.chunked(2).forEach { rowDays ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowDays.forEach { day ->
                        FilterChip(
                            selected = day in selectedDays,
                            onClick = {
                                if (day in selectedDays) selectedDays.remove(day)
                                else selectedDays.add(day)
                            },
                            label = { Text(day) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors()
                        )
                    }
                    // Add empty space if only one day in the row
                    if (rowDays.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Text("From Time:", fontSize = 16.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Hours:", modifier = Modifier.width(60.dp))
                OutlinedTextField(
                    value = fromHour.toString(),
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }
                        fromHour = filtered.toIntOrNull()?.coerceIn(1, 12) ?: 1
                    },
                    modifier = Modifier
                        .width(100.dp)
                        .height(56.dp),
                    singleLine = true,
                    trailingIcon = {
                        Column {
                            IconButton(
                                onClick = { if (fromHour < 12) fromHour++ },
                                enabled = fromHour < 12,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowUp, 
                                    contentDescription = "Increase hour",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { if (fromHour > 1) fromHour-- },
                                enabled = fromHour > 1,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowDown, 
                                    contentDescription = "Decrease hour",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                )
                
                Row {
                    FilterChip(
                        selected = fromAmPm == "AM",
                        onClick = { fromAmPm = "AM" },
                        label = { Text("AM") },
                        modifier = Modifier.width(60.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = fromAmPm == "PM",
                        onClick = { fromAmPm = "PM" },
                        label = { Text("PM") },
                        modifier = Modifier.width(60.dp)
                    )
                }
            }

            Text("To Time:", fontSize = 16.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Hours:", modifier = Modifier.width(60.dp))
                OutlinedTextField(
                    value = toHour.toString(),
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }
                        toHour = filtered.toIntOrNull()?.coerceIn(1, 12) ?: 1
                    },
                    modifier = Modifier
                        .width(100.dp)
                        .height(56.dp),
                    singleLine = true,
                    trailingIcon = {
                        Column {
                            IconButton(
                                onClick = { if (toHour < 12) toHour++ },
                                enabled = toHour < 12,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowUp, 
                                    contentDescription = "Increase hour",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { if (toHour > 1) toHour-- },
                                enabled = toHour > 1,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowDown, 
                                    contentDescription = "Decrease hour",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                )
                
                Row {
                    FilterChip(
                        selected = toAmPm == "AM",
                        onClick = { toAmPm = "AM" },
                        label = { Text("AM") },
                        modifier = Modifier.width(60.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = toAmPm == "PM",
                        onClick = { toAmPm = "PM" },
                        label = { Text("PM") },
                        modifier = Modifier.width(60.dp)
                    )
                }
            }

            // Display the selected time range
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    "Selected Time: $fromTimeDisplay to $toTimeDisplay",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            availabilityDao.insertAvailability(
                                AvailabilityEntity(
                                    doctorId = userId,
                                    days = selectedDays.joinToString(","),
                                    fromTime = fromTime,
                                    toTime = toTime
                                )
                            )
                        }
                        Toast.makeText(context, "Availability saved", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Availability")
            }
        }
    }
}
