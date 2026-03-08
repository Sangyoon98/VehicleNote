package com.sangyoon.ocr

object KoreanPlateFilter {
    private val OCR_CHAR_REPLACEMENTS = mapOf(
        'O' to '0',
        'o' to '0',
        'I' to '1',
        'l' to '1',
        'Z' to '2',
        'B' to '8'
    )

    private val PLATE_PATTERNS = listOf(
        Regex("^[가-힣]\\d{4}$"),                  // 임1816
        Regex("^\\d{2}[가-힣]\\d{4}$"),            // 02우1138, 12가1234
        Regex("^\\d{3}[가-힣]\\d{4}$"),            // 123거4568
        Regex("^[가-힣]{2}\\d{2}[가-힣]\\d{4}$"),  // 서울12아1234
        Regex("^[가-힣]{2}\\d{3}[가-힣]\\d{4}$")   // 경기123가4567
    )

    /**
     * ML Kit 인식 텍스트에서 한국 번호판 후보를 추출한다.
     * 공백 제거 후 각 줄/토큰을 패턴 매칭, 첫 번째 일치 반환.
     */
    fun extractPlate(rawText: String): String? {
        // 공백 없이 붙어있는 경우도 고려하여 공백 제거 버전도 시도
        val candidates = buildList {
            // 원문 분리
            rawText.lines().forEach { line ->
                line.trim().let { if (it.isNotEmpty()) add(it) }
                line.split(" ", "\t").forEach { token ->
                    token.trim().let { if (it.isNotEmpty()) add(it) }
                }
                normalizePlateCandidate(line)?.let(::add)
            }
            // 공백을 완전히 제거한 전체 텍스트도 후보로
            add(rawText.replace("\\s".toRegex(), ""))
            normalizePlateCandidate(rawText)?.let(::add)
        }

        return candidates.firstOrNull { candidate ->
            PLATE_PATTERNS.any { pattern -> pattern.matches(candidate) }
        }
    }

    private fun normalizePlateCandidate(text: String): String? {
        val normalized = text
            .map { OCR_CHAR_REPLACEMENTS[it] ?: it }
            .joinToString(separator = "")
            .replace("\\s".toRegex(), "")
            .replace("[^0-9가-힣]".toRegex(), "")
            .takeIf { it.isNotEmpty() }
            ?: return null

        return PLATE_PATTERNS.firstNotNullOfOrNull { pattern ->
            pattern.find(normalized)?.value
        }
    }
}
