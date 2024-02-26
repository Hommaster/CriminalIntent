package com.example.criminalintent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.criminalintent.Crime

@Database(entities = [Crime::class], version = 3, exportSchema = false)
@TypeConverters(CrimeTypeConverter::class)
abstract class CrimeDatabase : RoomDatabase() {
    abstract fun crimeDao(): CrimeDAO
}
val migration_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE Crime ADD COLUMN phone TEXT NOT NULL DEFAULT ''"
        )
    }
}