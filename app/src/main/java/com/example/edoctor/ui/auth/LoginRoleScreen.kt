package com.example.edoctor.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun LoginRoleScreen(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select Role to Login",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { navController.navigate("login/admin") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login as Admin")
            }

            Button(
                onClick = { navController.navigate("login/doctor") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login as Doctor")
            }

            Button(
                onClick = { navController.navigate("login/patient") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login as Patient")
            }
        }
    }
}
