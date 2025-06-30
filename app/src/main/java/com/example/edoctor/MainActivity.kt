package com.example.edoctor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.edoctor.ui.theme.EDoctorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Notifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip

// Import moved UI components
import com.example.edoctor.ui.admin.AdminProfileScreen
import com.example.edoctor.ui.doctor.DoctorProfileScreen
import com.example.edoctor.ui.doctor.DoctorAvailabilityScreen
import com.example.edoctor.ui.patient.PatientProfileScreen
import com.example.edoctor.ui.patient.PatientAppointmentsScreen
import com.example.edoctor.ui.patient.PatientDetailsScreen
import com.example.edoctor.ui.auth.LoginScreen
import com.example.edoctor.ui.auth.LoginRoleScreen
import com.example.edoctor.ui.auth.AdminRegistrationScreen
import com.example.edoctor.ui.auth.PatientRegistrationScreen
import com.example.edoctor.ui.auth.EnhancedDoctorRegistrationScreen
import com.example.edoctor.ui.common.HealthTipsScreen
import com.example.edoctor.ui.common.SettingsScreen
import com.example.edoctor.ui.common.ChangeEmailScreen
import com.example.edoctor.ui.common.ChangePasswordScreen
import com.example.edoctor.ui.common.CalendarScreen
import com.example.edoctor.utils.SessionManager

// Import data layer
import com.example.edoctor.data.database.AppDatabase
import com.example.edoctor.data.dao.UserDao
import com.example.edoctor.data.dao.AppointmentDao
import com.example.edoctor.data.dao.AvailabilityDao
import com.example.edoctor.data.entities.UserEntity
import com.example.edoctor.data.entities.AppointmentEntity
import com.example.edoctor.data.entities.AvailabilityEntity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EDoctorTheme {
                val context = LocalContext.current
                val sessionManager = remember { SessionManager(context) }
                val navController = rememberNavController()
                
                // Determine starting destination based on login status
                val startDestination = remember {
                    if (sessionManager.isLoggedIn()) {
                        sessionManager.getLoginDestination()
                    } else {
                        "welcome"
                    }
                }
                
                Surface(color = MaterialTheme.colorScheme.background) {
                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("welcome") { WelcomeScreen(navController) }
                        composable("register") { RegisterRoleScreen(navController) }
                        composable("select_login_role") { LoginRoleScreen(navController) }
                        composable("login/{role}") { backStackEntry ->
                            val role = backStackEntry.arguments?.getString("role") ?: "unknown"
                            LoginScreen(navController, role)
                        }
                        composable("login/admin") { LoginScreen(navController, "admin") }
                        composable("login/doctor") { LoginScreen(navController, "doctor") }
                        composable("login/patient") { LoginScreen(navController, "patient") }
                        composable("patient_registration") { PatientRegistrationScreen(navController) }
                        composable("doctor_registration") { EnhancedDoctorRegistrationScreen(navController) }
                        composable("admin_registration") { AdminRegistrationScreen(navController) }

                        composable(
                            "doctor_profile/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                            DoctorProfileScreen(navController, userId)
                        }

                        composable(
                            "patient_profile/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                            PatientProfileScreen(navController, userId)
                        }

                        composable(
                            "admin_profile/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                            AdminProfileScreen(navController, userId)
                        }

                        composable(
                            "patient_details/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                            PatientDetailsScreen(navController, userId)
                        }

                        composable(
                            "doctor_availability/{doctorId}",
                            arguments = listOf(navArgument("doctorId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val doctorId = backStackEntry.arguments?.getInt("doctorId") ?: 0
                            DoctorAvailabilityScreen(navController, doctorId)
                        }
                        composable("health_tips") {
                            HealthTipsScreen(navController)
                        }

                        // ✅ Updated calendar route to pass doctorId and patientId
                        composable(
                            "calendar/{doctorId}/{patientId}/{patientName}",
                            arguments = listOf(
                                navArgument("doctorId") { type = NavType.IntType },
                                navArgument("patientId") { type = NavType.IntType },
                                navArgument("patientName") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val doctorId = backStackEntry.arguments?.getInt("doctorId") ?: 0
                            val patientId = backStackEntry.arguments?.getInt("patientId") ?: 0
                            val patientName = backStackEntry.arguments?.getString("patientName") ?: ""
                            CalendarScreen { selectedDate ->
                                // For now, just show the selected date
                                // TODO: Implement appointment booking
                            }
                        }

                        composable(
                            "patient_appointments/{doctorId}/{patientId}",
                            arguments = listOf(
                                navArgument("doctorId") { type = NavType.IntType },
                                navArgument("patientId") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val doctorId = backStackEntry.arguments?.getInt("doctorId") ?: 0
                            val patientId = backStackEntry.arguments?.getInt("patientId") ?: 0
                            PatientAppointmentsScreen(navController, doctorId, patientId)
                        }
                        composable(
                            "change_password/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                            ChangePasswordScreen(navController, userId)
                        }
                        composable("change_email") {
                            val context = LocalContext.current
                            val sessionManager = remember { SessionManager(context) }
                            val userId = sessionManager.getCurrentUserId()
                            ChangeEmailScreen(navController, userId)
                        }
                        composable("settings") { SettingsScreen(navController) }
                        
                        composable(
                            "patients_screen/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                            PatientsScreen(navController, userId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(navController: NavController) {
    var showLogo by remember { mutableStateOf(false) }
    var showDoctor by remember { mutableStateOf(false) }
    var showLogin by remember { mutableStateOf(false) }
    var showRegister by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        showLogo = true
        delay(300)
        showDoctor = true
        delay(300)
        showLogin = true
        delay(300)
        showRegister = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = showLogo,
            enter = slideInHorizontally(initialOffsetX = { -200 }) + fadeIn()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Image(
                    painter = painterResource(id = R.drawable.edoctor_logo),
                    contentDescription = "eDoctor Logo",
                    modifier = Modifier
                        .height(80.dp)
                        .width(80.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Smart Health, Anytime, Anywhere",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedVisibility(
            visible = showDoctor,
            enter = slideInHorizontally(initialOffsetX = { -200 }) + fadeIn()
        ) {
            Image(
                painter = painterResource(id = R.drawable.doctor),
                contentDescription = "Doctor Image",
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedVisibility(
                visible = showLogin,
                enter = slideInHorizontally(initialOffsetX = { -200 }) + fadeIn()
            ) {
                Button(
                    onClick = { navController.navigate("select_login_role") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Login")
                }
            }

            AnimatedVisibility(
                visible = showRegister,
                enter = slideInHorizontally(initialOffsetX = { -200 }) + fadeIn()
            ) {
                Button(
                    onClick = { navController.navigate("register") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Register")
                }
            }
        }
    }
}

@Composable
fun RegisterRoleScreen(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.hospital_bg),
            contentDescription = "Hospital Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { navController.navigate("admin_registration") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Register as Admin")
            }

            Button(
                onClick = { navController.navigate("doctor_registration") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Register as Doctor")
            }

            Button(
                onClick = { navController.navigate("patient_registration") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Register as Patient")
            }
        }
    }
}

@Composable
fun BookAppointmentScreen(
    navController: NavController,
    doctorId: Int,
    patientId: Int,
    patientName: String,
    date: String,
    time: String
) {
    val context = LocalContext.current
    val appointmentDao = AppDatabase.getDatabase(context).appointmentDao()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Confirm Booking", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Text("Doctor ID: $doctorId")
            Text("Patient Name: $patientName")
            Text("Appointment Date: $date")
            Text("Time: $time")
            Spacer(Modifier.height(24.dp))

            Button(onClick = {
                coroutineScope.launch {
                    val appointment = AppointmentEntity(
                        doctorId = doctorId,
                        patientId = patientId,
                        patientName = patientName,
                        date = date,
                        time = time
                    )
                    appointmentDao.insertAppointment(appointment)

                    snackbarHostState.showSnackbar("Appointment booked successfully")
                    delay(1000)
                    navController.navigate("patient_profile/$patientId") {
                        popUpTo("book_appointment/$doctorId/$patientId/$patientName/$date/$time") { inclusive = true }
                    }
                }
            }) {
                Text("Confirm Booking")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsScreen(navController: NavController, userId: Int) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val userDao = db.userDao()
    val appointmentDao = db.appointmentDao()
    
    var patients by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
    var appointments by remember { mutableStateOf<List<AppointmentEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        try {
            // Get all patients who have appointments with this doctor
            val doctorAppointments = withContext(Dispatchers.IO) {
                appointmentDao.getAppointmentsForDoctor(userId)
            }
            appointments = doctorAppointments
            
            // Get unique patient IDs from appointments
            val patientIds = doctorAppointments.map { it.patientId }.distinct()
            
            // Get patient details for each patient ID
            val patientList = mutableListOf<UserEntity>()
            patientIds.forEach { patientId ->
                val patient = withContext(Dispatchers.IO) {
                    userDao.getUserById(patientId)
                }
                patient?.let { patientList.add(it) }
            }
            patients = patientList
        } catch (e: Exception) {
            // Handle any errors gracefully
            patients = emptyList()
            appointments = emptyList()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Patients (${patients.size})") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
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
        } else if (patients.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No Patients Yet",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "You don't have any patients yet. Patients will appear here once they book appointments with you.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Your Patients",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(patients) { patient ->
                    PatientCard(
                        patient = patient,
                        appointmentCount = appointments.count { it.patientId == patient.id }
                    )
                }
            }
        }
    }
}

@Composable
fun PatientCard(
    patient: UserEntity,
    appointmentCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.default_profile),
                contentDescription = patient.name,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = patient.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = patient.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$appointmentCount appointment${if (appointmentCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(navController: NavController, userId: Int) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Messages Screen",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "This feature is coming soon!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorSelectionScreen(navController: NavController, patientId: Int) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val userDao = db.userDao()
    val availabilityDao = db.availabilityDao()
    
    var doctors by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
    var doctorAvailabilities by remember { mutableStateOf<Map<Int, List<AvailabilityEntity>>>(emptyMap()) }
    var patient by remember { mutableStateOf<UserEntity?>(null) }

    LaunchedEffect(patientId) {
        val loadedDoctors = withContext(Dispatchers.IO) {
            userDao.getAllDoctors()
        }
        doctors = loadedDoctors
        
        val loadedPatient = withContext(Dispatchers.IO) {
            userDao.getUserById(patientId)
        }
        patient = loadedPatient
        
        // Load availability for each doctor
        val availabilities = mutableMapOf<Int, List<AvailabilityEntity>>()
        loadedDoctors.forEach { doctor ->
            val doctorAvailability = withContext(Dispatchers.IO) {
                availabilityDao.getAvailabilityForDoctor(doctor.id)
            }
            availabilities[doctor.id] = doctorAvailability
        }
        doctorAvailabilities = availabilities
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Doctor") },
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
                Text(
                    "Available Doctors",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(doctors) { doctor ->
                DoctorAvailabilityCard(
                    doctor = doctor,
                    availability = doctorAvailabilities[doctor.id] ?: emptyList(),
                    onBookClick = {
                        navController.navigate("calendar/${doctor.id}/$patientId/${patient?.name ?: ""}")
                    }
                )
            }
        }
    }
}

@Composable
fun DoctorAvailabilityCard(
    doctor: UserEntity,
    availability: List<AvailabilityEntity>,
    onBookClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.default_profile),
                    contentDescription = doctor.name,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        doctor.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        doctor.specialization ?: doctor.experience ?: "General Physician",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (doctor.rating > 0) {
                        Row {
                            repeat(doctor.rating.toInt()) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                " ${doctor.rating}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (availability.isNotEmpty()) {
                Text(
                    "Available Days:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                availability.forEach { avail ->
                    Text(
                        "${avail.days} - ${avail.fromTime} to ${avail.toTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    "No availability set",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onBookClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = availability.isNotEmpty()
            ) {
                Text("Book Appointment")
            }
        }
    }
}







