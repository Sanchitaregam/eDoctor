package com.example.edoctor

import android.widget.CalendarView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.util.*



@Composable
fun CalendarScreen(onDateSelected: (LocalDate) -> Unit) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Select Appointment Date", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(16.dp))

        AndroidView(
            factory = { context ->
                CalendarView(context).apply {
                    minDate = System.currentTimeMillis()
                    setOnDateChangeListener { _, year, month, dayOfMonth ->
                        selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                        onDateSelected(selectedDate)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Selected: $selectedDate", fontSize = 16.sp)
    }
}
