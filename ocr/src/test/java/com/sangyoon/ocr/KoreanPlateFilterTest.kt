package com.sangyoon.ocr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KoreanPlateFilterTest {

    @Test
    fun `두 자리 번호판을 추출한다`() {
        val rawText = "02우1138"

        assertEquals("02우1138", KoreanPlateFilter.extractPlate(rawText))
    }

    @Test
    fun `임시 연구용 번호판을 추출한다`() {
        val rawText = "임 1816"

        assertEquals("임1816", KoreanPlateFilter.extractPlate(rawText))
    }

    @Test
    fun `세 자리 번호판을 추출한다`() {
        val rawText = "123거4568"

        assertEquals("123거4568", KoreanPlateFilter.extractPlate(rawText))
    }

    @Test
    fun `지역명이 포함된 번호판을 추출한다`() {
        val rawText = "서울 12아 1234"

        assertEquals("서울12아1234", KoreanPlateFilter.extractPlate(rawText))
    }

    @Test
    fun `문장에 포함된 번호판도 추출한다`() {
        val rawText = "[차량번호] 서울12가1234 입차"

        assertEquals("서울12가1234", KoreanPlateFilter.extractPlate(rawText))
    }

    @Test
    fun `번호판이 없으면 null을 반환한다`() {
        assertNull(KoreanPlateFilter.extractPlate("차량 없음"))
    }
}
