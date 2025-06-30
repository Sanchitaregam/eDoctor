package com.example.edoctor

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HealthAndSafety
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileScreen(navController: NavController, userId: Int) {
    val context = LocalContext.current
    val userDao = AppDatabase.getDatabase(context).userDao()
    val appointmentDao = AppDatabase.getDatabase(context).appointmentDao()
    val sessionManager = remember { SessionManager(context) }
    
    var patient by remember { mutableStateOf<UserEntity?>(null) }
    var bookedAppointments by remember { mutableStateOf<List<AppointmentEntity>>(emptyList()) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<UserEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        val currentUserId = sessionManager.getCurrentUserId()
        if (currentUserId != -1) {
            val loadedPatient = withContext(Dispatchers.IO) {
                userDao.getUserById(currentUserId)
            }
            patient = loadedPatient
            
            // Load booked appointments
            val appointments = withContext(Dispatchers.IO) {
                appointmentDao.getAppointmentsByPatientId(currentUserId)
            }
            bookedAppointments = appointments.sortedBy { it.date }.take(5) // Show next 5 appointments
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patient Dashboard") },
                actions = {
                    IconButton(
                        onClick = { 
                            Log.d("PatientProfile", "Profile icon clicked, patient: ${patient?.name}")
                            showProfileDialog = true 
                        }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_profile),
                            contentDescription = "Profile",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF7F7F7)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Section
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
                            contentDescription = "Patient Profile",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "Hi, ${patient?.name ?: "Patient"}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Welcome to eDoctor",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        title = "Booked Appointments",
                        value = bookedAppointments.size.toString(),
                        icon = Icons.Default.CalendarToday,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Health Tips",
                        value = "5",
                        icon = Icons.Default.HealthAndSafety,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search Doctors Section
            item {
                Text(
                    "Find a Doctor",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { 
                                searchQuery = it
                                showSearchResults = it.isNotEmpty()
                            },
                            label = { Text("Search by specialty or doctor name") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        if (showSearchResults) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = {
                                    // Perform search
                                    showSearchResults = false
                                    val currentUserId = sessionManager.getCurrentUserId()
                                    navController.navigate("select_doctor/$currentUserId")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Search Doctors")
                            }
                        }
                    }
                }
            }

            // Quick Book Appointment
            item {
                ActionCard(
                    title = "Book New Appointment",
                    subtitle = "Find and book with available doctors",
                    icon = Icons.Default.CalendarToday,
                    onClick = {
                        val currentUserId = sessionManager.getCurrentUserId()
                        navController.navigate("select_doctor/$currentUserId")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Booked Appointments Section
            item {
                Text(
                    "My Appointments",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (bookedAppointments.isNotEmpty()) {
                items(bookedAppointments) { appointment ->
                    PatientAppointmentCard(appointment = appointment)
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No appointments booked yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionCard(
                        title = "Edit Profile",
                        icon = Icons.Default.Edit,
                        onClick = { navController.navigate("edit_patient_profile/${patient?.id ?: 0}") },
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title = "Health Tips",
                        icon = Icons.Default.HealthAndSafety,
                        onClick = { navController.navigate("health_tips") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionCard(
                        title = "Medical History",
                        icon = Icons.Default.Person,
                        onClick = { /* Navigate to medical history */ },
                        modifier = Modifier.weight(1f)
                    )
                    ActionCard(
                        title = "Settings",
                        icon = Icons.Default.Person,
                        onClick = { navController.navigate("settings") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Account Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(
                                onClick = { navController.navigate("settings") }
                            ) {
                                Text("Settings")
                            }
                            TextButton(
                                onClick = {
                                    sessionManager.clearLoginSession()
                                    navController.navigate("welcome") { popUpTo(0) { inclusive = true } }
                                }
                            ) {
                                Text("Logout")
                            }
                        }
                    }
                }
            }
        }

        // Profile Dialog
        if (showProfileDialog && patient != null) {
            AlertDialog(
                onDismissRequest = { showProfileDialog = false },
                title = { Text("Patient Profile") },
                text = {
                    Column {
                        ProfileInfoRow("Name", patient?.name ?: "N/A")
                        ProfileInfoRow("Email", patient?.email ?: "N/A")
                        ProfileInfoRow("Phone", patient?.phone ?: "N/A")
                        ProfileInfoRow("Gender", patient?.gender ?: "N/A")
                        ProfileInfoRow("Date of Birth", patient?.dob ?: "N/A")
                        ProfileInfoRow("Address", patient?.address ?: "N/A")
                        ProfileInfoRow("Blood Group", patient?.bloodGroup ?: "N/A")
                        ProfileInfoRow("Emergency Contact", patient?.emergencyContact ?: "N/A")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showProfileDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPatientProfileScreen(navController: NavController, userId: Int) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val userDao = db.userDao()
    val coroutineScope = rememberCoroutineScope()

    var user by remember { mutableStateOf<UserEntity?>(null) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        val fetchedUser = withContext(Dispatchers.IO) {
            userDao.getUserById(userId)
        }
        fetchedUser?.let {
            user = it
            name = it.name
            phone = it.phone
            dob = it.dob ?: ""
            address = it.address ?: ""
            bloodGroup = it.bloodGroup ?: ""
            emergencyContact = it.emergencyContact ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text("Date of Birth") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
            
            item {
                OutlinedTextField(
                    value = bloodGroup,
                    onValueChange = { bloodGroup = it },
                    label = { Text("Blood Group") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                OutlinedTextField(
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    label = { Text("Emergency Contact") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            user?.let {
                                val updated = it.copy(
                                    name = name,
                                    phone = phone,
                                    dob = dob.ifEmpty { null },
                                    address = address.ifEmpty { null },
                                    bloodGroup = bloodGroup.ifEmpty { null },
                                    emergencyContact = emergencyContact.ifEmpty { null }
                                )
                                withContext(Dispatchers.IO) {
                                    userDao.updateUser(updated)
                                }
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Changes")
                }
            }
        }
    }
} 