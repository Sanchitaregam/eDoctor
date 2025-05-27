package com.example.edoctor

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserEntity::class], version = 2) // ⬆️ Incremented version from 1 to 2
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
