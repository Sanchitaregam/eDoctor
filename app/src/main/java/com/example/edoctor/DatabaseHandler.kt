package com.example.edoctor

import android.content.Context
import androidx.room.Room

object DatabaseHandler {
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            ).build()
        }
        return INSTANCE!!
    }

    suspend fun authenticateUser(context: Context, email: String, password: String): UserEntity? {
        return getDatabase(context).userDao().login(email, password)
    }
}