package com.sangyoon.vehiclenote.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 입출차 기록 Room DB 엔티티. 테이블명: `entry_exit_records`.
 *
 * [vehicleId]는 [VehicleEntity]에 대한 외래키이며, 차량이 삭제되면 `SET NULL`된다.
 * [type]은 [com.sangyoon.vehiclenote.domain.model.RecordType.name] 문자열("ENTRY"/"EXIT")로 저장된다.
 *
 * @property id 자동 생성 PK.
 * @property licensePlate 차량번호.
 * @property type 기록 유형 문자열 ("ENTRY" 또는 "EXIT").
 * @property timestamp 기록 시각 (epoch ms).
 * @property vehicleId 연결된 차량 id (외래키, 차량 삭제 시 null).
 */
@Entity(
    tableName = "entry_exit_records",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["licensePlate"]),
        Index(value = ["vehicleId"]),
        Index(value = ["timestamp"])
    ]
)
data class EntryExitRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val licensePlate: String,
    val type: String,           // RecordType.name
    val timestamp: Long,
    val vehicleId: Long?
)
