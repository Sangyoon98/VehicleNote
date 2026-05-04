package com.sangyoon.ocr

import android.graphics.Rect
import com.google.mlkit.vision.text.Text

/**
 * 한국 차량번호판 텍스트 필터링 및 유사도 비교 유틸리티.
 *
 * Google ML Kit OCR 결과에서 번호판 패턴을 추출하며, 다음 전략을 사용한다:
 * 1. 한국 번호판 규격에 맞는 후보만 추출
 * 2. OCR 오류 보정 후 독립된 후보와 문장 속 후보를 신뢰도별로 점수화
 * 3. 여러 번호판이 보이면 이전 프레임 후보 또는 화면 중앙 후보를 우선 선택
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

    private val REGION_PREFIX = "(서울|부산|대구|인천|광주|대전|울산|세종|경기|강원|충북|충남|전북|전남|경북|경남|제주)"
    private const val PURPOSE_SYLLABLES = "[가나다라마거너더러머버서어저고노도로모보소오조구누두루무부수우주아바사자허하호배]"

    /**
     * 번호판 패턴 (긴 패턴 우선 — 더 구체적인 매칭 선호).
     * find()로 사용하므로 앵커(^$) 없이 정의한다.
     */
    private val PLATE_PATTERNS = listOf(
        Regex("$REGION_PREFIX\\d{3}$PURPOSE_SYLLABLES\\d{4}"), // 경기123가4567
        Regex("$REGION_PREFIX\\d{2}$PURPOSE_SYLLABLES\\d{4}"),  // 서울12가1234
        Regex("\\d{3}$PURPOSE_SYLLABLES\\d{4}"),                // 123가1234
        Regex("\\d{2}$PURPOSE_SYLLABLES\\d{4}"),                // 12가1234
        Regex("임\\d{4}"),                                      // 임1816
    )

    data class PlateCandidate(
        val plate: String,
        val boundingBox: Rect?,
        val patternIndex: Int,  // 낮을수록 더 구체적인 패턴
        val qualityRank: Int,
        val sourceOrder: Int,
    )

    /**
     * ML Kit [visionText]에서 모든 번호판 후보를 찾아 가장 신뢰도 높은 것을 반환한다.
     *
     * @param preferredPlate 직전 프레임에서 추적 중인 번호판. 여러 후보가 동시에 보이면 우선 유지한다.
     * @param ignoredPlates 쿨다운 등으로 이번 프레임에서 제외할 번호판 목록.
     * @return 선택된 번호판과 바운딩 박스. 번호판답지 않은 혼합 텍스트만 있으면 null.
     */
    fun findPlateBounds(
        visionText: Text,
        preferredPlate: String? = null,
        ignoredPlates: Set<String> = emptySet(),
    ): Pair<String, Rect?>? {
        val candidates = mutableListOf<PlateCandidate>()
        val seen = mutableSetOf<String>()
        val frameBounds = mergeAllBounds(visionText.textBlocks.mapNotNull { it.boundingBox })

        // 1. 블록/라인 단위 탐색
        for ((blockIndex, block) in visionText.textBlocks.withIndex()) {
            for ((lineIndex, line) in block.lines.withIndex()) {
                addCandidates(
                    target = candidates,
                    seen = seen,
                    matches = extractPlateMatches(line.text),
                    boundingBox = line.boundingBox,
                    sourceOrder = blockIndex * 100 + lineIndex,
                )

                val mergedElements = line.elements.joinToString(separator = "") { it.text }
                if (mergedElements != line.text.replace("\\s".toRegex(), "")) {
                    addCandidates(
                        target = candidates,
                        seen = seen,
                        matches = extractPlateMatches(mergedElements),
                        boundingBox = line.boundingBox,
                        sourceOrder = blockIndex * 100 + lineIndex,
                    )
                }
            }

            addCandidates(
                target = candidates,
                seen = seen,
                matches = extractPlateMatches(block.text),
                boundingBox = block.boundingBox,
                sourceOrder = blockIndex * 100,
            )
        }

        // 2. 인접 블록 텍스트 병합 — 가까이서 촬영 시 블록이 분리되는 케이스
        val blocks = visionText.textBlocks
        if (blocks.size >= 2) {
            for (i in 0 until blocks.size - 1) {
                val merged = blocks[i].text.replace("\\s".toRegex(), "") +
                        blocks[i + 1].text.replace("\\s".toRegex(), "")
                addCandidates(
                    target = candidates,
                    seen = seen,
                    matches = extractPlateMatches(merged),
                    boundingBox = mergeBounds(blocks[i].boundingBox, blocks[i + 1].boundingBox),
                    sourceOrder = i * 100 + 50,
                )
            }
        }

        // 3. 폴백: 전체 텍스트에서 추출 (바운딩 박스 없음)
        if (candidates.isEmpty()) {
            addCandidates(
                target = candidates,
                seen = seen,
                matches = extractPlateMatches(visionText.text),
                boundingBox = null,
                sourceOrder = Int.MAX_VALUE / 2,
            )
        }

        val activeCandidates = candidates.filterNot { candidate ->
            ignoredPlates.any { ignoredPlate -> isSimilarPlate(ignoredPlate, candidate.plate) }
        }

        val best = activeCandidates.minWithOrNull(
            compareBy<PlateCandidate>(
                { candidate -> if (preferredPlate != null && isSimilarPlate(preferredPlate, candidate.plate)) 0 else 1 },
                { it.qualityRank },
                { centralityRank(it.boundingBox, frameBounds) },
                { it.patternIndex },
                { it.sourceOrder },
            )
        )
        return best?.let { it.plate to it.boundingBox }
    }

    /**
     * 텍스트에서 한국 번호판 후보를 모두 추출한다.
     * 반환: List<Pair<번호판 문자열, 패턴 인덱스>>
     */
    fun extractAllPlates(rawText: String): List<Pair<String, Int>> {
        return extractPlateMatches(rawText)
            .distinctBy { it.plate }
            .map { it.plate to it.patternIndex }
    }

    /**
     * 텍스트에서 첫 번째 번호판을 추출한다 (하위 호환).
     */
    fun extractPlate(rawText: String): String? {
        return extractPlateMatches(rawText).minWithOrNull(
            compareBy<PlateMatch>({ it.qualityRank }, { it.patternIndex }, { it.matchStart })
        )?.plate
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

    private data class PlateMatch(
        val plate: String,
        val patternIndex: Int,
        val qualityRank: Int,
        val matchStart: Int,
    )

    private fun extractPlateMatches(rawText: String): List<PlateMatch> {
        val normalized = normalize(rawText)
        if (normalized.isBlank()) return emptyList()

        val results = mutableListOf<PlateMatch>()
        val seen = mutableSetOf<String>()

        for ((idx, pattern) in PLATE_PATTERNS.withIndex()) {
            var startIndex = 0
            while (startIndex < normalized.length) {
                val match = pattern.find(normalized, startIndex) ?: break
                val qualityRank = qualityRank(normalized, match.range.first, match.range.last)
                if (qualityRank <= QUALITY_LABELED_EMBEDDED && match.value !in seen) {
                    results.add(
                        PlateMatch(
                            plate = match.value,
                            patternIndex = idx,
                            qualityRank = qualityRank,
                            matchStart = match.range.first,
                        )
                    )
                    seen.add(match.value)
                }
                startIndex = match.range.last + 1
            }
        }

        return results
    }

    private fun addCandidates(
        target: MutableList<PlateCandidate>,
        seen: MutableSet<String>,
        matches: List<PlateMatch>,
        boundingBox: Rect?,
        sourceOrder: Int,
    ) {
        for (match in matches) {
            if (match.plate in seen) continue
            seen.add(match.plate)
            target.add(
                PlateCandidate(
                    plate = match.plate,
                    boundingBox = boundingBox,
                    patternIndex = match.patternIndex,
                    qualityRank = match.qualityRank,
                    sourceOrder = sourceOrder,
                )
            )
        }
    }

    private fun qualityRank(text: String, start: Int, end: Int): Int {
        val before = text.getOrNull(start - 1)
        val after = text.getOrNull(end + 1)

        return when {
            before == null && after == null -> QUALITY_EXACT
            hasAdjacentPlateBefore(text, start) || hasAdjacentPlateAfter(text, end) -> QUALITY_SEPARATED
            hasPlateLabelBefore(text, start) -> QUALITY_LABELED_EMBEDDED
            else -> QUALITY_MIXED
        }
    }

    private fun hasPlateLabelBefore(text: String, start: Int): Boolean {
        val prefix = text.take(start).takeLast(5)
        return PLATE_LABELS.any { prefix.endsWith(it) }
    }

    private fun hasAdjacentPlateBefore(text: String, start: Int): Boolean {
        val prefix = text.take(start)
        return PLATE_PATTERNS.any { Regex("${it.pattern}$").containsMatchIn(prefix) }
    }

    private fun hasAdjacentPlateAfter(text: String, end: Int): Boolean {
        val suffix = text.drop(end + 1)
        return PLATE_PATTERNS.any { Regex("^${it.pattern}").containsMatchIn(suffix) }
    }

    private fun centralityRank(bounds: Rect?, frameBounds: Rect?): Int {
        if (bounds == null || frameBounds == null) return Int.MAX_VALUE / 2
        val frameCenterX = frameBounds.centerX()
        val frameCenterY = frameBounds.centerY()
        val dx = bounds.centerX() - frameCenterX
        val dy = bounds.centerY() - frameCenterY
        return dx * dx + dy * dy
    }

    private fun normalize(text: String): String {
        return text
            .map { OCR_CHAR_REPLACEMENTS[it] ?: it }
            .joinToString("")
            .replace("\\s".toRegex(), "")
            .replace("[^0-9가-힣]".toRegex(), "")
    }

    private fun mergeAllBounds(bounds: List<Rect>): Rect? {
        return bounds.reduceOrNull { acc, rect -> mergeBounds(acc, rect) ?: acc }
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

    private const val QUALITY_EXACT = 0
    private const val QUALITY_SEPARATED = 1
    private const val QUALITY_LABELED_EMBEDDED = 2
    private const val QUALITY_MIXED = 3

    private val PLATE_LABELS = listOf("차량번호", "번호판", "차번")
}
