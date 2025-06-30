package com.example.edoctor.ui.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.edoctor.utils.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalHistoryScreen(navController: NavController, patientId: Int) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val appointmentDao = db.appointmentDao()
    val patientDao = db.patientDao()
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var patient by remember { mutableStateOf<PatientEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isEditing by remember { mutableStateOf(false) }
    var isDoctor by remember { mutableStateOf(false) }
    
    // Editable fields
    var knownConditions by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var currentMedications by remember { mutableStateOf("") }
    var pastSurgeries by remember { mutableStateOf("") }
    var familyHistory by remember { mutableStateOf("") }

    fun loadMedicalHistory() {
        coroutineScope.launch {
            try {
                // Check if current user is a doctor
                val currentUserId = sessionManager.getCurrentUserId()
                val currentUserRole = sessionManager.getCurrentUserRole()
                isDoctor = currentUserRole == "doctor"
                
                // Load patient information
                val loadedPatient = withContext(Dispatchers.IO) {
                    patientDao.getPatientById(patientId)
                }
                patient = loadedPatient
                
                // Initialize editable fields
                knownConditions = loadedPatient?.knownConditions ?: ""
                allergies = loadedPatient?.allergies ?: ""
                currentMedications = loadedPatient?.currentMedications ?: ""
                pastSurgeries = loadedPatient?.pastSurgeries ?: ""
                familyHistory = loadedPatient?.familyHistory ?: ""
            } catch (e: Exception) {
                patient = null
            } finally {
                isLoading = false
            }
        }
    }

    fun saveMedicalHistory() {
        coroutineScope.launch {
            try {
                patient?.let { currentPatient ->
                    val updatedPatient = currentPatient.copy(
                        knownConditions = knownConditions.takeIf { it.isNotBlank() },
                        allergies = allergies.takeIf { it.isNotBlank() },
                        currentMedications = currentMedications.takeIf { it.isNotBlank() },
                        pastSurgeries = pastSurgeries.takeIf { it.isNotBlank() },
                        familyHistory = familyHistory.takeIf { it.isNotBlank() }
                    )
                    
                    withContext(Dispatchers.IO) {
                        patientDao.updatePatient(updatedPatient)
                    }
                    
                    patient = updatedPatient
                    isEditing = false
                }
            } catch (e: Exception) {
                // Handle error
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
                },
                actions = {
                    if (isDoctor && !isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
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
                    .background(MaterialTheme.colorScheme.surface),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Patient Info Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    patient?.name ?: "N/A",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${patient?.dob?.let { calculateAge(it) } ?: "N/A"} years • ${patient?.gender ?: "N/A"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bloodtype,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Blood Group: ${patient?.bloodGroup ?: "Not specified"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Medical History Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Medical History",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        if (isEditing) {
                            Row {
                                IconButton(onClick = { 
                                    // Reset to original values
                                    knownConditions = patient?.knownConditions ?: ""
                                    allergies = patient?.allergies ?: ""
                                    currentMedications = patient?.currentMedications ?: ""
                                    pastSurgeries = patient?.pastSurgeries ?: ""
                                    familyHistory = patient?.familyHistory ?: ""
                                    isEditing = false 
                                }) {
                                    Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                                }
                                IconButton(onClick = { saveMedicalHistory() }) {
                                    Icon(Icons.Default.Save, contentDescription = "Save")
                                }
                            }
                        }
                    }
                }

                // Known Conditions
                item {
                    MedicalHistoryCard(
                        title = "Known Conditions",
                        content = patient?.knownConditions,
                        icon = Icons.Default.Info,
                        isEditing = isEditing,
                        value = knownConditions,
                        onValueChange = { knownConditions = it }
                    )
                }

                // Allergies
                item {
                    MedicalHistoryCard(
                        title = "Allergies",
                        content = patient?.allergies,
                        icon = Icons.Default.Warning,
                        isEditing = isEditing,
                        value = allergies,
                        onValueChange = { allergies = it }
                    )
                }

                // Current Medications
                item {
                    MedicalHistoryCard(
                        title = "Current Medications",
                        content = patient?.currentMedications,
                        icon = Icons.Default.Medication,
                        isEditing = isEditing,
                        value = currentMedications,
                        onValueChange = { currentMedications = it }
                    )
                }

                // Past Surgeries
                item {
                    MedicalHistoryCard(
                        title = "Past Surgeries",
                        content = patient?.pastSurgeries,
                        icon = Icons.Default.LocalHospital,
                        isEditing = isEditing,
                        value = pastSurgeries,
                        onValueChange = { pastSurgeries = it }
                    )
                }

                // Family History
                item {
                    MedicalHistoryCard(
                        title = "Family History",
                        content = patient?.familyHistory,
                        icon = Icons.Default.FamilyRestroom,
                        isEditing = isEditing,
                        value = familyHistory,
                        onValueChange = { familyHistory = it }
                    )
                }
            }
        }
    }
}

@Composable
fun MedicalHistoryCard(
    title: String,
    content: String?,
    icon: ImageVector,
    isEditing: Boolean,
    value: String,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isEditing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text("Enter $title") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            } else {
                Text(
                    content?.takeIf { !it.isNullOrBlank() } ?: "No information available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (content.isNullOrBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
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