@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.edoctor.ui.auth

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// Import data layer
import com.example.edoctor.data.database.AppDatabase
import com.example.edoctor.data.dao.PatientDao
import com.example.edoctor.data.entities.PatientEntity
import com.example.edoctor.utils.validatePasswordStrength
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import com.example.edoctor.ui.common.DatePicker

// ✅ Moved out of composable
fun savePatientToDatabase(
    context: Context,
    name: String,
    email: String,
    phone: String,
    password: String,
    gender: String,
    dob: LocalDate?,
    address: String?,
    bloodGroup: String?,
    onResult: (Boolean) -> Unit
) {
    val db = AppDatabase.getDatabase(context)
    val patientDao = db.patientDao()
    val patient = PatientEntity(
        0, name, email, phone, password, gender, 
        dob?.toString(), address, bloodGroup, ""
    )

    CoroutineScope(Dispatchers.IO).launch {
        try {
            patientDao.insertPatient(patient)
            onResult(true)
        } catch (e: Exception) {
            onResult(false)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PatientRegistrationScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var showGenderDropdown by remember { mutableStateOf(false) }
    var dob by remember { mutableStateOf<LocalDate?>(null) }
    var address by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var showBloodGroupDropdown by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val (strengthMessage, strengthColor) = validatePasswordStrength(password)
    val context = LocalContext.current

    fun validateAndSubmit() {
        when {
            name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank() || gender.isBlank() -> {
                message = "Please fill all required fields."
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                message = "Invalid email address."
            }
            !Pattern.matches("^[0-9]{10}\$", phone) -> {
                message = "Phone number must be 10 digits."
            }
            password.length < 6 -> {
                message = "Password must be at least 6 characters."
            }
            else -> {
                savePatientToDatabase(
                    context,
                    name,
                    email,
                    phone,
                    password,
                    gender,
                    dob,
                    address.takeIf { it.isNotBlank() },
                    bloodGroup.takeIf { it.isNotBlank() }
                ) { success ->
                    message = if (success) "Registration successful!" else "Failed to save patient to database."
                }
            }
        }
    }

    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF0F0F0)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(16.dp).fillMaxWidth(0.9f),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { navController.navigate("login/patient") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text("Login as Patient")
                }

                Text(
                    text = "Fill Your Details to Register as Patient",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    "Patient Registration", 
                    fontSize = 22.sp, 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") })

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = strengthMessage,
                    color = strengthColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                DatePicker(
                    selectedDate = dob,
                    onDateSelected = { dob = it },
                    label = "Date of Birth"
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = showGenderDropdown,
                    onExpandedChange = { showGenderDropdown = !showGenderDropdown }
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Gender") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Gender") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = showGenderDropdown,
                        onDismissRequest = { showGenderDropdown = false }
                    ) {
                        listOf("Male", "Female", "Other").forEach { option ->
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

                ExposedDropdownMenuBox(
                    expanded = showBloodGroupDropdown,
                    onExpandedChange = { showBloodGroupDropdown = !showBloodGroupDropdown }
                ) {
                    OutlinedTextField(
                        value = bloodGroup,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Blood Group") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Blood Group") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = showBloodGroupDropdown,
                        onDismissRequest = { showBloodGroupDropdown = false }
                    ) {
                        listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-").forEach { option ->
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

                Button(
                    onClick = { validateAndSubmit() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Register")
                }

                if (message.isNotEmpty()) {
                    Text(
                        text = message,
                        color = if (message == "Registration successful!") Color(0xFF2E7D32) else Color.Red,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}
