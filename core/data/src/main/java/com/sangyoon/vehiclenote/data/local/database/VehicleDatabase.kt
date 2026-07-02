package com.sangyoon.vehiclenote.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sangyoon.vehiclenote.data.local.dao.EntryExitRecordDao
import com.sangyoon.vehiclenote.data.local.dao.VehicleDao
import com.sangyoon.vehiclenote.data.local.entity.EntryExitRecordEntity
import com.sangyoon.vehiclenote.data.local.entity.VehicleEntity

/**
 * VehicleNote 앱 Room 데이터베이스. 현재 버전: 4.
 *
 * 포함 엔티티: [VehicleEntity], [EntryExitRecordEntity].
 *
 * 마이그레이션 이력:
 * - v1 → v2 ([MIGRATION_1_2]): vehicles 테이블에 `photoUri`, `customFields` 컬럼 추가.
 * - v2 → v3 ([MIGRATION_2_3]): `entry_exit_records` 테이블 및 인덱스 생성.
 * - v3 → v4 ([MIGRATION_3_4]): 인덱스명을 Room 자동 생성 규칙에 맞게 재생성.
 */
@Database(
    entities = [VehicleEntity::class, EntryExitRecordEntity::class],
    version = 4,
    exportSchema = false
)
abstract class VehicleDatabase : RoomDatabase() {

    /** 차량 테이블 DAO를 반환한다. */
    abstract fun vehicleDao(): VehicleDao

    /** 입출차 기록 테이블 DAO를 반환한다. */
    abstract fun entryExitRecordDao(): EntryExitRecordDao

    companion object {

        /**
         * v1 → v2: `vehicles` 테이블에 사진 경로와 커스텀 필드 컬럼을 추가한다.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vehicles ADD COLUMN photoUri TEXT")
                db.execSQL("ALTER TABLE vehicles ADD COLUMN customFields TEXT NOT NULL DEFAULT '[]'")
            }
        }

        /**
         * v2 → v3: `entry_exit_records` 테이블과 검색용 인덱스를 생성한다.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
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
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_entry_exit_records_licensePlate ON entry_exit_records(licensePlate)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_entry_exit_records_vehicleId ON entry_exit_records(vehicleId)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_entry_exit_records_timestamp ON entry_exit_records(timestamp)"
                )
            }
        }

        /**
         * v3 → v4: 구 인덱스명(idx_eer_*)을 Room 규칙 인덱스명으로 교체한다.
         *
         * Room이 스키마 검증 시 인덱스명 불일치로 오류를 발생시키는 문제를 해결한다.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP INDEX IF EXISTS idx_eer_plate")
                db.execSQL("DROP INDEX IF EXISTS idx_eer_vehicleId")
                db.execSQL("DROP INDEX IF EXISTS idx_eer_timestamp")

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_entry_exit_records_licensePlate ON entry_exit_records(licensePlate)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_entry_exit_records_vehicleId ON entry_exit_records(vehicleId)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_entry_exit_records_timestamp ON entry_exit_records(timestamp)"
                )
            }
        }
    }
}
