package com.example.edoctor.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Import data layer
import com.example.edoctor.data.database.AppDatabase
import com.example.edoctor.data.dao.DoctorDao
import com.example.edoctor.data.entities.DoctorEntity
import com.example.edoctor.ui.common.DoctorCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDoctorsScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val doctorDao = db.doctorDao()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var doctors by remember { mutableStateOf<List<DoctorEntity>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf<DoctorEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val allDoctors = withContext(Dispatchers.IO) { doctorDao.getAllDoctors() }
            doctors = allDoctors
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Doctors") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF7F7F7)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Manage Doctors (${doctors.size})",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(doctors) { doctor ->
                    DoctorCard(
                        doctor = doctor,
                        showDeleteButton = true,
                        onDelete = { showDeleteDialog = doctor }
                    )
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Confirm Delete") },
                text = { 
                    Text("Are you sure you want to delete Dr. ${showDeleteDialog!!.name}? This action cannot be undone.") 
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    doctorDao.deleteDoctor(showDeleteDialog!!)
                                }
                                val updatedDoctors = withContext(Dispatchers.IO) { doctorDao.getAllDoctors() }
                                doctors = updatedDoctors
                                showDeleteDialog = null
                                snackbarHostState.showSnackbar("Doctor deleted successfully")
                            }
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
} 