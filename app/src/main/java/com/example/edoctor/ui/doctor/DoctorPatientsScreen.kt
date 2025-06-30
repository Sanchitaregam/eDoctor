package com.example.edoctor.ui.doctor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Import data layer
import com.example.edoctor.data.database.AppDatabase
import com.example.edoctor.data.dao.PatientDao
import com.example.edoctor.data.dao.AppointmentDao
import com.example.edoctor.data.entities.PatientEntity
import com.example.edoctor.data.entities.AppointmentEntity
import com.example.edoctor.utils.SessionManager
import com.example.edoctor.R
import com.example.edoctor.ui.common.PatientCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorPatientsScreen(navController: NavController, doctorId: Int) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val appointmentDao = db.appointmentDao()
    val patientDao = db.patientDao()
    val sessionManager = remember { SessionManager(context) }
    
    var patients by remember { mutableStateOf<List<PatientEntity>>(emptyList()) }
    var appointments by remember { mutableStateOf<List<AppointmentEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(doctorId) {
        val currentDoctorId = sessionManager.getCurrentUserId()
        if (currentDoctorId != -1) {
            try {
                // Get all appointments for this doctor
                val loadedAppointments = withContext(Dispatchers.IO) {
                    appointmentDao.getAppointmentsForDoctor(currentDoctorId)
                }
                appointments = loadedAppointments
                
                // Get unique patient IDs from appointments
                val patientIds = loadedAppointments.map { it.patientId }.distinct()
                
                // Load patient details for each patient ID
                val patientList = mutableListOf<PatientEntity>()
                patientIds.forEach { patientId ->
                    val patient = withContext(Dispatchers.IO) {
                        patientDao.getPatientById(patientId)
                    }
                    if (patient != null) {
                        patientList.add(patient)
                    }
                }
                
                patients = patientList
            } catch (e: Exception) {
                patients = emptyList()
                appointments = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Patients (${patients.size})") },
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
        } else if (patients.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No Patients Yet",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "You don't have any patients yet. Patients will appear here once they book appointments with you.",
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
                        "Your Patients",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(patients) { patient ->
                    PatientCard(
                        patient = patient,
                        appointmentCount = appointments.count { it.patientId == patient.id },
                        onViewMedicalHistory = { patientId -> 
                            navController.navigate("medical_history/$patientId")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PatientCard(
    patient: PatientEntity,
    appointmentCount: Int,
    onViewMedicalHistory: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.default_profile),
                    contentDescription = patient.name,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = patient.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = patient.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$appointmentCount appointment${if (appointmentCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onViewMedicalHistory(patient.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.MedicalServices,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Medical History")
                }
            }
        }
    }
}