package com.example.edoctor.ui.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Import data layer
import com.example.edoctor.data.database.AppDatabase
import com.example.edoctor.data.dao.AdminDao
import com.example.edoctor.data.dao.DoctorDao
import com.example.edoctor.data.dao.PatientDao
import com.example.edoctor.data.entities.AdminEntity
import com.example.edoctor.data.entities.DoctorEntity
import com.example.edoctor.data.entities.PatientEntity
import com.example.edoctor.utils.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, role: String) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }

    val title = when (role.lowercase()) {
        "doctor" -> "Doctor Login"
        "patient" -> "Patient Login"
        "admin" -> "Admin Login"
        else -> "Login"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "All fields are required"
                        } else {
                            coroutineScope.launch {
                                val db = AppDatabase.getDatabase(context)
                                var user: Any? = null
                                var userId = 0
                                var userEmail = ""

                                when (role.lowercase()) {
                                    "admin" -> {
                                        val admin = withContext(Dispatchers.IO) {
                                            db.adminDao().loginAdmin(email, password)
                                        }
                                        if (admin != null) {
                                            user = admin
                                            userId = admin.id
                                            userEmail = admin.email
                                        }
                                    }
                                    "doctor" -> {
                                        val doctor = withContext(Dispatchers.IO) {
                                            db.doctorDao().loginDoctor(email, password)
                                        }
                                        if (doctor != null) {
                                            user = doctor
                                            userId = doctor.id
                                            userEmail = doctor.email
                                        }
                                    }
                                    "patient" -> {
                                        val patient = withContext(Dispatchers.IO) {
                                            db.patientDao().loginPatient(email, password)
                                        }
                                        if (patient != null) {
                                            user = patient
                                            userId = patient.id
                                            userEmail = patient.email
                                        }
                                    }
                                }

                                if (user != null) {
                                    // Save login session
                                    sessionManager.saveLoginSession(userId, role, userEmail)
                                    
                                    Toast.makeText(
                                        context,
                                        "Login Successful",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    
                                    when (role.lowercase()) {
                                        "doctor" -> navController.navigate("doctor_profile/$userId")
                                        "patient" -> navController.navigate("patient_profile/$userId")
                                        "admin" -> navController.navigate("admin_profile/$userId")
                                        else -> errorMessage = "Unknown role"
                                    }
                                } else {
                                    errorMessage = "Invalid credentials"
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Login")
                }
            }
        }
    }
}
