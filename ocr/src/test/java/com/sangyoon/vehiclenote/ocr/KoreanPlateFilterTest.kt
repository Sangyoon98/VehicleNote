package com.sangyoon.vehiclenote.ocr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KoreanPlateFilterTest {

    @Test
    fun `두 자리 번호판을 추출한다`() {
        assertEquals("02우1138", KoreanPlateFilter.extractPlate("02우1138"))
    }

    @Test
    fun `임시 연구용 번호판을 추출한다`() {
        assertEquals("임1816", KoreanPlateFilter.extractPlate("임 1816"))
    }

    @Test
    fun `세 자리 번호판을 추출한다`() {
        assertEquals("123거4568", KoreanPlateFilter.extractPlate("123거4568"))
    }

    @Test
    fun `지역명이 포함된 번호판을 추출한다`() {
        assertEquals("서울12아1234", KoreanPlateFilter.extractPlate("서울 12아 1234"))
    }

    @Test
    fun `문장에 포함된 번호판도 추출한다`() {
        assertEquals("서울12가1234", KoreanPlateFilter.extractPlate("[차량번호] 서울12가1234 입차"))
    }

    @Test
    fun `번호판이 없으면 null을 반환한다`() {
        assertNull(KoreanPlateFilter.extractPlate("차량 없음"))
    }

    // --- 새 테스트 케이스 ---

    @Test
    fun `OCR 오인식 문자를 보정하여 추출한다`() {
        // O → 0, I → 1
        assertEquals("123가4567", KoreanPlateFilter.extractPlate("I23가456T"))
    }

    @Test
    fun `특수문자가 섞인 텍스트에서 추출한다`() {
        assertEquals("123가4567", KoreanPlateFilter.extractPlate("123-가-4567"))
    }

    @Test
    fun `공백 없이 붙어있는 긴 텍스트에서 추출한다`() {
        assertEquals("123가4567", KoreanPlateFilter.extractPlate("차량번호123가4567입차"))
    }

    @Test
    fun `번호판 라벨 없이 다른 글자와 붙은 텍스트는 제외한다`() {
        assertNull(KoreanPlateFilter.extractPlate("방문차량123가4567입차"))
    }

    @Test
    fun `대표 번호판 형태를 추출한다`() {
        assertEquals("12가1234", KoreanPlateFilter.extractPlate("12가1234"))
        assertEquals("123가1234", KoreanPlateFilter.extractPlate("123가1234"))
        assertEquals("서울12가1234", KoreanPlateFilter.extractPlate("서울12가1234"))
    }

    @Test
    fun `여러 번호판이 있으면 가장 구체적인 패턴을 반환한다`() {
        // "서울12가1234"가 "가1234"보다 더 구체적인 패턴
        val plates = KoreanPlateFilter.extractAllPlates("서울12가1234 02나5678")
        assertTrue(plates.any { it.first == "서울12가1234" })
        assertTrue(plates.any { it.first == "02나5678" })
    }

    @Test
    fun `유사 번호판 비교가 1글자 오차를 허용한다`() {
        assertTrue(KoreanPlateFilter.isSimilarPlate("123가4567", "123가4567"))
        assertTrue(KoreanPlateFilter.isSimilarPlate("123가4567", "123가4568"))  // 1글자 차이
        assertFalse(KoreanPlateFilter.isSimilarPlate("123가4567", "123가4578")) // 2글자 차이
        assertFalse(KoreanPlateFilter.isSimilarPlate("123가4567", "12가4567"))  // 길이 다름
    }

    @Test
    fun `줄바꿈으로 분리된 번호판을 추출한다`() {
        assertEquals("123가4567", KoreanPlateFilter.extractPlate("123가\n4567"))
    }

    @Test
    fun `일련번호가 0으로 시작하면 번호판이 아니다`() {
        // 일련번호는 1000~9999만 발급됨 — 전화번호·잘못된 오프셋 매칭 오탐 차단
        assertNull(KoreanPlateFilter.extractPlate("12가0345"))
    }

    @Test
    fun `오인식 보정이 주변 문자를 번호판에 오염시키지 않는다`() {
        // 'A'를 무조건 4로 바꾸면 "412가3456"이라는 잘못된 번호판이 만들어진다.
        // 원본 매칭("12가3456")이 보정 매칭보다 우선해야 한다.
        assertEquals("12가3456", KoreanPlateFilter.extractPlate("A12가3456"))
    }

    @Test
    fun `보정 없이 매칭되지 않을 때만 오인식 보정을 적용한다`() {
        assertEquals("58도2810", KoreanPlateFilter.extractPlate("5B도2810"))
    }
}
