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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip

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
                            "edit_doctor_profile/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                            EditDoctorProfileScreen(navController, userId)
                        }

                        composable(
                            "edit_patient_profile/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                            EditPatientProfileScreen(navController, userId)
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
                                // Navigate to book_appointment with date and time (hardcoded time for now)
                                navController.navigate("book_appointment/$doctorId/$patientId/$patientName/${selectedDate.toString()}/10:00 AM")
                            }
                        }

                        composable(
                            "book_appointment/{doctorId}/{patientId}/{patientName}/{date}/{time}",
                            arguments = listOf(
                                navArgument("doctorId") { type = NavType.IntType },
                                navArgument("patientId") { type = NavType.IntType },
                                navArgument("patientName") { type = NavType.StringType },
                                navArgument("date") { type = NavType.StringType },
                                navArgument("time") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val doctorId = backStackEntry.arguments?.getInt("doctorId") ?: 0
                            val patientId = backStackEntry.arguments?.getInt("patientId") ?: 0
                            val patientName = backStackEntry.arguments?.getString("patientName") ?: ""
                            val date = backStackEntry.arguments?.getString("date") ?: ""
                            val time = backStackEntry.arguments?.getString("time") ?: ""
                            BookAppointmentScreen(navController, doctorId, patientId, patientName, date, time)
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
                        
                        composable(
                            "messages_screen/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                            MessagesScreen(navController, userId)
                        }
                        
                        composable(
                            "select_doctor/{patientId}",
                            arguments = listOf(navArgument("patientId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val patientId = backStackEntry.arguments?.getInt("patientId") ?: 0
                            DoctorSelectionScreen(navController, patientId)
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
                onClick = { navController.navigate("patient_registration") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Register as Patient")
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
                onClick = { navController.navigate("admin_registration") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Register as Admin")
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patients") },
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
                "Patients Screen",
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







