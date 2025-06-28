package com.example.edoctor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

import androidx.compose.material.icons.automirrored.filled.ArrowBack


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthTipsScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { DatabaseProvider.getDatabase(context) }
    val healthTipDao = db.healthTipDao()
    val coroutineScope = rememberCoroutineScope()

    var tips by remember { mutableStateOf(listOf<HealthTipEntity>()) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        tips = healthTipDao.getAllTips()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Tips") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")

                    }
                }
            )

        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    coroutineScope.launch {
                        val tip = HealthTipEntity(title = title, description = description)
                        healthTipDao.insertTip(tip)
                        tips = healthTipDao.getAllTips()
                        title = ""
                        description = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Tip")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(tips) { tip ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(tip.title, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(tip.description)
                        }
                    }
                }
            }
        }
    }
}
