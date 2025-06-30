package com.example.edoctor.ui.common

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers

// Import data layer
import com.example.edoctor.data.database.AppDatabase
import com.example.edoctor.data.dao.AdminDao
import com.example.edoctor.data.dao.DoctorDao
import com.example.edoctor.data.dao.PatientDao
import com.example.edoctor.data.entities.AdminEntity
import com.example.edoctor.data.entities.DoctorEntity
import com.example.edoctor.data.entities.PatientEntity
import com.example.edoctor.utils.SessionManager
import com.example.edoctor.R
import com.example.edoctor.ui.common.ActionCard
import com.example.edoctor.ui.common.ProfileInfoRow
import com.example.edoctor.ui.common.DatePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val adminDao = db.adminDao()
    val doctorDao = db.doctorDao()
    val patientDao = db.patientDao()
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()
    var user by remember { mutableStateOf<Any?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedEmail by remember { mutableStateOf("") }
    var editedPhone by remember { mutableStateOf("") }
    var editedAddress by remember { mutableStateOf("") }
    var editedDob by remember { mutableStateOf("") }
    var editedGender by remember { mutableStateOf("") }
    var editedSpecialization by remember { mutableStateOf("") }
    var editedExperience by remember { mutableStateOf("") }
    var editedBloodGroup by remember { mutableStateOf("") }
    var editedPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showChangeEmailDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var newEmail by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var currentPasswordForEmail by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var passwordChangeError by remember { mutableStateOf("") }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmNewPassword by remember { mutableStateOf(false) }

    val currentUserId = sessionManager.getCurrentUserId()

    LaunchedEffect(Unit) {
        val currentUserId = sessionManager.getCurrentUserId()
        val userRole = sessionManager.getCurrentUserRole()
        
        if (currentUserId != -1 && userRole != null) {
            val loadedUser = withContext(Dispatchers.IO) {
                when (userRole.lowercase()) {
                    "admin" -> adminDao.getAdminById(currentUserId)
                    "doctor" -> doctorDao.getDoctorById(currentUserId)
                    "patient" -> patientDao.getPatientById(currentUserId)
                    else -> null
                }
            }
            user = loadedUser
            
            // Initialize edit fields
            when (loadedUser) {
                is AdminEntity -> {
                    editedName = loadedUser.name
                    editedEmail = loadedUser.email
                    editedPhone = loadedUser.phone
                    editedAddress = loadedUser.address ?: ""
                    editedDob = loadedUser.dob ?: ""
                    editedGender = loadedUser.gender ?: ""
                }
                is DoctorEntity -> {
                    editedName = loadedUser.name
                    editedEmail = loadedUser.email
                    editedPhone = loadedUser.phone
                    editedAddress = loadedUser.address ?: ""
                    editedDob = loadedUser.dob ?: ""
                    editedGender = loadedUser.gender ?: ""
                    editedSpecialization = loadedUser.specialization ?: ""
                    editedExperience = loadedUser.experience?.toString() ?: ""
                }
                is PatientEntity -> {
                    editedName = loadedUser.name
                    editedEmail = loadedUser.email
                    editedPhone = loadedUser.phone
                    editedAddress = loadedUser.address ?: ""
                    editedDob = loadedUser.dob ?: ""
                    editedGender = loadedUser.gender ?: ""
                    editedBloodGroup = loadedUser.bloodGroup ?: ""
                }
            }
        }
    }

    // Dropdown states
    var showGenderDropdown by remember { mutableStateOf(false) }
    var showBloodGroupDropdown by remember { mutableStateOf(false) }
    
    // Options
    val genderOptions = listOf("Male", "Female", "Other")
    val bloodGroupOptions = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            Button(
                onClick = { showEditDialog = true; errorMessage = "" },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Edit Profile")
            }
            Button(
                onClick = { showChangeEmailDialog = true; emailError = "" },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Change Email")
            }
            Button(
                onClick = { showChangePasswordDialog = true; passwordChangeError = "" },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Change Password")
            }
            Button(
                onClick = {
                    sessionManager.clearLoginSession()
                    navController.navigate("welcome") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout")
            }
        }

        // Edit Profile Dialog
        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Profile") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = editedPhone,
                            onValueChange = { editedPhone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Gender Dropdown
                        ExposedDropdownMenuBox(
                            expanded = showGenderDropdown,
                            onExpandedChange = { showGenderDropdown = !showGenderDropdown },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = editedGender,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Gender") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGenderDropdown) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = showGenderDropdown,
                                onDismissRequest = { showGenderDropdown = false }
                            ) {
                                genderOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            editedGender = option
                                            showGenderDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Date of Birth with Date Picker
                        var selectedDob by remember { mutableStateOf<LocalDate?>(null) }
                        
                        // Initialize selectedDob from editedDob
                        LaunchedEffect(editedDob) {
                            if (editedDob.isNotEmpty()) {
                                try {
                                    selectedDob = LocalDate.parse(editedDob, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                } catch (e: Exception) {
                                    // Try alternative format
                                    try {
                                        selectedDob = LocalDate.parse(editedDob, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                    } catch (e2: Exception) {
                                        selectedDob = null
                                    }
                                }
                            }
                        }
                        
                        DatePicker(
                            selectedDate = selectedDob,
                            onDateSelected = { date ->
                                selectedDob = date
                                editedDob = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            },
                            label = "Date of Birth"
                        )
                        
                        OutlinedTextField(
                            value = editedAddress,
                            onValueChange = { editedAddress = it },
                            label = { Text("Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Blood Group Dropdown - Only for patients
                        if (user is PatientEntity) {
                            ExposedDropdownMenuBox(
                                expanded = showBloodGroupDropdown,
                                onExpandedChange = { showBloodGroupDropdown = !showBloodGroupDropdown },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = editedBloodGroup,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Blood Group") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBloodGroupDropdown) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = showBloodGroupDropdown,
                                    onDismissRequest = { showBloodGroupDropdown = false }
                                ) {
                                    bloodGroupOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                editedBloodGroup = option
                                                showBloodGroupDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        if (user is DoctorEntity) {
                            OutlinedTextField(
                                value = editedSpecialization,
                                onValueChange = { editedSpecialization = it },
                                label = { Text("Specialization") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editedExperience,
                                onValueChange = { editedExperience = it },
                                label = { Text("Experience (years)") },
                                trailingIcon = {
                                    Column {
                                        IconButton(
                                            onClick = {
                                                val currentValue = editedExperience.toIntOrNull() ?: 0
                                                editedExperience = (currentValue + 1).toString()
                                            },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.KeyboardArrowUp,
                                                contentDescription = "Increase",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                val currentValue = editedExperience.toIntOrNull() ?: 0
                                                if (currentValue > 0) {
                                                    editedExperience = (currentValue - 1).toString()
                                                }
                                            },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Decrease",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Validate inputs
                            when {
                                editedName.isBlank() -> {
                                    errorMessage = "Name cannot be empty"
                                    return@TextButton
                                }
                                editedEmail.isBlank() -> {
                                    errorMessage = "Email cannot be empty"
                                    return@TextButton
                                }
                                !editedEmail.contains("@") || !editedEmail.contains(".") -> {
                                    errorMessage = "Invalid email format"
                                    return@TextButton
                                }
                                editedPassword.isNotEmpty() && editedPassword != confirmPassword -> {
                                    errorMessage = "Passwords do not match"
                                    return@TextButton
                                }
                                editedPassword.isNotEmpty() && editedPassword.length < 6 -> {
                                    errorMessage = "Password must be at least 6 characters"
                                    return@TextButton
                                }
                                else -> {
                                    errorMessage = ""
                                }
                            }
                            
                            coroutineScope.launch {
                                try {
                                    user?.let {
                                        withContext(Dispatchers.IO) {
                                            when (it) {
                                                is AdminEntity -> {
                                                    // Check if email is already taken by another admin
                                                    val existingAdmin = adminDao.getAdminByEmail(editedEmail)
                                                    if (existingAdmin != null && existingAdmin.id != it.id) {
                                                        errorMessage = "Email is already taken"
                                                        return@withContext
                                                    }
                                                    
                                                    val updated = it.copy(
                                                        name = editedName,
                                                        email = editedEmail,
                                                        phone = editedPhone,
                                                        gender = editedGender,
                                                        dob = editedDob,
                                                        address = editedAddress,
                                                        password = if (editedPassword.isNotEmpty()) editedPassword else it.password
                                                    )
                                                    adminDao.updateAdmin(updated)
                                                }
                                                is DoctorEntity -> {
                                                    // Check if email is already taken by another doctor
                                                    val existingDoctor = doctorDao.getDoctorByEmail(editedEmail)
                                                    if (existingDoctor != null && existingDoctor.id != it.id) {
                                                        errorMessage = "Email is already taken"
                                                        return@withContext
                                                    }
                                                    
                                                    val updated = it.copy(
                                                        name = editedName,
                                                        email = editedEmail,
                                                        phone = editedPhone,
                                                        gender = editedGender,
                                                        dob = editedDob,
                                                        address = editedAddress,
                                                        specialization = editedSpecialization,
                                                        experience = editedExperience.toIntOrNull(),
                                                        password = if (editedPassword.isNotEmpty()) editedPassword else it.password
                                                    )
                                                    doctorDao.updateDoctor(updated)
                                                }
                                                is PatientEntity -> {
                                                    // Check if email is already taken by another patient
                                                    val existingPatient = patientDao.getPatientByEmail(editedEmail)
                                                    if (existingPatient != null && existingPatient.id != it.id) {
                                                        errorMessage = "Email is already taken"
                                                        return@withContext
                                                    }
                                                    
                                                    val updated = it.copy(
                                                        name = editedName,
                                                        email = editedEmail,
                                                        phone = editedPhone,
                                                        gender = editedGender,
                                                        dob = editedDob,
                                                        address = editedAddress,
                                                        bloodGroup = editedBloodGroup,
                                                        password = if (editedPassword.isNotEmpty()) editedPassword else it.password
                                                    )
                                                    patientDao.updatePatient(updated)
                                                }
                                            }
                                        }
                                        showEditDialog = false
                                        editedPassword = ""
                                        confirmPassword = ""
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Failed to update profile: ${e.message}"
                                }
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showEditDialog = false 
                        errorMessage = ""
                        editedPassword = ""
                        confirmPassword = ""
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Change Email Dialog
        if (showChangeEmailDialog) {
            AlertDialog(
                onDismissRequest = { showChangeEmailDialog = false },
                title = { Text("Change Email") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = { newEmail = it },
                            label = { Text("New Email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = currentPasswordForEmail,
                            onValueChange = { currentPasswordForEmail = it },
                            label = { Text("Current Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (emailError.isNotEmpty()) {
                            Text(emailError, color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            if (newEmail.isBlank() || !newEmail.contains("@") || !newEmail.contains(".")) {
                                emailError = "Enter a valid email"
                                return@launch
                            }
                            if (currentPasswordForEmail.isBlank()) {
                                emailError = "Enter your current password"
                                return@launch
                            }
                            val userRole = sessionManager.getCurrentUserRole()?.lowercase()
                            val currentUserId = sessionManager.getCurrentUserId()
                            val userEntity = when (userRole) {
                                "admin" -> adminDao.getAdminById(currentUserId)
                                "doctor" -> doctorDao.getDoctorById(currentUserId)
                                "patient" -> patientDao.getPatientById(currentUserId)
                                else -> null
                            }
                            if (userEntity == null) {
                                emailError = "User not found"
                                return@launch
                            }
                            val correctPassword = when (userEntity) {
                                is AdminEntity -> userEntity.password
                                is DoctorEntity -> userEntity.password
                                is PatientEntity -> userEntity.password
                                else -> ""
                            }
                            if (currentPasswordForEmail != correctPassword) {
                                emailError = "Incorrect password"
                                return@launch
                            }
                            // Check for email uniqueness
                            val emailTaken = when (userRole) {
                                "admin" -> adminDao.getAdminByEmail(newEmail)
                                "doctor" -> doctorDao.getDoctorByEmail(newEmail)
                                "patient" -> patientDao.getPatientByEmail(newEmail)
                                else -> null
                            }
                            if (emailTaken != null && (emailTaken as? AdminEntity)?.id != currentUserId && (emailTaken as? DoctorEntity)?.id != currentUserId && (emailTaken as? PatientEntity)?.id != currentUserId) {
                                emailError = "Email already in use"
                                return@launch
                            }
                            // Update email
                            when (userEntity) {
                                is AdminEntity -> adminDao.updateAdmin(userEntity.copy(email = newEmail))
                                is DoctorEntity -> doctorDao.updateDoctor(userEntity.copy(email = newEmail))
                                is PatientEntity -> patientDao.updatePatient(userEntity.copy(email = newEmail))
                            }
                            showChangeEmailDialog = false
                            newEmail = ""
                            currentPasswordForEmail = ""
                        }
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showChangeEmailDialog = false
                        newEmail = ""
                        currentPasswordForEmail = ""
                        emailError = ""
                    }) { Text("Cancel") }
                }
            )
        }

        // Change Password Dialog
        if (showChangePasswordDialog) {
            AlertDialog(
                onDismissRequest = { showChangePasswordDialog = false },
                title = { Text("Change Password") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Password") },
                            visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                    Icon(
                                        if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (showNewPassword) "Hide password" else "Show password"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = confirmNewPassword,
                            onValueChange = { confirmNewPassword = it },
                            label = { Text("Confirm New Password") },
                            visualTransformation = if (showConfirmNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showConfirmNewPassword = !showConfirmNewPassword }) {
                                    Icon(
                                        if (showConfirmNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (showConfirmNewPassword) "Hide password" else "Show password"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (passwordChangeError.isNotEmpty()) {
                            Text(passwordChangeError, color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            if (newPassword.isBlank() || newPassword.length < 6) {
                                passwordChangeError = "Password must be at least 6 characters"
                                return@launch
                            }
                            if (newPassword != confirmNewPassword) {
                                passwordChangeError = "Passwords do not match"
                                return@launch
                            }
                            val userRole = sessionManager.getCurrentUserRole()?.lowercase()
                            val currentUserId = sessionManager.getCurrentUserId()
                            val userEntity = when (userRole) {
                                "admin" -> adminDao.getAdminById(currentUserId)
                                "doctor" -> doctorDao.getDoctorById(currentUserId)
                                "patient" -> patientDao.getPatientById(currentUserId)
                                else -> null
                            }
                            if (userEntity == null) {
                                passwordChangeError = "User not found"
                                return@launch
                            }
                            // Update password
                            when (userEntity) {
                                is AdminEntity -> adminDao.updateAdmin(userEntity.copy(password = newPassword))
                                is DoctorEntity -> doctorDao.updateDoctor(userEntity.copy(password = newPassword))
                                is PatientEntity -> patientDao.updatePatient(userEntity.copy(password = newPassword))
                            }
                            showChangePasswordDialog = false
                            newPassword = ""
                            confirmNewPassword = ""
                        }
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showChangePasswordDialog = false
                        newPassword = ""
                        confirmNewPassword = ""
                        passwordChangeError = ""
                    }) { Text("Cancel") }
                }
            )
        }
    }
}
