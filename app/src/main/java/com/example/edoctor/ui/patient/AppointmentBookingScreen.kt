package com.example.edoctor.ui.patient

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

// Import data layer
import com.example.edoctor.data.database.AppDatabase
import com.example.edoctor.data.dao.AvailabilityDao
import com.example.edoctor.data.entities.AvailabilityEntity
import com.example.edoctor.data.entities.AppointmentEntity
import com.example.edoctor.data.dao.AppointmentDao

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppointmentBookingScreen(
    navController: NavController,
    doctorId: Int,
    patientId: Int,
    patientName: String
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val availabilityDao = db.availabilityDao()
    val appointmentDao = db.appointmentDao()
    val coroutineScope = rememberCoroutineScope()
    
    var selectedDate by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var selectedTime by remember { mutableStateOf("") }
    var doctorAvailability by remember { mutableStateOf<List<AvailabilityEntity>>(emptyList()) }
    var existingAppointments by remember { mutableStateOf<List<AppointmentEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var availableTimeSlots by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    // Load doctor's availability and existing appointments
    LaunchedEffect(doctorId) {
        try {
            doctorAvailability = withContext(Dispatchers.IO) {
                availabilityDao.getAvailabilityForDoctor(doctorId)
            }
            existingAppointments = withContext(Dispatchers.IO) {
                appointmentDao.getAppointmentsForDoctor(doctorId)
            }
        } catch (e: Exception) {
            doctorAvailability = emptyList()
            existingAppointments = emptyList()
        } finally {
            isLoading = false
        }
    }

    // Check if a date is available
    fun isDateAvailable(date: LocalDate): Boolean {
        val dayOfWeekFull = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).trim().lowercase()
        return doctorAvailability.any { it.days.trim().lowercase() == dayOfWeekFull }
    }

    // Check if a time slot is already booked
    fun isTimeSlotBooked(date: LocalDate, time: String): Boolean {
        val dateString = date.toString()
        return existingAppointments.any { appointment ->
            appointment.date == dateString && appointment.time == time
        }
    }

    // Generate time slots based on selected date and doctor's availability
    fun generateTimeSlots(date: LocalDate): List<String> {
        val dayOfWeekFull = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).trim().lowercase()
        val isAvailable = doctorAvailability.any { it.days.trim().lowercase() == dayOfWeekFull }
        if (!isAvailable) return emptyList()
        
        val timeSlots = mutableListOf<String>()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val startTime = java.time.LocalTime.parse("09:00", formatter)
        val endTime = java.time.LocalTime.parse("16:00", formatter)
        var currentTime = startTime
        
        while (!currentTime.isAfter(endTime)) {
            val timeString = currentTime.format(DateTimeFormatter.ofPattern("h:mm a"))
            timeSlots.add(timeString)
            currentTime = currentTime.plusHours(1)
        }
        return timeSlots
    }

    // Update available time slots when date changes
    LaunchedEffect(selectedDate, doctorAvailability, existingAppointments) {
        availableTimeSlots = generateTimeSlots(selectedDate)
        if (availableTimeSlots.isNotEmpty() && selectedTime.isEmpty()) {
            selectedTime = availableTimeSlots.first()
        } else if (selectedTime.isNotEmpty() && !availableTimeSlots.contains(selectedTime)) {
            // If currently selected time is no longer available, clear it
            selectedTime = ""
        }
    }

    // Generate calendar days
    fun getCalendarDays(): List<LocalDate?> {
        val firstDayOfMonth = currentMonth.atDay(1)
        val lastDayOfMonth = currentMonth.atEndOfMonth()
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
        
        val days = mutableListOf<LocalDate?>()
        
        // Add empty cells for days before the first day of the month
        repeat(firstDayOfWeek) {
            days.add(null)
        }
        
        // Add all days of the month
        var currentDay = firstDayOfMonth
        while (!currentDay.isAfter(lastDayOfMonth)) {
            days.add(currentDay)
            currentDay = currentDay.plusDays(1)
        }
        
        return days
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Select Appointment Date & Time", 
            fontSize = 20.sp, 
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Custom Calendar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Month navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { 
                            currentMonth = currentMonth.minusMonths(1)
                        }
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, "Previous Month")
                    }
                    
                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(
                        onClick = { 
                            currentMonth = currentMonth.plusMonths(1)
                        }
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, "Next Month")
                    }
                }

        Spacer(modifier = Modifier.height(16.dp))

                // Day headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(240.dp)
                ) {
                    items(getCalendarDays()) { date ->
                        if (date == null) {
                            // Empty cell
                            Box(modifier = Modifier.size(40.dp))
                        } else {
                            val isAvailable = isDateAvailable(date)
                            val isSelected = date == selectedDate
                            val isToday = date == LocalDate.now()
                            val isPastOrToday = !date.isAfter(LocalDate.now())
                            val enabled = isAvailable && !isPastOrToday
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isAvailable && !isPastOrToday -> MaterialTheme.colorScheme.primaryContainer
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = if (isToday) 2.dp else 0.dp,
                                        color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable(
                                        enabled = enabled
                                    ) {
                                        selectedDate = date
                                        selectedTime = ""
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isAvailable && !isPastOrToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Selected Date: ${selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))}", 
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else if (availableTimeSlots.isEmpty()) {
            val isDateAvailable = isDateAvailable(selectedDate)
            val hasBookedSlots = selectedDate.isAfter(LocalDate.now()) && isDateAvailable && 
                existingAppointments.any { it.date == selectedDate.toString() }
            
            Text(
                if (hasBookedSlots) "All time slots are booked for this date" else "No availability for this date",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                "Time Slots:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Time slots grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(availableTimeSlots) { time ->
                    val isBooked = isTimeSlotBooked(selectedDate, time)
                    val isSelectable = !isBooked
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isSelectable) { 
                                if (isSelectable) selectedTime = time 
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                selectedTime == time -> MaterialTheme.colorScheme.primary
                                isBooked -> MaterialTheme.colorScheme.surfaceVariant
                                else -> MaterialTheme.colorScheme.surface
                            }
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Text(
                            text = time,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = when {
                                selectedTime == time -> MaterialTheme.colorScheme.onPrimary
                                isBooked -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Confirmation button
            Button(
                onClick = {
                    if (selectedTime.isNotEmpty()) {
                        navController.navigate(
                            "book_appointment/$doctorId/$patientId/$patientName/${selectedDate.toString()}/$selectedTime"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = selectedTime.isNotEmpty() && !isTimeSlotBooked(selectedDate, selectedTime)
            ) {
                Text(
                    "Confirm Appointment",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
