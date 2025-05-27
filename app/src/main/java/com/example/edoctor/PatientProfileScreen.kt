package com.example.edoctor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileScreen(navController: NavHostController, userId: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { DatabaseProvider.getDatabase(context) }
    val userDao = db.userDao()

    var user by remember { mutableStateOf<UserEntity?>(null) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        coroutineScope.launch(Dispatchers.IO) {
            user = userDao.getUserById(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patient Profile") },
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Profile Settings") },
                            onClick = {
                                expanded = false
                                navController.navigate("patient_details/$userId")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                expanded = false
                                navController.navigate("welcome") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (user != null) {
                FloatingActionButton(onClick = {
                    // Navigate to EditProfileScreen if implemented
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                }
            }
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                if (user == null) {
                    CircularProgressIndicator()
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.doctor), // placeholder image
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Welcome, ${user!!.name}!", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Role: ${user!!.role}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    )
}
