package com.sangyoon.vehiclenote.domain.model

enum class DataRetentionPeriod(val days: Int?) {
    ONE_DAY(1),
    ONE_WEEK(7),
    ONE_MONTH(30),
    UNLIMITED(null)
}
