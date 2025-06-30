package com.example.edoctor

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorProfileScreen(navController: NavController, userId: Int) {
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
                    IconButton(onClick = { /* Show notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
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
                                doctorInfo?.specialization ?: "General Physician",
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Today's Appointments",
                        value = upcomingAppointments.count { it.date == "2024-01-15" }.toString(), // Placeholder date
                        icon = Icons.Default.CalendarToday,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Patients",
                        value = upcomingAppointments.map { it.patientId }.distinct().size.toString(),
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Upcoming Appointments Section
            item {
                Text(
                    "Upcoming Appointments",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (upcomingAppointments.isNotEmpty()) {
                items(upcomingAppointments) { appointment ->
                    AppointmentCard(appointment = appointment)
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No upcoming appointments",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Text(
                    "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionCard(
                        title = "Set Availability",
                        icon = Icons.Default.CalendarToday,
                        onClick = { navController.navigate("doctor_availability/$currentUserId") },
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title = "Edit Profile",
                        icon = Icons.Default.Edit,
                        onClick = { navController.navigate("edit_doctor_profile/$currentUserId") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionCard(
                        title = "View Patients",
                        icon = Icons.Default.Person,
                        onClick = { navController.navigate("patients_screen/$currentUserId") },
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title = "Health Tips",
                        icon = Icons.Default.Notifications,
                        onClick = { navController.navigate("health_tips") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Settings and Logout
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(
                                onClick = { navController.navigate("settings") }
                            ) {
                                Text("Settings")
                            }
                            TextButton(
                                onClick = {
                                    sessionManager.clearLoginSession()
                                    navController.navigate("welcome") { popUpTo(0) { inclusive = true } }
                                }
                            ) {
                                Text("Logout")
                            }
                        }
                    }
                }
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
                            ProfileInfoRow("Rating", "${doctorInfo!!.rating}/5.0")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDoctorProfileScreen(navController: NavController, userId: Int) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val userDao = db.userDao()
    val coroutineScope = rememberCoroutineScope()

    var user by remember { mutableStateOf<UserEntity?>(null) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf(0) }
    var specialization by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        val fetchedUser = withContext(Dispatchers.IO) {
            userDao.getUserById(userId)
        }
        fetchedUser?.let {
            user = it
            name = it.name
            phone = it.phone
            experience = it.experience?.toIntOrNull() ?: 0
            specialization = it.specialization ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
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
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = specialization,
                onValueChange = { specialization = it },
                label = { Text("Specialization") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = experience.toString(),
                onValueChange = { newValue ->
                    val filtered = newValue.filter { it.isDigit() }
                    experience = filtered.toIntOrNull() ?: 0
                },
                label = { Text("Experience (years)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    Row {
                        IconButton(
                            onClick = { if (experience > 0) experience-- },
                            enabled = experience > 0
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease")
                        }
                        IconButton(
                            onClick = { if (experience < 60) experience++ },
                            enabled = experience < 60
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase")
                        }
                    }
                }
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        user?.let {
                            val updated = it.copy(
                                name = name,
                                phone = phone,
                                experience = experience.toString(),
                                specialization = specialization
                            )
                            withContext(Dispatchers.IO) {
                                userDao.updateUser(updated)
                            }
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
} 