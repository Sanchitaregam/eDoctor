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

// Import data layer
import com.example.edoctor.data.database.AppDatabase
import com.example.edoctor.data.dao.UserDao
import com.example.edoctor.data.entities.UserEntity
import com.example.edoctor.utils.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userId = sessionManager.getCurrentUserId()
    val db = remember { AppDatabase.getDatabase(context) }
    val userDao = db.userDao()
    val coroutineScope = rememberCoroutineScope()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var user by remember { mutableStateOf<UserEntity?>(null) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    
    // Dropdown states
    var showGenderDropdown by remember { mutableStateOf(false) }
    var showBloodGroupDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Options
    val genderOptions = listOf("Male", "Female", "Other")
    val bloodGroupOptions = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    // Load user data
    LaunchedEffect(userId) {
        val fetchedUser = withContext(kotlinx.coroutines.Dispatchers.IO) {
            userDao.getUserById(userId)
        }
        fetchedUser?.let {
            user = it
            name = it.name
            phone = it.phone
            experience = it.experience ?: ""
            specialization = it.specialization ?: ""
            gender = it.gender ?: ""
            dob = it.dob ?: ""
            address = it.address ?: ""
            bloodGroup = it.bloodGroup ?: ""
        }
    }

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
                onClick = { showEditProfileDialog = true },
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
                onClick = { navController.navigate("change_password/$userId") },
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
        if (showEditProfileDialog) {
            AlertDialog(
                onDismissRequest = { showEditProfileDialog = false },
                title = { Text("Edit Profile") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
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
                        
                        // Gender Dropdown
                        ExposedDropdownMenuBox(
                            expanded = showGenderDropdown,
                            onExpandedChange = { showGenderDropdown = !showGenderDropdown },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = gender,
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
                                            gender = option
                                            showGenderDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Date of Birth with Date Picker
                        Column {
                            OutlinedTextField(
                                value = dob,
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
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Blood Group Dropdown
                        ExposedDropdownMenuBox(
                            expanded = showBloodGroupDropdown,
                            onExpandedChange = { showBloodGroupDropdown = !showBloodGroupDropdown },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = bloodGroup,
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
                                            bloodGroup = option
                                            showBloodGroupDropdown = false
                                        }
                                    )
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
                                            dob = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
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
                        
                        if (user?.role == "doctor") {
                            OutlinedTextField(
                                value = specialization,
                                onValueChange = { specialization = it },
                                label = { Text("Specialization") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = experience,
                                onValueChange = { experience = it },
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
                                    val updated = it.copy(
                                        name = name,
                                        phone = phone,
                                        experience = experience,
                                        specialization = specialization,
                                        gender = gender,
                                        dob = dob,
                                        address = address,
                                        bloodGroup = bloodGroup
                                    )
                                    withContext(kotlinx.coroutines.Dispatchers.IO) {
                                        userDao.updateUser(updated)
                                    }
                                    showEditProfileDialog = false
                                }
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditProfileDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
