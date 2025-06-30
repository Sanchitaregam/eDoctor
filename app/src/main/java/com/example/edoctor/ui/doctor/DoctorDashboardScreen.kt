package com.example.edoctor.ui.doctor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
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
import com.example.edoctor.ui.common.AppointmentCard
import com.example.edoctor.ui.common.ProfileInfoRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDashboardScreen(navController: NavController, userId: Int) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val userDao = db.userDao()
    val appointmentDao = db.appointmentDao()
    val sessionManager = remember { SessionManager(context) }
    var doctorName by remember { mutableStateOf("") }
    var showProfileDialog by remember { mutableStateOf(false) }
    var doctorInfo by remember { mutableStateOf<UserEntity?>(null) }
    var upcomingAppointments by remember { mutableStateOf<List<AppointmentEntity>>(emptyList()) }

    // Get current logged-in user ID from session
    val currentUserId = sessionManager.getCurrentUserId()
    val isLoggedIn = sessionManager.isLoggedIn()

    LaunchedEffect(currentUserId) {
        if (currentUserId > 0 && isLoggedIn) {
            val user = withContext(Dispatchers.IO) {
                userDao.getUserById(currentUserId)
            }
            doctorName = user?.name ?: ""
            doctorInfo = user
            
            // Load upcoming appointments
            val appointments = withContext(Dispatchers.IO) {
                appointmentDao.getAppointmentsForDoctor(currentUserId)
            }
            upcomingAppointments = appointments.sortedBy { it.date }.take(5) // Show next 5 appointments
        }
    }

    // Always reload data when dialog is opened
    LaunchedEffect(showProfileDialog) {
        if (showProfileDialog) {
            val userId = sessionManager.getCurrentUserId()
            val loggedIn = sessionManager.isLoggedIn()
            
            if (userId > 0 && loggedIn) {
                val user = withContext(Dispatchers.IO) {
                    userDao.getUserById(userId)
                }
                doctorInfo = user
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doctor Dashboard") },
                actions = {
                    IconButton(onClick = { showProfileDialog = true }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
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
                            contentDescription = "Doctor Profile",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "Hi, Dr. $doctorName",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                doctorInfo?.specialization ?: "Not specified",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (doctorInfo?.experience != null) {
                                Text(
                                    "${doctorInfo!!.experience} years of experience",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                        title = "Today's Appointments",
                        value = upcomingAppointments.count { it.date == "2024-01-15" }.toString(), // Placeholder date
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
                    title = "Upcoming Appointments",
                    subtitle = "View your upcoming appointments",
                    icon = Icons.Default.CalendarToday,
                    onClick = { navController.navigate("doctor_appointments/$currentUserId") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                ActionCard(
                    title = "Set Availability",
                    subtitle = "Set your available weekdays",
                    icon = Icons.Default.CalendarToday,
                    onClick = { navController.navigate("doctor_availability/$currentUserId") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                ActionCard(
                    title = "View Patients",
                    subtitle = "View your patient list and appointments",
                    icon = Icons.Default.Person,
                    onClick = { navController.navigate("patients_screen/$currentUserId") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                ActionCard(
                    title = "Settings",
                    subtitle = "Edit profile, change email/password, and logout",
                    icon = Icons.Default.Settings,
                    onClick = { navController.navigate("settings") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Profile Dialog
        if (showProfileDialog) {
            AlertDialog(
                onDismissRequest = { showProfileDialog = false },
                title = { Text("Profile Information") },
                text = {
                    Column {
                        if (doctorInfo != null) {
                            ProfileInfoRow("Name", doctorInfo!!.name.ifEmpty { "Not set" })
                            ProfileInfoRow("Email", doctorInfo!!.email.ifEmpty { "Not set" })
                            ProfileInfoRow("Phone", doctorInfo!!.phone.ifEmpty { "Not set" })
                            ProfileInfoRow("Experience", "${doctorInfo!!.experience ?: "0"} years")
                            ProfileInfoRow("Specialization", doctorInfo!!.specialization ?: "Not specified")
                            ProfileInfoRow("Date of Birth", doctorInfo!!.dob ?: "Not set")
                            ProfileInfoRow("Gender", doctorInfo!!.gender.ifEmpty { "Not set" })
                        } else {
                            Text(
                                "Profile data not found in database.\n\nThis may happen if the database was reset or the user account was deleted.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Please log in again to refresh your session.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showProfileDialog = false }) {
                        Text("Close")
                    }
                },
                dismissButton = {
                    if (doctorInfo == null) {
                        TextButton(
                            onClick = { 
                                sessionManager.clearLoginSession()
                                showProfileDialog = false
                                navController.navigate("welcome") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        ) {
                            Text("Logout")
                        }
                    }
                }
            )
        }
    }
} 