@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.edoctor.ui.auth

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import java.time.LocalDate
import java.util.regex.Pattern
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// Import data layer
import com.example.edoctor.data.database.AppDatabase
import com.example.edoctor.data.dao.DoctorDao
import com.example.edoctor.data.entities.DoctorEntity
import com.example.edoctor.utils.BackButton
import com.example.edoctor.utils.validatePasswordStrength
import com.example.edoctor.ui.common.DatePicker

fun saveDoctorToDatabase(
    context: android.content.Context,
    name: String,
    email: String,
    phone: String,
    password: String,
    gender: String,
    dob: LocalDate?,
    address: String?,
    specialization: String?,
    experience: String?,
    onResult: (Boolean) -> Unit
) {
    val db = AppDatabase.getDatabase(context) // ✅ Singleton usage
    val doctorDao = db.doctorDao()
    val doctor = DoctorEntity(
        0, name, email, phone, password, gender, 
        dob?.toString(), address, true, specialization, experience?.toIntOrNull()
    )

    CoroutineScope(Dispatchers.IO).launch {
        try {
            doctorDao.insertDoctor(doctor)
            onResult(true)
        } catch (e: Exception) {
            onResult(false)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EnhancedDoctorRegistrationScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var showGenderDropdown by remember { mutableStateOf(false) }
    var dob by remember { mutableStateOf<LocalDate?>(null) }
    var address by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var messageColor by remember { mutableStateOf(Color.Red) }

    val context = LocalContext.current

    fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})")
        return emailPattern.matcher(email).matches()
    }

    fun isValidPhone(phone: String): Boolean {
        return phone.length == 10 && phone.all { it.isDigit() }
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
                    onClick = { navController.navigate("login/doctor") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text("Login as Doctor")
                }

                Text(
                    "Fill Your Details to Register as Doctor", 
                    fontSize = 14.sp, 
                    fontWeight = FontWeight.Bold, 
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    "Doctor Registration", 
                    fontSize = 22.sp, 
                    fontWeight = FontWeight.Bold, 
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth()
                )

                val (strengthMessage, strengthColor) = validatePasswordStrength(password)
                Text(text = strengthMessage, color = strengthColor, fontSize = 12.sp)

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
                    trailingIcon = {
                        Column {
                            IconButton(
                                onClick = {
                                    val currentValue = experience.toIntOrNull() ?: 0
                                    experience = (currentValue + 1).toString()
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
                                    val currentValue = experience.toIntOrNull() ?: 0
                                    if (currentValue > 0) {
                                        experience = (currentValue - 1).toString()
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

                // Gender Dropdown
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
                    ExposedDropdownMenu(expanded = showGenderDropdown, onDismissRequest = { showGenderDropdown = false }) {
                        listOf("Male", "Female", "Other").forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = {
                                gender = option
                                showGenderDropdown = false
                            })
                        }
                    }
                }

                Button(
                    onClick = {
                        messageColor = Color.Red
                        when {
                            name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank() || gender.isBlank() -> {
                                message = "Please fill all required fields."
                            }
                            !isValidEmail(email) -> {
                                message = "Invalid email format."
                            }
                            !isValidPhone(phone) -> {
                                message = "Phone number must be exactly 10 digits."
                            }
                            password.length < 6 -> {
                                message = "Password must be at least 6 characters."
                            }
                            else -> {
                                saveDoctorToDatabase(
                                    context, 
                                    name, 
                                    email, 
                                    phone, 
                                    password, 
                                    gender,
                                    dob,
                                    address.takeIf { it.isNotBlank() },
                                    specialization.takeIf { it.isNotBlank() },
                                    experience.takeIf { it.isNotBlank() }
                                ) { success ->
                                    if (success) {
                                        message = "Doctor Registered Successfully!"
                                        messageColor = Color(0xFF2E7D32)
                                    } else {
                                        message = "Failed to register. Try again."
                                        messageColor = Color.Red
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Register", fontSize = 16.sp)
                }

                if (message.isNotEmpty()) {
                    Text(
                        text = message,
                        color = messageColor,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}
