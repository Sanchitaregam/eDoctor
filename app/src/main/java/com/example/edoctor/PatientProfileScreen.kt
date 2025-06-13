package com.example.edoctor

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PatientProfileScreen(navController: NavController, userId: Int) {
    val context = LocalContext.current
    val userDao = AppDatabase.getDatabase(context).userDao()
    Log.d("PatientProfile", "userId received: $userId")

    var patient by remember { mutableStateOf<UserEntity?>(null) }
    var doctors by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        val loadedPatient: UserEntity?
        val loadedDoctors: List<UserEntity>

        withContext(Dispatchers.IO) {
            loadedPatient = userDao.getUserById(userId)

            if (userDao.getAllDoctors().isEmpty()) {
                // Insert dummy doctors with fixed unique IDs > 1000 to avoid collision
                userDao.insertUser(
                    UserEntity(
                        id = 1001,
                        name = "Dr. Rohan Patel",
                        role = "doctor",
                        experience = "Cardiologist",
                        rating = 4.5f,
                        ratingCount = 10
                    )
                )
                userDao.insertUser(
                    UserEntity(
                        id = 1002,
                        name = "Dr. Anita Sharma",
                        role = "doctor",
                        experience = "Dermatologist",
                        rating = 4.2f,
                        ratingCount = 8
                    )
                )
                userDao.insertUser(
                    UserEntity(
                        id = 1003,
                        name = "Dr. Imran Khan",
                        role = "doctor",
                        experience = "Pediatrician",
                        rating = 4.8f,
                        ratingCount = 15
                    )
                )
                userDao.insertUser(
                    UserEntity(
                        id = 1004,
                        name = "Dr. Neha Verma",
                        role = "doctor",
                        experience = "Neurologist",
                        rating = 4.6f,
                        ratingCount = 12
                    )
                )
            }

            loadedDoctors = userDao.getAllDoctors()
        }

        patient = loadedPatient
        doctors = loadedDoctors

        Log.d("PatientProfile", "Patient: ${loadedPatient?.name}")
        Log.d("PatientProfile", "Doctors count: ${loadedDoctors.size}")
    }

    val patientName = patient?.name ?: ""
    val specializations = listOf("Cardiologist", "Dermatologist", "Pediatrician", "Neurologist")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .padding(16.dp)
    ) {
        // Top Row: Welcome text + Logout button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Welcome, $patientName",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            IconButton(onClick = {
                // Navigate back to login screen - assuming role is "patient"
                navController.navigate("login/patient") {
                    // Clear back stack so user can't press back to return here
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout"
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search doctors or symptoms") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Specialties", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            specializations.forEach { spec ->
                AssistChip(onClick = { searchQuery = spec }, label = { Text(spec) })
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            doctors.filter {
                searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) ||
                        (it.experience?.contains(searchQuery, ignoreCase = true) ?: false)
            }.forEach { doctor ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInHorizontally(initialOffsetX = { -200 }) + fadeIn()
                ) {
                    DoctorCard(doctor = doctor) {
                        navController.navigate("calendar/${doctor.id}/$userId/$patientName")
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorCard(doctor: UserEntity, onBookClick: () -> Unit) {
    var showRatingDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.default_profile),
                contentDescription = doctor.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(doctor.name, fontWeight = FontWeight.Bold)
                Text(doctor.experience ?: "No specialization listed")
                Row {
                    repeat(doctor.rating.toInt()) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700))
                    }
                }
                TextButton(onClick = { showRatingDialog = true }) {
                    Text("Rate")
                }
            }
            Button(onClick = onBookClick) {
                Text("Book")
            }
        }
    }

    if (showRatingDialog) {
        RatingDialog(doctor = doctor, onDismiss = { showRatingDialog = false })
    }
}

@Composable
fun RatingDialog(doctor: UserEntity, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val userDao = remember { AppDatabase.getDatabase(context).userDao() }
    var rating by remember { mutableStateOf(3f) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate ${doctor.name}") },
        text = {
            Column {
                Text("Select your rating:")
                Slider(
                    value = rating,
                    onValueChange = { rating = it },
                    valueRange = 1f..5f,
                    steps = 3
                )
                Text("Rating: ${rating.toInt()}")
            }
        },
        confirmButton = {
            Button(onClick = {
                coroutineScope.launch {
                    val newCount = doctor.ratingCount + 1
                    val newRating = ((doctor.rating * doctor.ratingCount) + rating) / newCount

                    val updatedDoctor = doctor.copy(
                        rating = newRating,
                        ratingCount = newCount
                    )

                    withContext(Dispatchers.IO) {
                        userDao.updateUser(updatedDoctor)
                    }
                    onDismiss()
                }
            }) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
