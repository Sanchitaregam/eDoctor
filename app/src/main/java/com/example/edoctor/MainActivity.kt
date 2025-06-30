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
import com.example.edoctor.ui.doctor.DoctorDashboardScreen
import com.example.edoctor.ui.doctor.DoctorAvailabilityScreen
import com.example.edoctor.ui.doctor.DoctorAppointmentsScreen
import com.example.edoctor.ui.doctor.DoctorPatientsScreen
import com.example.edoctor.ui.patient.PatientDashboardScreen
import com.example.edoctor.ui.patient.PatientAppointmentsScreen
import com.example.edoctor.ui.auth.LoginScreen
import com.example.edoctor.ui.auth.LoginRoleScreen
import com.example.edoctor.ui.auth.AdminRegistrationScreen
import com.example.edoctor.ui.auth.PatientRegistrationScreen
import com.example.edoctor.ui.auth.EnhancedDoctorRegistrationScreen
import com.example.edoctor.ui.common.SettingsScreen
import com.example.edoctor.ui.common.ChangeEmailScreen
import com.example.edoctor.ui.common.ChangePasswordScreen
import com.example.edoctor.ui.common.CalendarScreen
import com.example.edoctor.ui.common.AppointmentCard
import com.example.edoctor.utils.SessionManager

// Import data layer
import com.example.edoctor.data.database.AppDatabase
import com.example.edoctor.data.dao.UserDao
import com.example.edoctor.data.dao.AppointmentDao
import com.example.edoctor.data.dao.AvailabilityDao
import com.example.edoctor.data.entities.UserEntity
import com.example.edoctor.data.entities.AppointmentEntity
import com.example.edoctor.data.entities.AvailabilityEntity

// Import patient UI components
import com.example.edoctor.ui.patient.BookAppointmentDoctorSelectionScreen
import com.example.edoctor.ui.patient.MedicalHistoryScreen

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
                            DoctorDashboardScreen(navController, userId)
                        }

                        composable(
                            "patient_profile/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                            PatientDashboardScreen(navController, userId)
                        }

                        composable(
                            "admin_profile/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                            AdminProfileScreen(navController, userId)
                        }

                        composable(
                            "doctor_availability/{doctorId}",
                            arguments = listOf(navArgument("doctorId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val doctorId = backStackEntry.arguments?.getInt("doctorId") ?: 0
                            DoctorAvailabilityScreen(navController, doctorId)
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
                            CalendarScreen(navController, doctorId, patientId, patientName)
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
                            "medical_history/{patientId}",
                            arguments = listOf(navArgument("patientId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val patientId = backStackEntry.arguments?.getInt("patientId") ?: 0
                            MedicalHistoryScreen(navController, patientId)
                        }
                        
                        // DoctorAppointmentsScreen moved to ui/doctor/DoctorAppointmentsScreen.kt
                        
                        composable(
                            "doctor_appointments/{doctorId}",
                            arguments = listOf(
                                navArgument("doctorId") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val doctorId = backStackEntry.arguments?.getInt("doctorId") ?: 0
                            DoctorAppointmentsScreen(navController, doctorId)
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
                        
                        // PatientsScreen and PatientCard moved to ui/doctor/DoctorPatientsScreen.kt
                        
                        composable(
                            "select_doctor/{patientId}",
                            arguments = listOf(navArgument("patientId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val patientId = backStackEntry.arguments?.getInt("patientId") ?: 0
                            BookAppointmentDoctorSelectionScreen(navController, patientId)
                        }
                        
                        composable(
                            "patients_screen/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                            DoctorPatientsScreen(navController, userId)
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
    val db = remember { AppDatabase.getDatabase(context) }
    val appointmentDao = db.appointmentDao()
    val userDao = db.userDao()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var doctor by remember { mutableStateOf<UserEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Load doctor information
    LaunchedEffect(doctorId) {
        try {
            doctor = withContext(Dispatchers.IO) {
                userDao.getUserById(doctorId)
            }
        } catch (e: Exception) {
            doctor = null
        } finally {
            isLoading = false
        }
    }

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
            
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Doctor: ${doctor?.name ?: "Unknown"}")
                doctor?.specialization?.let { specialization ->
                    Text("Specialization: $specialization")
                }
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

                        // Navigate immediately without any snackbar delay
                        try {
                            navController.navigate("patient_profile/$patientId") {
                                popUpTo(0) { inclusive = false }
                            }
                        } catch (e: Exception) {
                            // Fallback: try to pop back to the start and navigate
                            navController.popBackStack(0, false)
                            navController.navigate("patient_profile/$patientId")
                        }
                        
                        // Show snackbar after navigation (optional)
                        snackbarHostState.showSnackbar("Appointment booked successfully")
                    }
                }) {
                    Text("Confirm Booking")
                }
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
fun DoctorAppointmentsScreen(navController: NavController, doctorId: Int) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val appointmentDao = db.appointmentDao()
    val userDao = db.userDao()
    
    var appointments by remember { mutableStateOf<List<AppointmentEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(doctorId) {
        try {
            val allAppointments = withContext(Dispatchers.IO) {
                appointmentDao.getAppointmentsForDoctor(doctorId)
            }
            
            // Filter to only show upcoming appointments (today and future)
            val today = java.time.LocalDate.now().toString()
            val upcomingAppointments = allAppointments.filter { appointment ->
                appointment.date >= today
            }
            
            appointments = upcomingAppointments.sortedBy { it.date }
        } catch (e: Exception) {
            appointments = emptyList()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upcoming Appointments") },
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
        } else if (appointments.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No Upcoming Appointments",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "You don't have any upcoming appointments scheduled.",
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
                        "Upcoming Appointments (${appointments.size})",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(appointments) { appointment ->
                    AppointmentCard(appointment = appointment)
                }
            }
        }
    }
}







