package com.example.edoctor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailsScreen(navController: NavController, userId: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { DatabaseProvider.getDatabase(context) }
    val userDao = db.userDao()

    var user by remember { mutableStateOf<UserEntity?>(null) }

    LaunchedEffect(userId) {
        coroutineScope.launch(Dispatchers.IO) {
            user = userDao.getUserById(userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            if (user == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .padding(padding),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.doctor), // Placeholder image
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )

                    Text("Full Name: ${user!!.name}", fontSize = 20.sp)
                    Text("Email: ${user!!.email}", fontSize = 18.sp)
                    Text("Phone: ${user!!.phone}", fontSize = 18.sp)
                    Text("Gender: ${user!!.gender}", fontSize = 18.sp)
                    Text("Role: ${user!!.role}", fontSize = 18.sp)
                    Text("Date of Birth: ${user!!.dob ?: "N/A"}", fontSize = 18.sp)
                    Text("Address: ${user!!.address ?: "N/A"}", fontSize = 18.sp)
                    Text("Blood Group: ${user!!.bloodGroup ?: "N/A"}", fontSize = 18.sp)
                    Text("Emergency Contact: ${user!!.emergencyContact ?: "N/A"}", fontSize = 18.sp)
                }
            }
        }
    )
}
