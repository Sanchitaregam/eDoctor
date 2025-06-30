package com.example.edoctor.ui.patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
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
import com.example.edoctor.data.dao.PatientDao
import com.example.edoctor.data.entities.AppointmentEntity
import com.example.edoctor.data.entities.PatientEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalHistoryScreen(navController: NavController, patientId: Int) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val appointmentDao = db.appointmentDao()
    val patientDao = db.patientDao()
    val coroutineScope = rememberCoroutineScope()
    
    var patient by remember { mutableStateOf<PatientEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadMedicalHistory() {
        coroutineScope.launch {
            try {
                // Load patient information
                val loadedPatient = withContext(Dispatchers.IO) {
                    patientDao.getPatientById(patientId)
                }
                patient = loadedPatient
            } catch (e: Exception) {
                patient = null
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(patientId) {
        loadMedicalHistory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical History") },
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Medical History Fields Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Medical History",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Known Conditions: ${patient?.knownConditions?.takeIf { !it.isNullOrBlank() } ?: "None reported"}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Allergies: ${patient?.allergies?.takeIf { !it.isNullOrBlank() } ?: "None reported"}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Current Medications: ${patient?.currentMedications?.takeIf { !it.isNullOrBlank() } ?: "None reported"}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Past Surgeries: ${patient?.pastSurgeries?.takeIf { !it.isNullOrBlank() } ?: "None reported"}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Family History: ${patient?.familyHistory?.takeIf { !it.isNullOrBlank() } ?: "None reported"}")
                        }
                    }
                }
            }
        }
    }
}

private fun calculateAge(dob: String): Int {
    return try {
        val birthDate = java.time.LocalDate.parse(dob)
        val currentDate = java.time.LocalDate.now()
        java.time.Period.between(birthDate, currentDate).years
    } catch (e: Exception) {
        0
    }
} 