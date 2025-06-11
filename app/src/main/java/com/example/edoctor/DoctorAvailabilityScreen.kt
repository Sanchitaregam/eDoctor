package com.example.edoctor

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    var fromTime by remember { mutableStateOf("09:00") }
    var toTime by remember { mutableStateOf("17:00") }

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
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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
                }
            }

            OutlinedTextField(
                value = fromTime,
                onValueChange = { fromTime = it },
                label = { Text("From Time (HH:MM)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = toTime,
                onValueChange = { toTime = it },
                label = { Text("To Time (HH:MM)") },
                modifier = Modifier.fillMaxWidth()
            )

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
