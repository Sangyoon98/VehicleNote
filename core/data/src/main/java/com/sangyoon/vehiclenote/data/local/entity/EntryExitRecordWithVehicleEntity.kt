package com.sangyoon.vehiclenote.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * `entry_exit_records LEFT JOIN vehicles` 결과용 Room @Relation 클래스.
 *
 * [EntryExitRecordDao.getByIdWithVehicle]에서 `@Transaction`과 함께 사용된다.
 * [vehicle]은 차량이 삭제되었거나 미등록 번호판이면 null이 된다.
 *
 * @property record 입출차 기록 엔티티.
 * @property vehicle 연결된 차량 엔티티. null이면 미등록 또는 삭제된 차량.
 */
data class EntryExitRecordWithVehicleEntity(
    @Embedded val record: EntryExitRecordEntity,
    @Relation(
        parentColumn = "vehicleId",
        entityColumn = "id"
    )
    val vehicle: VehicleEntity?
)
