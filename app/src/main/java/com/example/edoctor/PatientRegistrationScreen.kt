@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.edoctor

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
import androidx.compose.ui.text.input.PasswordVisualTransformation

// ✅ Moved out of composable
fun savePatientToDatabase(
    context: Context,
    name: String,
    email: String,
    phone: String,
    password: String,
    gender: String,
    role: String = "patient",
    onResult: (Boolean) -> Unit
) {
    val db = DatabaseProvider.getDatabase(context)
    val userDao = db.userDao()
    val user = UserEntity(0, name, email, phone, password, gender, role)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            userDao.insertUser(user)
            onResult(true)
        } catch (e: Exception) {
            onResult(false)
        }
    }
}

@Composable
fun PatientRegistrationScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var showGenderDropdown by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    val (strengthMessage, strengthColor) = validatePasswordStrength(password)
    val context = LocalContext.current

    fun validateAndSubmit() {
        when {
            name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank() || gender.isBlank() -> {
                message = "Please fill all fields."
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
                    "patient"
                ) { success ->
                    message = if (success) "Registration successful!" else "Failed to save patient to database."
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.9f),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BackButton(navController)

                Text(
                    text = "Fill Your Details to Register as Patient",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text("Patient Registration", fontSize = 22.sp, fontWeight = FontWeight.Bold)

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
                        color = if (message == "Registration successful!") Color.Green else Color.Red,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}
