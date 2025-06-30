package com.example.edoctor.ui.common

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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DatePicker(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    label: String = "Date of Birth"
) {
    var showDialog by remember { mutableStateOf(false) }
    var currentMonth by remember { mutableStateOf(YearMonth.now().minusYears(18)) } // Default to 18 years ago
    
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    OutlinedTextField(
        value = selectedDate?.format(dateFormatter) ?: "",
        onValueChange = { },
        label = { Text(label) },
        readOnly = true,
        singleLine = true,
        trailingIcon = {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = "Select Date",
                modifier = Modifier.clickable { showDialog = true }
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
    )
    
    if (showDialog) {
        DatePickerDialog(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                onDateSelected(date)
                showDialog = false
            },
            onDismiss = { showDialog = false },
            currentMonth = currentMonth,
            onMonthChanged = { currentMonth = it }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DatePickerDialog(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    currentMonth: YearMonth,
    onMonthChanged: (YearMonth) -> Unit
) {
    var showYearPicker by remember { mutableStateOf(false) }
    val currentYear = currentMonth.year
    val years = (currentYear - 100..currentYear).toList().reversed() // Last 100 years
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Select Date of Birth",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Month and Year navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { 
                            onMonthChanged(currentMonth.minusMonths(1))
                        }
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, "Previous Month")
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM")),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Year picker button
                        OutlinedButton(
                            onClick = { showYearPicker = true },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = currentYear.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = { 
                            onMonthChanged(currentMonth.plusMonths(1))
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
                    items(getCalendarDays(currentMonth)) { date ->
                        if (date == null) {
                            // Empty cell
                            Box(modifier = Modifier.size(40.dp))
                        } else {
                            val isSelected = date == selectedDate
                            val isToday = date == LocalDate.now()
                            val isFuture = date.isAfter(LocalDate.now())
                            val enabled = !isFuture // Can't select future dates for DOB
                            
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            enabled -> MaterialTheme.colorScheme.primaryContainer
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = if (isToday) 2.dp else 0.dp,
                                        color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable(enabled = enabled) {
                                        onDateSelected(date)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        enabled -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
    
    // Year picker dialog
    if (showYearPicker) {
        Dialog(onDismissRequest = { showYearPicker = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.99f)
                    .height(300.dp)
                    .padding(10.dp),
                shape = RoundedCornerShape(10.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Select Year",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(years) { year ->
                            OutlinedButton(
                                onClick = {
                                    onMonthChanged(YearMonth.of(year, currentMonth.month))
                                    showYearPicker = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (year == currentYear) 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(
                                    text = year.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = if (year == currentYear) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showYearPicker = false }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getCalendarDays(yearMonth: YearMonth): List<LocalDate?> {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
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