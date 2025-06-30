package com.example.edoctor.ui.patient

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Import data layer
import com.example.edoctor.data.database.AppDatabase
import com.example.edoctor.data.dao.UserDao
import com.example.edoctor.data.dao.AppointmentDao
import com.example.edoctor.data.entities.UserEntity
import com.example.edoctor.data.entities.AppointmentEntity
import com.example.edoctor.utils.SessionManager
import com.example.edoctor.R
import com.example.edoctor.ui.common.StatCard
import com.example.edoctor.ui.common.ActionCard
import com.example.edoctor.ui.common.ProfileInfoRow
import com.example.edoctor.ui.common.PatientAppointmentCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDashboardScreen(navController: NavController, userId: Int) {
    val context = LocalContext.current
    val userDao = AppDatabase.getDatabase(context).userDao()
    val appointmentDao = AppDatabase.getDatabase(context).appointmentDao()
    val sessionManager = remember { SessionManager(context) }
    
    var patient by remember { mutableStateOf<UserEntity?>(null) }
    var bookedAppointments by remember { mutableStateOf<List<AppointmentEntity>>(emptyList()) }
    var totalAppointmentCount by remember { mutableStateOf(0) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<UserEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        val currentUserId = sessionManager.getCurrentUserId()
        if (currentUserId != -1) {
            val loadedPatient = withContext(Dispatchers.IO) {
                userDao.getUserById(currentUserId)
            }
            patient = loadedPatient
            
            // Load booked appointments
            val appointments = withContext(Dispatchers.IO) {
                appointmentDao.getAppointmentsByPatientId(currentUserId)
            }
            totalAppointmentCount = appointments.size
            bookedAppointments = appointments.sortedBy { it.date }.take(5) // Show next 5 appointments
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patient Dashboard") },
                actions = {
                    IconButton(
                        onClick = { 
                            Log.d("PatientProfile", "Profile icon clicked, patient: ${patient?.name}")
                            showProfileDialog = true 
                        }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_profile),
                            contentDescription = "Profile",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF7F7F7)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.default_profile),
                            contentDescription = "Patient Profile",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "Hi, ${patient?.name ?: "Patient"}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Welcome to eDoctor",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Quick Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    StatCard(
                        title = "Booked Appointments",
                        value = totalAppointmentCount.toString(),
                        icon = Icons.Default.CalendarToday
                    )
                }
            }

            // Quick Actions
            item {
                Text(
                    "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                ActionCard(
                    title = "Book New Appointment",
                    subtitle = "Book appointment with available doctors",
                    icon = Icons.Default.CalendarToday,
                    onClick = {
                        val currentUserId = sessionManager.getCurrentUserId()
                        navController.navigate("select_doctor/$currentUserId")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                ActionCard(
                    title = "My Appointments",
                    subtitle = "View your booked appointments",
                    icon = Icons.Default.CalendarToday,
                    onClick = {
                        val currentUserId = sessionManager.getCurrentUserId()
                        navController.navigate("patient_appointments/0/$currentUserId")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                ActionCard(
                    title = "Medical History",
                    subtitle = "View your medical records and history",
                    icon = Icons.Default.Person,
                    onClick = { 
                        val currentUserId = sessionManager.getCurrentUserId()
                        navController.navigate("medical_history/$currentUserId")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                ActionCard(
                    title = "Settings",
                    subtitle = "Edit profile, change email/password, logout",
                    icon = Icons.Default.Settings,
                    onClick = { navController.navigate("settings") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Profile Dialog
        if (showProfileDialog && patient != null) {
            AlertDialog(
                onDismissRequest = { showProfileDialog = false },
                title = { Text("Patient Profile") },
                text = {
                    Column {
                        ProfileInfoRow("Name", patient?.name ?: "N/A")
                        ProfileInfoRow("Email", patient?.email ?: "N/A")
                        ProfileInfoRow("Phone", patient?.phone ?: "N/A")
                        ProfileInfoRow("Gender", patient?.gender ?: "N/A")
                        ProfileInfoRow("Date of Birth", patient?.dob ?: "N/A")
                        ProfileInfoRow("Address", patient?.address ?: "N/A")
                        ProfileInfoRow("Blood Group", patient?.bloodGroup ?: "N/A")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showProfileDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
} 