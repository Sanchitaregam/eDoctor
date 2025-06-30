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
import androidx.compose.ui.text.style.TextAlign
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
import com.example.edoctor.ui.common.ActionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController, userId: Int) {
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
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf<Any?>(null) }
    var showProfileDialog by remember { mutableStateOf(false) }

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
                val allAppointments = withContext(Dispatchers.IO) { appointmentDao.getUpcomingAppointments() }
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

    // Always reload data when dialog is opened
    LaunchedEffect(showProfileDialog) {
        if (showProfileDialog) {
            val currentUserId = sessionManager.getCurrentUserId()
            if (currentUserId != -1) {
                val admin = withContext(Dispatchers.IO) {
                    adminDao.getAdminById(currentUserId)
                }
                adminInfo = admin
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = { showProfileDialog = true }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF7F7F7)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card at the very top
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                .size(72.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                adminInfo?.name ?: "Admin",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                adminInfo?.email ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "System Administrator",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Quick Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Total Doctors",
                        value = doctors.size.toString(),
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Patients",
                        value = patients.size.toString(),
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                StatCard(
                    title = "Total Appointments",
                    value = appointments.size.toString(),
                    icon = Icons.Default.CalendarToday,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Quick Actions
            item {
                Text(
                    "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                ActionCard(
                    title = "Manage Doctors",
                    subtitle = "View, approve, and manage doctor accounts",
                    icon = Icons.Default.Person,
                    onClick = { 
                        // Navigate to doctors management
                        navController.navigate("admin_doctors")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                ActionCard(
                    title = "Manage Patients",
                    subtitle = "View and manage patient accounts",
                    icon = Icons.Default.Person,
                    onClick = { 
                        // Navigate to patients management
                        navController.navigate("admin_patients")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                ActionCard(
                    title = "View Appointments",
                    subtitle = "Monitor all appointments in the system",
                    icon = Icons.Default.CalendarToday,
                    onClick = { 
                        // Navigate to appointments view
                        navController.navigate("admin_appointments")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                ActionCard(
                    title = "Settings",
                    subtitle = "Edit profile, change email/password, and logout",
                    icon = Icons.Default.Settings,
                    onClick = { navController.navigate("settings") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Profile Dialog
        if (showProfileDialog) {
            AlertDialog(
                onDismissRequest = { showProfileDialog = false },
                title = { Text("Profile Information") },
                text = {
                    Column {
                        if (adminInfo != null) {
                            ProfileInfoRow("Name", adminInfo!!.name.ifEmpty { "Not set" })
                            ProfileInfoRow("Email", adminInfo!!.email.ifEmpty { "Not set" })
                            ProfileInfoRow("Phone", adminInfo!!.phone.ifEmpty { "Not set" })
                            ProfileInfoRow("Date of Birth", adminInfo!!.dob ?: "Not set")
                            ProfileInfoRow("Gender", adminInfo!!.gender.ifEmpty { "Not set" })
                            ProfileInfoRow("Address", adminInfo!!.address ?: "Not set")
                        } else {
                            Text(
                                "Profile data not found in database.\n\nThis may happen if the database was reset or the user account was deleted.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Please log in again to refresh your session.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showProfileDialog = false }) {
                        Text("Close")
                    }
                },
                dismissButton = {
                    if (adminInfo == null) {
                        TextButton(
                            onClick = { 
                                sessionManager.clearLoginSession()
                                showProfileDialog = false
                                navController.navigate("welcome") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        ) {
                            Text("Logout")
                        }
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Confirm Delete") },
                text = { Text("Are you sure you want to delete this user? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                when (showDeleteDialog) {
                                    is DoctorEntity -> {
                                        withContext(Dispatchers.IO) {
                                            doctorDao.deleteDoctor(showDeleteDialog as DoctorEntity)
                                        }
                                        val updatedDoctors = withContext(Dispatchers.IO) { doctorDao.getAllDoctors() }
                                        doctors = updatedDoctors
                                    }
                                    is PatientEntity -> {
                                        withContext(Dispatchers.IO) {
                                            patientDao.deletePatient(showDeleteDialog as PatientEntity)
                                        }
                                        val updatedPatients = withContext(Dispatchers.IO) { patientDao.getAllPatients() }
                                        patients = updatedPatients
                                    }
                                }
                                showDeleteDialog = null
                                snackbarHostState.showSnackbar("User deleted successfully")
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