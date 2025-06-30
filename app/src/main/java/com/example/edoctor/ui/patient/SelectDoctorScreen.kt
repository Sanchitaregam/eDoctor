package com.example.edoctor.ui.patient

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.edoctor.data.database.AppDatabase
import com.example.edoctor.data.entities.UserEntity
import com.example.edoctor.data.entities.AvailabilityEntity
import com.example.edoctor.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorSelectionScreen(navController: NavController, patientId: Int) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val userDao = db.userDao()
    val availabilityDao = db.availabilityDao()
    
    var doctors by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
    var doctorAvailabilities by remember { mutableStateOf<Map<Int, List<AvailabilityEntity>>>(emptyMap()) }
    var patient by remember { mutableStateOf<UserEntity?>(null) }

    LaunchedEffect(patientId) {
        val loadedDoctors = withContext(Dispatchers.IO) {
            userDao.getAllDoctors()
        }
        doctors = loadedDoctors
        
        val loadedPatient = withContext(Dispatchers.IO) {
            userDao.getUserById(patientId)
        }
        patient = loadedPatient
        
        // Load availability for each doctor
        val availabilities = mutableMapOf<Int, List<AvailabilityEntity>>()
        loadedDoctors.forEach { doctor ->
            val doctorAvailability = withContext(Dispatchers.IO) {
                availabilityDao.getAvailabilityForDoctor(doctor.id)
            }
            availabilities[doctor.id] = doctorAvailability
        }
        doctorAvailabilities = availabilities
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Doctor") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Available Doctors",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(doctors) { doctor ->
                DoctorAvailabilityCard(
                    doctor = doctor,
                    availability = doctorAvailabilities[doctor.id] ?: emptyList(),
                    onBookClick = {
                        navController.navigate("calendar/${doctor.id}/$patientId/${patient?.name ?: ""}")
                    }
                )
            }
        }
    }
}

@Composable
fun DoctorAvailabilityCard(
    doctor: UserEntity,
    availability: List<AvailabilityEntity>,
    onBookClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.default_profile),
                    contentDescription = doctor.name,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        doctor.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        doctor.specialization ?: "Not specified",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (doctor.experience != null) {
                        Text(
                            "${doctor.experience} years experience",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (availability.isNotEmpty()) {
                Text(
                    "Availability:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                val weekOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                val daysSet = availability.map { it.days }.toSet()
                val orderedDays = weekOrder.filter { daysSet.contains(it) }
                val daysLabel = orderedDays.joinToString(", ")
                Text(
                    "$daysLabel: 9:00 AM - 5:00 PM",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "No availability set",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onBookClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = availability.isNotEmpty()
            ) {
                Text("Book Appointment")
            }
        }
    }
} 