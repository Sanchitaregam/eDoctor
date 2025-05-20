

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.edoctor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun saveAdminToDatabase(name: String, email: String, phone: String, password: String, gender: String, role: String ) {
    val context = LocalContext.current
    val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "user_database"
    ).build()

    val userDao = db.userDao()
    val user = UserEntity(0, name, email, phone, password, gender, role)

    CoroutineScope(Dispatchers.IO).launch {
        userDao.insertUser(user)
    }
}

@Composable
fun AdminRegistrationScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    val passwordStrength = validatePasswordStrength(password)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BackButton(navController)

        Text(
            text = "Fill Your Details to Register as Admin",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text("Admin Registration", fontSize = 22.sp, fontWeight = FontWeight.Bold)

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
// Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

// Password Strength Feedback
        val (strengthMessage, strengthColor) = validatePasswordStrength(password)

        Text(
            text = strengthMessage,
            color = strengthColor,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )


        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        if (successMessage.isNotEmpty()) {
            Text(text = successMessage, color = MaterialTheme.colorScheme.primary)
        }

        Button(
            onClick = {
                when {
                    name.isBlank() || email.isBlank() || password.isBlank() -> {
                        errorMessage = "Please fill all fields"
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
                        successMessage = "Admin Registered Successfully!"
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