package com.example.edoctor

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(navController: NavController, userId: Int) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val userDao = db.userDao()
    val appointmentDao = db.appointmentDao()
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var adminInfo by remember { mutableStateOf<UserEntity?>(null) }
    var doctors by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
    var patients by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
    var appointments by remember { mutableStateOf<List<AppointmentEntity>>(emptyList()) }
    var statistics by remember { mutableStateOf(AdminStatistics(0, 0, 0)) }
    var currentTab by remember { mutableStateOf(AdminTab.DASHBOARD) }
    var showDeleteDialog by remember { mutableStateOf<UserEntity?>(null) }

    // Load admin data
    LaunchedEffect(Unit) {
        val currentUserId = sessionManager.getCurrentUserId()
        if (currentUserId > 0) {
            val admin = withContext(Dispatchers.IO) {
                userDao.getUserById(currentUserId)
            }
            adminInfo = admin
        }
    }

    // Load all data
    LaunchedEffect(currentTab) {
        when (currentTab) {
            AdminTab.DASHBOARD -> {
                val allDoctors = withContext(Dispatchers.IO) { userDao.getAllDoctors() }
                val allPatients = withContext(Dispatchers.IO) { userDao.getAllPatients() }
                val allAppointments = withContext(Dispatchers.IO) { appointmentDao.getAllAppointments() }
                
                doctors = allDoctors
                patients = allPatients
                appointments = allAppointments
                statistics = AdminStatistics(
                    totalDoctors = allDoctors.size,
                    totalPatients = allPatients.size,
                    totalAppointments = allAppointments.size
                )
            }
            AdminTab.DOCTORS -> {
                val allDoctors = withContext(Dispatchers.IO) { userDao.getAllDoctors() }
                doctors = allDoctors
            }
            AdminTab.PATIENTS -> {
                val allPatients = withContext(Dispatchers.IO) { userDao.getAllPatients() }
                patients = allPatients
            }
            AdminTab.APPOINTMENTS -> {
                val allAppointments = withContext(Dispatchers.IO) { appointmentDao.getAllAppointments() }
                appointments = allAppointments
            }
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
                selectedTabIndex = currentTab.ordinal,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                AdminTab.values().forEach { tab ->
                    Tab(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        text = { Text(tab.title) }
                    )
                }
            }

            // Content based on selected tab
            when (currentTab) {
                AdminTab.DASHBOARD -> DashboardContent(statistics, doctors, patients, appointments)
                AdminTab.DOCTORS -> DoctorsContent(doctors, onDeleteUser = { user ->
                    showDeleteDialog = user
                })
                AdminTab.PATIENTS -> PatientsContent(patients, onDeleteUser = { user ->
                    showDeleteDialog = user
                })
                AdminTab.APPOINTMENTS -> AppointmentsContent(appointments)
            }
        }

        // Delete Confirmation Dialog
        showDeleteDialog?.let { user ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete User") },
                text = { Text("Are you sure you want to delete ${user.name}? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    userDao.deleteUser(user)
                                }
                                snackbarHostState.showSnackbar("${user.name} deleted successfully")
                                showDeleteDialog = null
                                // Refresh current tab
                                currentTab = currentTab
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
    doctors: List<UserEntity>,
    patients: List<UserEntity>,
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
            UserCard(
                user = doctor,
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
            UserCard(
                user = patient,
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
    doctors: List<UserEntity>,
    onDeleteUser: (UserEntity) -> Unit
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
            UserCard(
                user = doctor,
                showDeleteButton = true,
                onDelete = { onDeleteUser(doctor) }
            )
        }
    }
}

@Composable
fun PatientsContent(
    patients: List<UserEntity>,
    onDeleteUser: (UserEntity) -> Unit
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
            UserCard(
                user = patient,
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