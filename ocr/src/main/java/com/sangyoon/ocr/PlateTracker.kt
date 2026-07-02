package com.sangyoon.ocr

/**
 * 프레임 간 번호판 후보 추적기 (순수 Kotlin — JVM 단위 테스트 가능).
 *
 * 기존의 "연속 N프레임" 방식은 모션 블러 등으로 한 프레임만 놓쳐도 카운터가 리셋되고,
 * 한 프레임에 번호판이 여러 개 보이면 후보가 번갈아 선택되며 서로를 리셋시키는 문제가 있었다.
 * 이 추적기는 번호판별로 독립 추적하며 프레임 누락을 허용한다:
 *
 * - 동일(유사) 번호판이 [trackExpiryMs] 이내에 [requiredSightings]회 관측되면 확정한다.
 * - 1글자 오차 변형은 같은 번호판으로 묶어 추적하고, 확정 시 가장 많이 관측된 변형을 반환한다.
 * - 확정된 번호판은 [cooldownMs] 동안 재확정이 억제되고, 화면에 계속 보이면
 *   [refreshCooldowns]로 쿨다운이 연장되어 같은 차량이 떠나기 전까지 반복 확정되지 않는다.
 */
class PlateTracker(
    private val requiredSightings: Int = 2,
    private val trackExpiryMs: Long = 1500L,
    private val cooldownMs: Long = 3000L,
) {
    private class Track {
        var lastSeenMs = 0L
        var sightings = 0
        val variantCounts = mutableMapOf<String, Int>()

        /** 가장 많이 관측된 변형 — OCR 오차가 섞여도 다수결로 올바른 문자열을 선택한다. */
        fun canonical(): String = variantCounts.maxByOrNull { it.value }?.key.orEmpty()
    }

    private val tracks = mutableMapOf<String, Track>()
    private val cooldowns = mutableMapOf<String, Long>()

    /** 현재 쿨다운 중인 번호판 목록 — 필터의 ignoredPlates로 전달한다. */
    fun activeCooldownPlates(nowMs: Long): Set<String> {
        cooldowns.entries.removeAll { nowMs - it.value >= cooldownMs }
        return cooldowns.keys.toSet()
    }

    /** 쿨다운 중인 번호판이 이번 프레임에도 보였다면 쿨다운을 연장한다. */
    fun refreshCooldowns(plates: Collection<String>, nowMs: Long) {
        for (plate in plates) {
            if (cooldowns.containsKey(plate)) cooldowns[plate] = nowMs
        }
    }

    /**
     * 프레임의 번호판 후보들(신뢰도 순)을 반영한다.
     *
     * @return 이번 프레임에서 확정된 번호판. 없으면 null.
     *   여러 번호판이 동시에 임계치를 넘어도 한 프레임에 하나만 확정하고,
     *   나머지는 추적 상태를 유지해 다음 관측 시 확정된다.
     */
    fun onFrame(plates: List<String>, nowMs: Long): String? {
        tracks.entries.removeAll { nowMs - it.value.lastSeenMs > trackExpiryMs }

        var confirmed: String? = null
        val countedThisFrame = mutableListOf<String>()
        for (plate in plates) {
            // 같은 프레임의 유사 중복 후보(동일 번호판의 변형)는 1회로만 계산
            if (countedThisFrame.any { KoreanPlateFilter.isSimilarPlate(it, plate) }) continue
            countedThisFrame.add(plate)

            val key = tracks.keys.firstOrNull { KoreanPlateFilter.isSimilarPlate(it, plate) } ?: plate
            val track = tracks.getOrPut(key) { Track() }
            track.lastSeenMs = nowMs
            track.sightings++
            track.variantCounts.merge(plate, 1, Int::plus)

            if (confirmed == null && track.sightings >= requiredSightings) {
                confirmed = track.canonical()
                tracks.remove(key)
                cooldowns[confirmed] = nowMs
            }
        }
        return confirmed
    }

    /** 추적·쿨다운 상태 초기화. */
    fun reset() {
        tracks.clear()
        cooldowns.clear()
    }
}
