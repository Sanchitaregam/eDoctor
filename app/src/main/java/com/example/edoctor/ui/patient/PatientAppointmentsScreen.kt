package com.example.edoctor.ui.patient

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Import data layer
import com.example.edoctor.data.database.AppDatabase
import com.example.edoctor.data.dao.AppointmentDao
import com.example.edoctor.data.entities.AppointmentEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientAppointmentsScreen(navController: NavController, doctorId: Int, patientId: Int) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val appointmentDao = db.appointmentDao()
    val userDao = db.userDao()
    val coroutineScope = rememberCoroutineScope()

    var appointments by remember { mutableStateOf<List<AppointmentEntity>>(emptyList()) }
    var doctorNames by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadAppointments() {
        coroutineScope.launch {
            try {
                val loadedAppointments = withContext(Dispatchers.IO) {
                    appointmentDao.getAppointmentsByPatientId(patientId)
                }
                appointments = loadedAppointments
                
                // Load doctor names for all appointments
                val doctorIds = loadedAppointments.map { it.doctorId }.distinct()
                val names = mutableMapOf<Int, String>()
                doctorIds.forEach { doctorId ->
                    val doctor = withContext(Dispatchers.IO) {
                        userDao.getUserById(doctorId)
                    }
                    names[doctorId] = doctor?.name ?: "Unknown Doctor"
                }
                doctorNames = names
            } catch (e: Exception) {
                appointments = emptyList()
                doctorNames = emptyMap()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(patientId) {
        loadAppointments()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Appointments (${appointments.size})") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (appointments.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No Appointments Yet",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "You don't have any appointments booked yet. Book your first appointment to get started!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        navController.navigate("select_doctor/$patientId")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Book New Appointment")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Your Appointments",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
                
                items(appointments) { appointment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Appointment with Dr. ${doctorNames[appointment.doctorId] ?: "Unknown Doctor"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
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
