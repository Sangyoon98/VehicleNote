package com.sangyoon.vehiclenote.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sangyoon.vehiclenote.data.local.dao.EntryExitRecordDao
import com.sangyoon.vehiclenote.data.local.dao.VehicleDao
import com.sangyoon.vehiclenote.data.local.entity.EntryExitRecordEntity
import com.sangyoon.vehiclenote.data.local.entity.VehicleEntity

@Database(
    entities = [VehicleEntity::class, EntryExitRecordEntity::class],
    version = 4,
    exportSchema = false
)
abstract class VehicleDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun entryExitRecordDao(): EntryExitRecordDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE vehicles ADD COLUMN photoUri TEXT")
                database.execSQL("ALTER TABLE vehicles ADD COLUMN customFields TEXT NOT NULL DEFAULT '[]'")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS entry_exit_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        licensePlate TEXT NOT NULL,
                        type TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        vehicleId INTEGER,
                        FOREIGN KEY (vehicleId) REFERENCES vehicles(id) ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_entry_exit_records_licensePlate ON entry_exit_records(licensePlate)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_entry_exit_records_vehicleId ON entry_exit_records(vehicleId)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_entry_exit_records_timestamp ON entry_exit_records(timestamp)"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP INDEX IF EXISTS idx_eer_plate")
                database.execSQL("DROP INDEX IF EXISTS idx_eer_vehicleId")
                database.execSQL("DROP INDEX IF EXISTS idx_eer_timestamp")

                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_entry_exit_records_licensePlate ON entry_exit_records(licensePlate)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_entry_exit_records_vehicleId ON entry_exit_records(vehicleId)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_entry_exit_records_timestamp ON entry_exit_records(timestamp)"
                )
            }
        }
    }
}
