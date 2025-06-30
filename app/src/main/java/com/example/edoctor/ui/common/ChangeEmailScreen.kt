package com.example.edoctor.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
fun ChangeEmailScreen(navController: NavController) {
    var currentEmail by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
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
                title = { Text("Change Email") },
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
                value = currentEmail,
                onValueChange = { currentEmail = it },
                label = { Text("Current Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = newEmail,
                onValueChange = { newEmail = it },
                label = { Text("New Email") },
                modifier = Modifier.fillMaxWidth()
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
                                // Verify current email
                                val isValidEmail = when (user) {
                                    is AdminEntity -> user.email == currentEmail
                                    is DoctorEntity -> user.email == currentEmail
                                    is PatientEntity -> user.email == currentEmail
                                    else -> false
                                }
                                
                                if (isValidEmail) {
                                    if (newEmail.isNotEmpty() && newEmail.contains("@")) {
                                        // Update email
                                        val updatedUser = when (user) {
                                            is AdminEntity -> user.copy(email = newEmail)
                                            is DoctorEntity -> user.copy(email = newEmail)
                                            is PatientEntity -> user.copy(email = newEmail)
                                            else -> user
                                        }
                                        
                                        withContext(Dispatchers.IO) {
                                            when (updatedUser) {
                                                is AdminEntity -> adminDao.updateAdmin(updatedUser)
                                                is DoctorEntity -> doctorDao.updateDoctor(updatedUser)
                                                is PatientEntity -> patientDao.updatePatient(updatedUser)
                                            }
                                        }
                                        
                                        // Update session
                                        sessionManager.saveLoginSession(currentUserId, userRole, newEmail)
                                        
                                        successMessage = "Email updated successfully"
                                        errorMessage = ""
                                        currentEmail = ""
                                        newEmail = ""
                                    } else {
                                        errorMessage = "Please enter a valid email address"
                                        successMessage = ""
                                    }
                                } else {
                                    errorMessage = "Current email is incorrect"
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
                Text("Update Email")
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
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
} 