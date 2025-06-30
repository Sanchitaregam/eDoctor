package com.example.edoctor.ui.common

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
    var showProfileDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedPhone by remember { mutableStateOf("") }
    var editedAddress by remember { mutableStateOf("") }
    var editedDob by remember { mutableStateOf("") }
    var editedGender by remember { mutableStateOf("") }
    var editedSpecialization by remember { mutableStateOf("") }
    var editedExperience by remember { mutableStateOf("") }
    var editedBloodGroup by remember { mutableStateOf("") }

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
                    editedPhone = loadedUser.phone
                    editedAddress = loadedUser.address ?: ""
                    editedDob = loadedUser.dob ?: ""
                    editedGender = loadedUser.gender ?: ""
                }
                is DoctorEntity -> {
                    editedName = loadedUser.name
                    editedPhone = loadedUser.phone
                    editedAddress = loadedUser.address ?: ""
                    editedDob = loadedUser.dob ?: ""
                    editedGender = loadedUser.gender ?: ""
                    editedSpecialization = loadedUser.specialization ?: ""
                    editedExperience = loadedUser.experience?.toString() ?: ""
                }
                is PatientEntity -> {
                    editedName = loadedUser.name
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
    var showDatePicker by remember { mutableStateOf(false) }
    
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
                onClick = { showEditDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Edit Profile")
            }

            Button(
                onClick = { navController.navigate("change_email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Change Email")
            }

            Button(
                onClick = { navController.navigate("change_password/$currentUserId") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Change Password")
            }

            Button(
                onClick = {
                    // Clear login session
                    sessionManager.clearLoginSession()
                    
                    // Navigate to welcome screen and clear back stack
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
                        Column {
                            OutlinedTextField(
                                value = editedDob,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Date of Birth") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            TextButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.align(Alignment.Start)
                            ) {
                                Text("Select Date")
                            }
                        }
                        
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
                        
                        // Date Picker Dialog
                        if (showDatePicker) {
                            val datePickerState = rememberDatePickerState()
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            val date = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                                            editedDob = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                        }
                                        showDatePicker = false
                                    }) {
                                        Text("OK")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDatePicker = false }) {
                                        Text("Cancel")
                                    }
                                }
                            ) {
                                DatePicker(state = datePickerState)
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
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                user?.let {
                                    withContext(Dispatchers.IO) {
                                        when (it) {
                                            is AdminEntity -> {
                                                val updated = it.copy(
                                                    name = editedName,
                                                    phone = editedPhone,
                                                    gender = editedGender,
                                                    dob = editedDob,
                                                    address = editedAddress
                                                )
                                                adminDao.updateAdmin(updated)
                                            }
                                            is DoctorEntity -> {
                                                val updated = it.copy(
                                                    name = editedName,
                                                    phone = editedPhone,
                                                    gender = editedGender,
                                                    dob = editedDob,
                                                    address = editedAddress,
                                                    specialization = editedSpecialization,
                                                    experience = editedExperience.toIntOrNull()
                                                )
                                                doctorDao.updateDoctor(updated)
                                            }
                                            is PatientEntity -> {
                                                val updated = it.copy(
                                                    name = editedName,
                                                    phone = editedPhone,
                                                    gender = editedGender,
                                                    dob = editedDob,
                                                    address = editedAddress,
                                                    bloodGroup = editedBloodGroup
                                                )
                                                patientDao.updatePatient(updated)
                                            }
                                        }
                                    }
                                    showEditDialog = false
                                }
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
