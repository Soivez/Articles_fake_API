package com.example.fakeapi

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Element::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun elementDao() : ElementDao
}