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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DoctorProfileScreen(navController: NavController, userId: Int) {
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val userDao = db.userDao()
    var doctorName by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        val user = withContext(Dispatchers.IO) {
            userDao.getUserById(userId)
        }
        doctorName = user?.name ?: ""
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
                IconButton(onClick = { navController.navigate("edit_doctor_profile/$userId") }) {
                    Icon(Icons.Default.Person, contentDescription = "Profile")
                }
                IconButton(onClick = { /* Show notifications */ }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                }
            }
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
                    navController.navigate("patients_screen/$userId")
                }
                FeatureCard("Messages", R.drawable.ic_chat) {
                    navController.navigate("messages_screen/$userId")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeatureCard("Availability", R.drawable.ic_calendar) {
                    navController.navigate("doctor_availability/$userId")
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
    val db = remember { DatabaseProvider.getDatabase(context) }
    val userDao = db.userDao()
    val coroutineScope = rememberCoroutineScope()

    var user by remember { mutableStateOf<UserEntity?>(null) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf(0) }

    LaunchedEffect(userId) {
        val fetchedUser = withContext(Dispatchers.IO) {
            userDao.getUserById(userId)
        }
        fetchedUser?.let {
            user = it
            name = it.name
            phone = it.phone
            experience = it.experience?.toIntOrNull() ?: 0
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
                            val updated = it.copy(name = name, phone = phone, experience = experience.toString())
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

