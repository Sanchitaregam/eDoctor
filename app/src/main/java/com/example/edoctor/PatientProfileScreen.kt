package com.example.edoctor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController

@Composable
fun PatientProfileScreen(navController: NavController, userId: Int) {


    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    val specialities = listOf("Cardiologist", "Dermatologist", "Pediatrician", "Neurologist")
    val doctors = listOf(
        Doctor("Dr. Smith", "Cardiologist", 12, "Mon-Fri", 4.8f),
        Doctor("Dr. Riya", "Dermatologist", 8, "Tue-Sat", 4.6f),
        Doctor("Dr. Kumar", "Neurologist", 15, "Mon-Wed", 4.9f),
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text(
            text = "Welcome!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search symptoms or doctors") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Specialities", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            specialities.forEach {
                AssistChip(onClick = {}, label = { Text(it) })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Find Your Doctor", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

        LazyColumn {
            items(doctors) { doctor ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = doctor.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Specialization: ${doctor.specialization}")
                        Text("Experience: ${doctor.experience} years")
                        Text("Available: ${doctor.availableDays}")
                        Text("Rating: ${doctor.rating}")
                    }
                }
            }
        }
    }
}

data class Doctor(
    val name: String,
    val specialization: String,
    val experience: Int,
    val availableDays: String,
    val rating: Float
)