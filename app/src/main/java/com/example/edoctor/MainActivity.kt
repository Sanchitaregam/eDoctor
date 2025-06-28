package com.example.edoctor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EDoctorTheme {
                val navController = rememberNavController()
                Surface(color = MaterialTheme.colorScheme.background) {
                    NavHost(navController = navController, startDestination = "welcome") {
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







