package com.example.edoctor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailsScreen(navController: NavController, userId: String) {
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val userDao = db.userDao()

    var user by remember { mutableStateOf<UserEntity?>(null) }

    LaunchedEffect(userId) {
        user = withContext(Dispatchers.IO) {
            userDao.getUserById(userId.toInt())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patient Profile Details") },
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
                user?.let { u ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.doctor),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                        )

                        Text("${u.name}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Role: ${u.role}", fontSize = 16.sp, color = Color.Gray)

                        Divider(color = Color.LightGray, thickness = 1.dp)

                        ProfileItem(label = "Email", value = u.email)
                        ProfileItem(label = "Phone", value = u.phone)
                        ProfileItem(label = "Gender", value = u.gender)
                        ProfileItem(label = "Date of Birth", value = u.dob ?: "N/A")
                        ProfileItem(label = "Address", value = u.address ?: "N/A")
                        ProfileItem(label = "Blood Group", value = u.bloodGroup ?: "N/A")
                        ProfileItem(label = "Emergency Contact", value = u.emergencyContact ?: "N/A")
                    }
                }
            }
        }
    )
}

@Composable
fun ProfileItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 14.sp, color = Color.DarkGray)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray, thickness = 0.5.dp)
    }
}
