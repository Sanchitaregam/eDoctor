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
            Text(text = "Hi, Dr. John Doe", fontSize = 22.sp)

            Row {
                IconButton(onClick = { /* Navigate to profile settings */ }) {
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
                FeatureCard("Profile", R.drawable.ic_profile) {
                    navController.navigate("doctor_full_profile/$userId")
                }
                FeatureCard("Appointments", R.drawable.ic_calendar) {
                    navController.navigate("doctor_availability/$userId")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                FeatureCard("Edit Profile", R.drawable.ic_profile) {
                    navController.navigate("edit_doctor_profile/$userId")
                }
                FeatureCard("Availability", R.drawable.ic_calendar) {
                    navController.navigate("doctor_availability/$userId")
                }
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

@Composable
fun DoctorFullProfileScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Doctor Full Profile Screen")
    }
}

@Composable
fun AppointmentsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Appointments Screen")
    }
}

@Composable
fun PatientsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Patients Screen")
    }
}

@Composable
fun MessagesScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Messages Screen")
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
    var experience by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        val fetchedUser = withContext(Dispatchers.IO) {
            userDao.getUserById(userId)
        }
        fetchedUser?.let {
            user = it
            name = it.name
            phone = it.phone
            experience = it.experience ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Edit Profile") }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            })
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
                value = experience,
                onValueChange = { experience = it },
                label = { Text("Experience (e.g. 10 years)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        user?.let {
                            val updated = it.copy(name = name, phone = phone, experience = experience)
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
