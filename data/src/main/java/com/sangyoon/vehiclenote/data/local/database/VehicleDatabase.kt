package com.sangyoon.vehiclenote.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sangyoon.vehiclenote.data.local.dao.VehicleDao
import com.sangyoon.vehiclenote.data.local.entity.VehicleEntity

@Database(
    entities = [VehicleEntity::class],
    version = 2,
    exportSchema = false
)
abstract class VehicleDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE vehicles ADD COLUMN photoUri TEXT")
                database.execSQL("ALTER TABLE vehicles ADD COLUMN customFields TEXT NOT NULL DEFAULT '[]'")
            }
        }
    }
}
