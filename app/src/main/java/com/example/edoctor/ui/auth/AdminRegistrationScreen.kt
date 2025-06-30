@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.edoctor.ui.auth

import android.os.Build
import androidx.annotation.RequiresApi
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
import java.time.LocalDate
import java.util.regex.Pattern
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// Import data layer
import com.example.edoctor.data.database.AppDatabase
import com.example.edoctor.data.dao.AdminDao
import com.example.edoctor.data.entities.AdminEntity
import com.example.edoctor.utils.BackButton
import com.example.edoctor.utils.validatePasswordStrength
import com.example.edoctor.ui.common.DatePicker

fun saveAdminToDatabase(
    context: android.content.Context,
    name: String,
    email: String,
    phone: String,
    password: String,
    gender: String,
    dob: LocalDate?,
    address: String?,
    onResult: (Boolean) -> Unit
) {
    val db = AppDatabase.getDatabase(context) // ✅ Singleton instance
    val adminDao = db.adminDao()
    val admin = AdminEntity(0, name, email, phone, password, gender, dob?.toString(), address)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            adminDao.insertAdmin(admin)
            onResult(true)
        } catch (e: Exception) {
            onResult(false)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AdminRegistrationScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Other") } // Example default
    var dob by remember { mutableStateOf<LocalDate?>(null) }
    var address by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    val context = LocalContext.current

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
                    onClick = { navController.navigate("login/admin") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text("Login as Admin")
                }

        Text(
                    "Fill Your Details to Register as Admin", 
            fontSize = 14.sp,
                    fontWeight = FontWeight.Bold, 
                    modifier = Modifier.align(Alignment.CenterHorizontally)
        )
                Text(
                    "Admin Registration", 
                    fontSize = 22.sp, 
                    fontWeight = FontWeight.Bold, 
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Admin Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        val (strengthMessage, strengthColor) = validatePasswordStrength(password)

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

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        if (successMessage.isNotEmpty()) {
                    Text(text = successMessage, color = Color(0xFF2E7D32))
        }

        Button(
            onClick = {
                when {
                    name.isBlank() || email.isBlank() || password.isBlank() || phone.isBlank() -> {
                                errorMessage = "Please fill all required fields"
                        successMessage = ""
                    }
                    !email.contains("@") || !email.contains(".") -> {
                        errorMessage = "Invalid email format"
                        successMessage = ""
                    }
                    password.length < 6 -> {
                        errorMessage = "Password must be at least 6 characters"
                        successMessage = ""
                    }
                    else -> {
                        errorMessage = ""
                        saveAdminToDatabase(
                            context,
                            name,
                            email,
                            phone,
                            password,
                            gender,
                                    dob,
                                    address.takeIf { it.isNotBlank() }
                        ) { success ->
                            if (success) {
                                successMessage = "Admin Registered Successfully!"
                            } else {
                                errorMessage = "Error saving admin to database"
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Register")
                }
            }
        }
    }
}
