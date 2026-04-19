package com.sangyoon.ocr

import android.graphics.Rect

/**
 * 한국 차량번호판 텍스트 필터링 및 유사도 비교 유틸리티.
 *
 * Google ML Kit OCR 결과에서 번호판 패턴을 추출하며, 다음 전략을 사용한다:
 * 1. 토큰 단위 정확 매칭 (exact match)
 * 2. OCR 오류 보정 후 부분 매칭 (find)
 * 3. 인접 블록 텍스트 병합 후 재시도 (가까이서 촬영 시 블록 분리 케이스 대응)
 */
object KoreanPlateFilter {

    // ML Kit 한국어 OCR에서 흔한 오인식 문자 보정
    private val OCR_CHAR_REPLACEMENTS = mapOf(
        'O' to '0', 'o' to '0', 'Q' to '0', 'D' to '0',
        'I' to '1', 'l' to '1', '|' to '1', '!' to '1',
        'Z' to '2', 'z' to '2',
        'A' to '4',
        'S' to '5', 's' to '5',
        'G' to '6',
        'T' to '7',
        'B' to '8',
        'g' to '9', 'q' to '9',
    )

    /**
     * 번호판 패턴 (긴 패턴 우선 — 더 구체적인 매칭 선호).
     * find()로 사용하므로 앵커(^$) 없이 정의한다.
     */
    private val PLATE_PATTERNS = listOf(
        Regex("[가-힣]{2}\\d{3}[가-힣]\\d{4}"),  // 경기123가4567
        Regex("[가-힣]{2}\\d{2}[가-힣]\\d{4}"),   // 서울12아1234
        Regex("\\d{3}[가-힣]\\d{4}"),             // 123거4568
        Regex("\\d{2}[가-힣]\\d{4}"),             // 02우1138
        Regex("[가-힣]\\d{4}"),                    // 임1816
    )

    /** 토큰 단위 exact match용 앵커 패턴 */
    private val EXACT_PLATE_PATTERNS = PLATE_PATTERNS.map {
        Regex("^${it.pattern}$")
    }

    data class PlateCandidate(
        val plate: String,
        val boundingBox: Rect?,
        val patternIndex: Int,  // 낮을수록 더 구체적인 패턴
    )

    /**
     * ML Kit [visionText]에서 모든 번호판 후보를 찾아 가장 신뢰도 높은 것을 반환한다.
     * 선택 기준: 1) 패턴 구체성 (긴 패턴 우선) → 2) 바운딩 박스 면적 (큰 것 우선)
     */
    fun findPlateBounds(visionText: com.google.mlkit.vision.text.Text): Pair<String, Rect?>? {
        val candidates = mutableListOf<PlateCandidate>()
        val seen = mutableSetOf<String>()

        // 1. 블록/라인 단위 탐색
        for (block in visionText.textBlocks) {
            for ((plate, idx) in extractAllPlates(block.text)) {
                if (plate in seen) continue
                seen.add(plate)
                val matchingLine = block.lines.firstOrNull { line ->
                    extractAllPlates(line.text).any { it.first == plate }
                }
                candidates.add(
                    PlateCandidate(plate, matchingLine?.boundingBox ?: block.boundingBox, idx)
                )
            }

            for (line in block.lines) {
                for ((plate, idx) in extractAllPlates(line.text)) {
                    if (plate in seen) continue
                    seen.add(plate)
                    candidates.add(PlateCandidate(plate, line.boundingBox, idx))
                }
            }
        }

        // 2. 인접 블록 텍스트 병합 — 가까이서 촬영 시 블록이 분리되는 케이스
        val blocks = visionText.textBlocks
        if (blocks.size >= 2) {
            for (i in 0 until blocks.size - 1) {
                val merged = blocks[i].text.replace("\\s".toRegex(), "") +
                        blocks[i + 1].text.replace("\\s".toRegex(), "")
                for ((plate, idx) in extractAllPlates(merged)) {
                    if (plate in seen) continue
                    seen.add(plate)
                    val mergedBox = mergeBounds(blocks[i].boundingBox, blocks[i + 1].boundingBox)
                    candidates.add(PlateCandidate(plate, mergedBox, idx))
                }
            }
        }

        // 3. 폴백: 전체 텍스트에서 추출 (바운딩 박스 없음)
        if (candidates.isEmpty()) {
            for ((plate, idx) in extractAllPlates(visionText.text)) {
                if (plate in seen) continue
                seen.add(plate)
                candidates.add(PlateCandidate(plate, null, idx))
            }
        }

        // 최적 후보 선택: 패턴 구체성 우선, 동률 시 바운딩 박스 면적 큰 것
        return candidates.minByOrNull { candidate ->
            val area = candidate.boundingBox?.let { it.width() * it.height() } ?: 0
            candidate.patternIndex * 10_000_000 - area
        }?.let { it.plate to it.boundingBox }
    }

    /**
     * 텍스트에서 한국 번호판 후보를 모두 추출한다.
     * 반환: List<Pair<번호판 문자열, 패턴 인덱스>>
     */
    fun extractAllPlates(rawText: String): List<Pair<String, Int>> {
        val results = mutableListOf<Pair<String, Int>>()
        val seen = mutableSetOf<String>()

        // 1단계: 토큰 단위 exact match (원본 텍스트)
        val tokens = buildList {
            rawText.lines().forEach { line ->
                line.trim().let { if (it.isNotEmpty()) add(it) }
                line.split(" ", "\t", "-", "·", ".", ":", ")", "(", "]", "[").forEach { token ->
                    token.trim().let { if (it.isNotEmpty()) add(it) }
                }
            }
            add(rawText.replace("\\s".toRegex(), ""))
        }

        for (token in tokens) {
            for ((idx, pattern) in EXACT_PLATE_PATTERNS.withIndex()) {
                if (pattern.matches(token) && token !in seen) {
                    results.add(token to idx)
                    seen.add(token)
                }
            }
        }

        // 2단계: 정규화 후 부분 매칭 (find) — OCR 오류 보정 + 연결된 텍스트에서 추출
        val normalized = normalize(rawText)
        for ((idx, pattern) in PLATE_PATTERNS.withIndex()) {
            var startIndex = 0
            while (startIndex < normalized.length) {
                val match = pattern.find(normalized, startIndex) ?: break
                val plate = match.value
                if (plate !in seen) {
                    results.add(plate to idx)
                    seen.add(plate)
                }
                startIndex = match.range.last + 1
            }
        }

        return results
    }

    /**
     * 텍스트에서 첫 번째 번호판을 추출한다 (하위 호환).
     */
    fun extractPlate(rawText: String): String? {
        return extractAllPlates(rawText).minByOrNull { it.second }?.first
    }

    /**
     * 두 번호판 문자열이 유사한지 판별 (OCR 오차 허용).
     * 길이가 같고 1글자 이내 차이면 동일 번호판으로 간주한다.
     */
    fun isSimilarPlate(a: String, b: String): Boolean {
        if (a == b) return true
        if (a.length != b.length) return false
        return a.zip(b).count { (c1, c2) -> c1 != c2 } <= 1
    }

    private fun normalize(text: String): String {
        return text
            .map { OCR_CHAR_REPLACEMENTS[it] ?: it }
            .joinToString("")
            .replace("\\s".toRegex(), "")
            .replace("[^0-9가-힣]".toRegex(), "")
    }

    private fun mergeBounds(a: Rect?, b: Rect?): Rect? {
        if (a == null) return b
        if (b == null) return a
        return Rect(
            minOf(a.left, b.left),
            minOf(a.top, b.top),
            maxOf(a.right, b.right),
            maxOf(a.bottom, b.bottom),
        )
    }
}
