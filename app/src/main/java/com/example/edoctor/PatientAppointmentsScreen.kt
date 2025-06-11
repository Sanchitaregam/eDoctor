package com.example.edoctor

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientAppointmentsScreen(navController: NavController, doctorId: Int) {
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val appointmentDao = db.appointmentDao()
    val coroutineScope = rememberCoroutineScope()

    var appointments by remember { mutableStateOf<List<AppointmentEntity>>(emptyList()) }

    var patientName by remember { mutableStateOf(TextFieldValue()) }
    var date by remember { mutableStateOf(TextFieldValue()) }
    var time by remember { mutableStateOf(TextFieldValue()) }
    var notes by remember { mutableStateOf(TextFieldValue()) }

    LaunchedEffect(doctorId) {
        appointments = withContext(Dispatchers.IO) {
            appointmentDao.getAppointmentsForDoctor(doctorId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointments") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Add Appointment", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = patientName,
                onValueChange = { patientName = it },
                label = { Text("Patient Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (e.g., 2025-06-10)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Time (e.g., 10:30 AM)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        val newAppointment = AppointmentEntity(
                            doctorId = doctorId,
                            patientName = patientName.text,
                            date = date.text,
                            time = time.text,
                            notes = notes.text
                        )
                        withContext(Dispatchers.IO) {
                            appointmentDao.addAppointment(newAppointment)
                            appointments = appointmentDao.getAppointmentsForDoctor(doctorId)
                        }
                        patientName = TextFieldValue()
                        date = TextFieldValue()
                        time = TextFieldValue()
                        notes = TextFieldValue()
                        Toast.makeText(context, "Appointment added", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Appointment")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Upcoming Appointments", style = MaterialTheme.typography.titleMedium)

            LazyColumn {
                items(appointments) { appointment ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Patient: ${appointment.patientName}")
                            Text("Date: ${appointment.date}")
                            Text("Time: ${appointment.time}")
                            if (!appointment.notes.isNullOrEmpty()) {
                                Text("Notes: ${appointment.notes}")
                            }
                        }
                    }
                }
            }
        }
    }
}
