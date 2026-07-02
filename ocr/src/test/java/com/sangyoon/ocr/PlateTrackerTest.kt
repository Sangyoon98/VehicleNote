package com.sangyoon.ocr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlateTrackerTest {

    @Test
    fun `같은 번호판이 2회 관측되면 확정된다`() {
        val tracker = PlateTracker()
        assertNull(tracker.onFrame(listOf("12가3456"), 0))
        assertEquals("12가3456", tracker.onFrame(listOf("12가3456"), 100))
    }

    @Test
    fun `중간 프레임 누락을 허용한다`() {
        // 모션 블러 등으로 한 프레임 놓쳐도 추적 상태를 유지해야 한다
        val tracker = PlateTracker()
        assertNull(tracker.onFrame(listOf("12가3456"), 0))
        assertNull(tracker.onFrame(emptyList(), 300))
        assertEquals("12가3456", tracker.onFrame(listOf("12가3456"), 600))
    }

    @Test
    fun `추적 만료 시간이 지나면 처음부터 다시 센다`() {
        val tracker = PlateTracker(trackExpiryMs = 1500L)
        assertNull(tracker.onFrame(listOf("12가3456"), 0))
        assertNull(tracker.onFrame(listOf("12가3456"), 2000))
    }

    @Test
    fun `한 프레임의 여러 번호판을 각각 추적해 순차 확정한다`() {
        val tracker = PlateTracker()
        assertNull(tracker.onFrame(listOf("12가3456", "34나7890"), 0))
        // 두 번호판 모두 임계치 도달 — 한 프레임에 하나만 확정
        assertEquals("12가3456", tracker.onFrame(listOf("12가3456", "34나7890"), 100))
        // 나머지는 다음 관측 시 바로 확정
        assertEquals("34나7890", tracker.onFrame(listOf("34나7890"), 200))
    }

    @Test
    fun `1글자 오차 변형은 같은 번호판으로 추적하고 다수 변형을 반환한다`() {
        val tracker = PlateTracker(requiredSightings = 3)
        assertNull(tracker.onFrame(listOf("12가3456"), 0))
        assertNull(tracker.onFrame(listOf("12가3450"), 100)) // OCR 오차 변형
        assertEquals("12가3456", tracker.onFrame(listOf("12가3456"), 200))
    }

    @Test
    fun `같은 프레임의 유사 중복 후보는 1회로 계산한다`() {
        val tracker = PlateTracker()
        // 같은 번호판의 변형 2개가 한 프레임에 있어도 즉시 확정되면 안 된다
        assertNull(tracker.onFrame(listOf("12가3456", "12가3450"), 0))
    }

    @Test
    fun `확정된 번호판은 쿨다운에 등록되고 만료 후 해제된다`() {
        val tracker = PlateTracker(cooldownMs = 3000L)
        tracker.onFrame(listOf("12가3456"), 0)
        tracker.onFrame(listOf("12가3456"), 100)
        assertTrue(tracker.activeCooldownPlates(200).contains("12가3456"))
        assertTrue(tracker.activeCooldownPlates(3200).isEmpty())
    }

    @Test
    fun `화면에 계속 보이는 번호판은 쿨다운이 연장된다`() {
        val tracker = PlateTracker(cooldownMs = 3000L)
        tracker.onFrame(listOf("12가3456"), 0)
        tracker.onFrame(listOf("12가3456"), 100)
        // 2900ms 시점에 아직 화면에 보임 → 쿨다운 갱신
        tracker.refreshCooldowns(listOf("12가3456"), 2900)
        assertTrue(tracker.activeCooldownPlates(5000).contains("12가3456"))
        // 마지막 관측(2900) 기준 3000ms 경과 후 해제
        assertTrue(tracker.activeCooldownPlates(6000).isEmpty())
    }

    @Test
    fun `reset은 추적과 쿨다운 상태를 모두 초기화한다`() {
        val tracker = PlateTracker()
        tracker.onFrame(listOf("12가3456"), 0)
        tracker.onFrame(listOf("12가3456"), 100)
        tracker.reset()
        assertTrue(tracker.activeCooldownPlates(200).isEmpty())
        assertNull(tracker.onFrame(listOf("12가3456"), 300))
    }
}
