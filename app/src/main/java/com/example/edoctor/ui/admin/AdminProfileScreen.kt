package com.example.edoctor.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Import data layer
import com.example.edoctor.data.database.AppDatabase
import com.example.edoctor.data.dao.AdminDao
import com.example.edoctor.data.dao.DoctorDao
import com.example.edoctor.data.dao.PatientDao
import com.example.edoctor.data.dao.AppointmentDao
import com.example.edoctor.data.entities.AdminEntity
import com.example.edoctor.data.entities.DoctorEntity
import com.example.edoctor.data.entities.PatientEntity
import com.example.edoctor.data.entities.AppointmentEntity
import com.example.edoctor.utils.SessionManager
import com.example.edoctor.R

// Import SharedComponents
import com.example.edoctor.ui.common.StatCard
import com.example.edoctor.ui.common.UserCard
import com.example.edoctor.ui.common.AppointmentCard
import com.example.edoctor.ui.common.ProfileInfoRow
import com.example.edoctor.ui.common.DoctorCard
import com.example.edoctor.ui.common.PatientCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(navController: NavController, userId: Int) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val adminDao = db.adminDao()
    val doctorDao = db.doctorDao()
    val patientDao = db.patientDao()
    val appointmentDao = db.appointmentDao()
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var adminInfo by remember { mutableStateOf<AdminEntity?>(null) }
    var doctors by remember { mutableStateOf<List<DoctorEntity>>(emptyList()) }
    var patients by remember { mutableStateOf<List<PatientEntity>>(emptyList()) }
    var appointments by remember { mutableStateOf<List<AppointmentEntity>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(Unit) {
        val currentUserId = sessionManager.getCurrentUserId()
        if (currentUserId != -1) {
            try {
            val admin = withContext(Dispatchers.IO) {
                    adminDao.getAdminById(currentUserId)
            }
            adminInfo = admin
                
                val allDoctors = withContext(Dispatchers.IO) { doctorDao.getAllDoctors() }
                val allPatients = withContext(Dispatchers.IO) { patientDao.getAllPatients() }
                val allAppointments = withContext(Dispatchers.IO) { appointmentDao.getAllAppointments() }
                doctors = allDoctors
                patients = allPatients
                appointments = allAppointments
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            val allDoctors = withContext(Dispatchers.IO) { doctorDao.getAllDoctors() }
            val allPatients = withContext(Dispatchers.IO) { patientDao.getAllPatients() }
            val allAppointments = withContext(Dispatchers.IO) { appointmentDao.getAllAppointments() }
            doctors = allDoctors
            patients = allPatients
            appointments = allAppointments
        } else if (selectedTab == 1) {
            val allDoctors = withContext(Dispatchers.IO) { doctorDao.getAllDoctors() }
                doctors = allDoctors
        } else if (selectedTab == 2) {
            val allPatients = withContext(Dispatchers.IO) { patientDao.getAllPatients() }
                patients = allPatients
        } else if (selectedTab == 3) {
                val allAppointments = withContext(Dispatchers.IO) { appointmentDao.getAllAppointments() }
                appointments = allAppointments
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = {
                        sessionManager.clearLoginSession()
                        navController.navigate("welcome") { popUpTo(0) { inclusive = true } }
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF7F7F7))
        ) {
            // Welcome Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.default_profile),
                        contentDescription = "Admin Profile",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Welcome, ${adminInfo?.name ?: "Admin"}!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "System Administrator",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Doctors") }
                )
                    Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Patients") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Appointments") }
                    )
            }

            // Content based on selected tab
            when (selectedTab) {
                0 -> {
                    val statistics = AdminStatistics(
                        totalDoctors = doctors.size,
                        totalPatients = patients.size,
                        totalAppointments = appointments.size
                    )
                    DashboardContent(statistics, doctors, patients, appointments)
                }
                1 -> DoctorsContent(doctors, onDeleteUser = { user ->
                    showDeleteDialog = user
                })
                2 -> PatientsContent(patients, onDeleteUser = { user ->
                    showDeleteDialog = user
                })
                3 -> AppointmentsContent(appointments)
            }
        }

        // Delete Confirmation Dialog
        showDeleteDialog?.let { user ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete User") },
                text = { 
                    val userName = when (user) {
                        is DoctorEntity -> user.name
                        is PatientEntity -> user.name
                        else -> "Unknown"
                    }
                    Text("Are you sure you want to delete $userName? This action cannot be undone.") 
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    when (user) {
                                        is DoctorEntity -> doctorDao.deleteDoctor(user)
                                        is PatientEntity -> patientDao.deletePatient(user)
                                    }
                                }
                                val userName = when (user) {
                                    is DoctorEntity -> user.name
                                    is PatientEntity -> user.name
                                    else -> "Unknown"
                                }
                                snackbarHostState.showSnackbar("$userName deleted successfully")
                                showDeleteDialog = null
                                // Refresh current tab
                                selectedTab = selectedTab
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

@Composable
fun DashboardContent(
    statistics: AdminStatistics,
    doctors: List<DoctorEntity>,
    patients: List<PatientEntity>,
    appointments: List<AppointmentEntity>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "System Overview",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Statistics Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Total Doctors",
                    value = statistics.totalDoctors.toString(),
                    icon = Icons.Default.Person,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total Patients",
                    value = statistics.totalPatients.toString(),
                    icon = Icons.Default.Person,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            StatCard(
                title = "Total Appointments",
                value = statistics.totalAppointments.toString(),
                icon = Icons.Default.CalendarToday,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Recent Doctors
        item {
            Text(
                "Recent Doctors",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(doctors.take(3)) { doctor ->
            DoctorCard(
                doctor = doctor,
                showDeleteButton = false
            )
        }

        // Recent Patients
        item {
            Text(
                "Recent Patients",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(patients.take(3)) { patient ->
            PatientCard(
                patient = patient,
                showDeleteButton = false
            )
        }

        // Recent Appointments
        item {
            Text(
                "Recent Appointments",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(appointments.take(3)) { appointment ->
            AppointmentCard(appointment = appointment)
        }
    }
}

@Composable
fun DoctorsContent(
    doctors: List<DoctorEntity>,
    onDeleteUser: (DoctorEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
                onDelete = { onDeleteUser(doctor) }
            )
        }
    }
}

@Composable
fun PatientsContent(
    patients: List<PatientEntity>,
    onDeleteUser: (PatientEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Manage Patients (${patients.size})",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(patients) { patient ->
            PatientCard(
                patient = patient,
                showDeleteButton = true,
                onDelete = { onDeleteUser(patient) }
            )
        }
    }
}

@Composable
fun AppointmentsContent(appointments: List<AppointmentEntity>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "All Appointments (${appointments.size})",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(appointments) { appointment ->
            AppointmentCard(appointment = appointment)
        }
    }
}

enum class AdminTab(val title: String) {
    DASHBOARD("Dashboard"),
    DOCTORS("Doctors"),
    PATIENTS("Patients"),
    APPOINTMENTS("Appointments")
}

data class AdminStatistics(
    val totalDoctors: Int,
    val totalPatients: Int,
    val totalAppointments: Int
) 