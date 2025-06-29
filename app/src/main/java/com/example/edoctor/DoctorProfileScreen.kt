package com.example.edoctor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DoctorProfileScreen(navController: NavController, userId: Int) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val userDao = db.userDao()
    val sessionManager = remember { SessionManager(context) }
    var doctorName by remember { mutableStateOf("") }
    var showProfileDialog by remember { mutableStateOf(false) }
    var doctorInfo by remember { mutableStateOf<UserEntity?>(null) }

    // Get current logged-in user ID from session
    val currentUserId = sessionManager.getCurrentUserId()
    val isLoggedIn = sessionManager.isLoggedIn()
    android.util.Log.d("DoctorProfile", "Current logged-in User ID: $currentUserId")
    android.util.Log.d("DoctorProfile", "Is logged in: $isLoggedIn")

    LaunchedEffect(currentUserId) {
        if (currentUserId > 0 && isLoggedIn) {
            android.util.Log.d("DoctorProfile", "Loading data for current user: $currentUserId")
            val user = withContext(Dispatchers.IO) {
                userDao.getUserById(currentUserId)
            }
            doctorName = user?.name ?: ""
            doctorInfo = user
            android.util.Log.d("DoctorProfile", "Loaded user: $user")
            android.util.Log.d("DoctorProfile", "Doctor name: $doctorName")
        } else {
            android.util.Log.d("DoctorProfile", "No user logged in or invalid user ID")
        }
    }

    // Always reload data when dialog is opened
    LaunchedEffect(showProfileDialog) {
        if (showProfileDialog) {
            val userId = sessionManager.getCurrentUserId()
            val loggedIn = sessionManager.isLoggedIn()
            android.util.Log.d("DoctorProfile", "Dialog opened - User ID: $userId, Logged in: $loggedIn")
            
            if (userId > 0 && loggedIn) {
                val user = withContext(Dispatchers.IO) {
                    userDao.getUserById(userId)
                }
                doctorInfo = user
                android.util.Log.d("DoctorProfile", "Dialog - Loaded user: $user")
                
                // Debug: Check all users in database
                val allUsers = withContext(Dispatchers.IO) {
                    userDao.getAllUsers()
                }
                android.util.Log.d("DoctorProfile", "All users in database: $allUsers")
                
                // Debug: Check all doctors specifically
                val allDoctors = withContext(Dispatchers.IO) {
                    userDao.getAllDoctors()
                }
                android.util.Log.d("DoctorProfile", "All doctors in database: $allDoctors")
            } else {
                android.util.Log.d("DoctorProfile", "Dialog - No user logged in or invalid user ID")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Hi, Dr. $doctorName", fontSize = 22.sp)

            Row {
                IconButton(onClick = { showProfileDialog = true }) {
                    Icon(Icons.Default.Person, contentDescription = "Profile")
                }
                IconButton(onClick = { /* Show notifications */ }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
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

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.default_profile),
            contentDescription = "Doctor Profile",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeatureCard("Patients", R.drawable.ic_stethoscope) {
                    navController.navigate("patients_screen/$currentUserId")
                }
                FeatureCard("Messages", R.drawable.ic_chat) {
                    navController.navigate("messages_screen/$currentUserId")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeatureCard("Availability", R.drawable.ic_calendar) {
                    navController.navigate("doctor_availability/$currentUserId")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FeatureCard("Health Tips", R.drawable.ic_file) {
                navController.navigate("health_tips")
            }

            FeatureCard("Settings", R.drawable.ic_settings) {
                navController.navigate("settings")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = "Stay tuned for updates...", color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun FeatureCard(title: String, iconResId: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(140.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD6EFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = title,
                modifier = Modifier.size(32.dp)
            )
            Text(text = title, color = Color.Black)
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

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

