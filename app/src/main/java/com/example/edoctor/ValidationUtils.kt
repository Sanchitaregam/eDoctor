
package com.example.edoctor

import androidx.compose.ui.graphics.Color

fun validatePasswordStrength(password: String): Pair<String, Color> {
    return when {
        password.length < 6 -> "Weak Password" to Color.Red
        password.length < 10 -> "Medium Strength Password" to Color.Yellow
        else -> "Strong Password" to Color(0xFF2E7D32) // Dark Green
    }
}