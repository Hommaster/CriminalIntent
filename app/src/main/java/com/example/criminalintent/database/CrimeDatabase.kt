package com.example.criminalintent.database

import androidx.room.Database
import androidx.room.DatabaseConfiguration
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.criminalintent.Crime

@Database(entities = [Crime::class], version = 1, exportSchema = false)
@TypeConverters(CrimeTypeConverter::class)
abstract class CrimeDatabase : RoomDatabase() {

    override fun init(configuration: DatabaseConfiguration) {
        super.init(configuration)
    }

    abstract fun crimeDao(): CrimeDAO
}