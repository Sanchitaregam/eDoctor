package com.example.edoctor

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "eDoctorSession", Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveLoginSession(userId: Int, role: String, email: String) {
        sharedPreferences.edit {
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_ROLE, role)
            putString(KEY_USER_EMAIL, email)
            putBoolean(KEY_IS_LOGGED_IN, true)
        }
    }

    fun clearLoginSession() {
        sharedPreferences.edit {
            remove(KEY_USER_ID)
            remove(KEY_USER_ROLE)
            remove(KEY_USER_EMAIL)
            putBoolean(KEY_IS_LOGGED_IN, false)
        }
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    private fun getUserId(): Int {
        return sharedPreferences.getInt(KEY_USER_ID, -1)
    }

    private fun getUserRole(): String {
        return sharedPreferences.getString(KEY_USER_ROLE, "") ?: ""
    }

    fun getLoginDestination(): String {
        val role = getUserRole()
        val userId = getUserId()
        return when (role.lowercase()) {
            "doctor" -> "doctor_profile/$userId"
            "patient" -> "patient_profile/$userId"
            "admin" -> "admin_profile/$userId"
            else -> "welcome"
        }
    }
} 