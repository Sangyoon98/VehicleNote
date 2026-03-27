package com.sangyoon.vehiclenote.data.local.entity

/**
 * 검색 쿼리의 플랫 결과를 받기 위한 튜플.
 * Room @Relation의 WHERE 무시 문제를 우회한다.
 */
data class SearchResultTuple(
    val id: Long,
    val licensePlate: String,
    val type: String,
    val timestamp: Long,
    val vehicleId: Long?,
    val ownerName: String?
)
