package com.example.edoctor.ui.doctor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Import data layer
import com.example.edoctor.data.database.AppDatabase
import com.example.edoctor.data.dao.AppointmentDao
import com.example.edoctor.data.entities.AppointmentEntity
import com.example.edoctor.ui.common.AppointmentCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorAppointmentsScreen(navController: NavController, doctorId: Int) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val appointmentDao = db.appointmentDao()
    val userDao = db.userDao()
    val coroutineScope = rememberCoroutineScope()
    
    var appointments by remember { mutableStateOf<List<AppointmentEntity>>(emptyList()) }
    var patientNames by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadAppointments() {
        coroutineScope.launch {
            try {
                val allAppointments = withContext(Dispatchers.IO) {
                    appointmentDao.getAppointmentsForDoctor(doctorId)
                }
                
                // Filter to only show upcoming appointments (today and future)
                val today = java.time.LocalDate.now().toString()
                val upcomingAppointments = allAppointments.filter { appointment ->
                    appointment.date >= today
                }
                
                appointments = upcomingAppointments.sortedBy { it.date }
                
                // Load patient names for all appointments
                val patientIds = upcomingAppointments.map { it.patientId }.distinct()
                val names = mutableMapOf<Int, String>()
                patientIds.forEach { patientId ->
                    val patient = withContext(Dispatchers.IO) {
                        userDao.getUserById(patientId)
                    }
                    names[patientId] = patient?.name ?: "Unknown Patient"
                }
                patientNames = names
            } catch (e: Exception) {
                appointments = emptyList()
                patientNames = emptyMap()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(doctorId) {
        loadAppointments()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upcoming Appointments (${appointments.size})") },
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
                contentAlignment = Alignment.Center
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No Upcoming Appointments",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "You don't have any upcoming appointments scheduled.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                        "Upcoming Appointments",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(appointments) { appointment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                patientNames[appointment.patientId] ?: "Unknown Patient",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
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