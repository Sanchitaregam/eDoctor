package com.example.edoctor.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
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
fun ChangePasswordScreen(navController: NavController, userId: Int) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val adminDao = db.adminDao()
    val doctorDao = db.doctorDao()
    val patientDao = db.patientDao()
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm New Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        val currentUserId = sessionManager.getCurrentUserId()
                        val userRole = sessionManager.getCurrentUserRole()
                        
                        if (currentUserId != -1 && userRole != null) {
                            val user = withContext(Dispatchers.IO) {
                                when (userRole.lowercase()) {
                                    "admin" -> adminDao.getAdminById(currentUserId)
                                    "doctor" -> doctorDao.getDoctorById(currentUserId)
                                    "patient" -> patientDao.getPatientById(currentUserId)
                                    else -> null
                                }
                            }
                            
                        if (user != null) {
                            if (newPassword == confirmPassword && newPassword.length >= 6) {
                                    // Update password
                                    val updatedUser = when (user) {
                                        is AdminEntity -> user.copy(password = newPassword)
                                        is DoctorEntity -> user.copy(password = newPassword)
                                        is PatientEntity -> user.copy(password = newPassword)
                                        else -> user
                                    }
                                    
                                    withContext(Dispatchers.IO) {
                                        when (updatedUser) {
                                            is AdminEntity -> adminDao.updateAdmin(updatedUser)
                                            is DoctorEntity -> doctorDao.updateDoctor(updatedUser)
                                            is PatientEntity -> patientDao.updatePatient(updatedUser)
                                        }
                                    }
                                    successMessage = "Password updated successfully"
                                    errorMessage = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                } else {
                                    errorMessage = "New passwords do not match or are too short (minimum 6 characters)"
                                    successMessage = ""
                                }
                            } else {
                                errorMessage = "User not found"
                                successMessage = ""
                            }
                        } else {
                            errorMessage = "Session error. Please login again."
                            successMessage = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Update Password")
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage, 
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (successMessage.isNotEmpty()) {
                Text(
                    text = successMessage, 
                    color = Color(0xFF2E7D32), // Dark Green - same as strong password
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
