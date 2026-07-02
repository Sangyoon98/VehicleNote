package com.sangyoon.vehiclenote.ocr

import android.graphics.Rect
import com.google.mlkit.vision.text.Text

/**
 * 한국 차량번호판 텍스트 필터링 및 유사도 비교 유틸리티.
 *
 * Google ML Kit OCR 결과에서 번호판 패턴을 추출하며, 다음 전략을 사용한다:
 * 1. 한국 번호판 규격에 맞는 후보만 추출 (일련번호는 1000~9999만 발급되므로 첫 자리 0 제외)
 * 2. 원본 텍스트 매칭을 우선하고, OCR 오인식 문자 보정(O→0 등)은 폴백으로만 적용
 *    — 보정이 주변 잡음 문자를 숫자로 바꿔 번호판을 오염시키는 것을 방지
 * 3. 여러 번호판이 보이면 품질(독립 매칭 여부)과 화면 중앙 근접도로 순위를 매겨 모두 반환
 */
internal object KoreanPlateFilter {

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

    /** 일련번호는 1000~9999만 발급되므로 첫 자리 0을 제외해 오프셋 밀림·전화번호 오탐을 차단한다. */
    private const val SERIAL = "[1-9]\\d{3}"

    /**
     * 번호판 패턴 (긴 패턴 우선 — 더 구체적인 매칭 선호).
     * find()로 사용하므로 앵커(^$) 없이 정의한다.
     */
    private val PLATE_PATTERNS = listOf(
        Regex("$REGION_PREFIX\\d{3}$PURPOSE_SYLLABLES$SERIAL"), // 경기123가4567
        Regex("$REGION_PREFIX\\d{2}$PURPOSE_SYLLABLES$SERIAL"), // 서울12가1234
        Regex("\\d{3}$PURPOSE_SYLLABLES$SERIAL"),               // 123가1234
        Regex("\\d{2}$PURPOSE_SYLLABLES$SERIAL"),               // 12가1234
        Regex("임$SERIAL"),                                     // 임1816
    )

    // 인접 매칭 검사용 앵커 패턴 (프레임마다 재컴파일하지 않도록 미리 생성)
    private val PLATE_PATTERNS_END_ANCHORED = PLATE_PATTERNS.map { Regex("${it.pattern}$") }
    private val PLATE_PATTERNS_START_ANCHORED = PLATE_PATTERNS.map { Regex("^${it.pattern}") }

    data class PlateCandidate(
        val plate: String,
        val boundingBox: Rect?,
        val patternIndex: Int,   // 낮을수록 더 구체적인 패턴
        val qualityRank: Int,    // 낮을수록 독립적으로 매칭된 신뢰도 높은 후보
        val correctionRank: Int, // 0: 원본 매칭, 1: OCR 보정 후 매칭
        val sourceOrder: Int,
    )

    /**
     * 프레임 필터링 결과.
     *
     * @property candidates 신뢰도 순으로 정렬된 번호판 후보 (쿨다운 번호판 제외).
     * @property matchedIgnored [findPlateCandidates]의 ignoredPlates 중 이번 프레임에서 실제로 보인 것.
     *   화면에 남아 있는 차량의 쿨다운 연장 판단에 사용한다.
     */
    data class FilterResult(
        val candidates: List<PlateCandidate>,
        val matchedIgnored: Set<String>,
    )

    /**
     * ML Kit [visionText]에서 번호판 후보를 모두 찾아 신뢰도 순으로 반환한다.
     *
     * @param ignoredPlates 쿨다운 등으로 이번 프레임에서 제외할 번호판 목록.
     * @param imageWidth 회전 보정된 프레임 너비. 중앙 근접도 계산에 사용 (없으면 텍스트 영역 중심 사용).
     * @param imageHeight 회전 보정된 프레임 높이.
     */
    fun findPlateCandidates(
        visionText: Text,
        ignoredPlates: Set<String> = emptySet(),
        imageWidth: Int? = null,
        imageHeight: Int? = null,
    ): FilterResult {
        val candidates = mutableListOf<PlateCandidate>()
        val seen = mutableSetOf<String>()

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

        // 2. 인접 블록 텍스트 병합 — 두 줄 번호판·근접 촬영 시 블록이 분리되는 케이스.
        //    ML Kit 블록 순서가 위→아래를 보장하지 않으므로 역순 병합도 시도한다.
        val blocks = visionText.textBlocks
        if (blocks.size >= 2) {
            for (i in 0 until blocks.size - 1) {
                val first = blocks[i].text.replace("\\s".toRegex(), "")
                val second = blocks[i + 1].text.replace("\\s".toRegex(), "")
                val mergedBox = mergeBounds(blocks[i].boundingBox, blocks[i + 1].boundingBox)
                addCandidates(
                    target = candidates,
                    seen = seen,
                    matches = extractPlateMatches(first + second),
                    boundingBox = mergedBox,
                    sourceOrder = i * 100 + 50,
                )
                addCandidates(
                    target = candidates,
                    seen = seen,
                    matches = extractPlateMatches(second + first),
                    boundingBox = mergedBox,
                    sourceOrder = i * 100 + 51,
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

        val matchedIgnored = mutableSetOf<String>()
        val activeCandidates = candidates.filter { candidate ->
            val ignored = ignoredPlates.firstOrNull { isSimilarPlate(it, candidate.plate) }
            if (ignored != null) matchedIgnored.add(ignored)
            ignored == null
        }

        val center = frameCenter(imageWidth, imageHeight, visionText)
        val sorted = activeCandidates.sortedWith(
            compareBy(
                { it.qualityRank },
                { centralityRank(it.boundingBox, center) },
                { it.correctionRank },
                { it.patternIndex },
                { it.sourceOrder },
            )
        )
        return FilterResult(candidates = sorted, matchedIgnored = matchedIgnored)
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
     * 텍스트에서 가장 신뢰도 높은 번호판을 추출한다.
     */
    fun extractPlate(rawText: String): String? {
        return extractPlateMatches(rawText).minWithOrNull(
            compareBy<PlateMatch>(
                { it.qualityRank },
                { it.correctionRank },
                { it.patternIndex },
                { it.matchStart },
            )
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
        val correctionRank: Int,
        val matchStart: Int,
    )

    /**
     * 원본 정규화 텍스트에서 먼저 매칭하고, OCR 문자 보정본은 폴백으로만 사용한다.
     * 보정을 무조건 적용하면 "A12가3456"의 A가 4로 바뀌어 "412가3456"이라는
     * 잘못된 번호판이 만들어지는 문제가 있다.
     */
    private fun extractPlateMatches(rawText: String): List<PlateMatch> {
        val plain = normalize(rawText, applyOcrCorrections = false)
        val corrected = normalize(rawText, applyOcrCorrections = true)

        val results = mutableListOf<PlateMatch>()
        val seen = mutableSetOf<String>()
        collectMatches(plain, correctionRank = 0, results = results, seen = seen)
        if (corrected != plain) {
            collectMatches(corrected, correctionRank = 1, results = results, seen = seen)
        }
        return results
    }

    private fun collectMatches(
        normalized: String,
        correctionRank: Int,
        results: MutableList<PlateMatch>,
        seen: MutableSet<String>,
    ) {
        if (normalized.isBlank()) return

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
                            correctionRank = correctionRank,
                            matchStart = match.range.first,
                        )
                    )
                    seen.add(match.value)
                }
                startIndex = match.range.last + 1
            }
        }
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
                    correctionRank = match.correctionRank,
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
        return PLATE_PATTERNS_END_ANCHORED.any { it.containsMatchIn(prefix) }
    }

    private fun hasAdjacentPlateAfter(text: String, end: Int): Boolean {
        val suffix = text.drop(end + 1)
        return PLATE_PATTERNS_START_ANCHORED.any { it.containsMatchIn(suffix) }
    }

    /** 중앙 근접도 기준점: 실제 프레임 중앙 우선, 없으면 인식된 텍스트 영역의 중심. */
    private fun frameCenter(imageWidth: Int?, imageHeight: Int?, visionText: Text): Pair<Int, Int>? {
        if (imageWidth != null && imageHeight != null && imageWidth > 0 && imageHeight > 0) {
            return imageWidth / 2 to imageHeight / 2
        }
        val frameBounds = mergeAllBounds(visionText.textBlocks.mapNotNull { it.boundingBox })
            ?: return null
        return frameBounds.centerX() to frameBounds.centerY()
    }

    private fun centralityRank(bounds: Rect?, center: Pair<Int, Int>?): Int {
        if (bounds == null || center == null) return Int.MAX_VALUE / 2
        val dx = bounds.centerX() - center.first
        val dy = bounds.centerY() - center.second
        return dx * dx + dy * dy
    }

    private fun normalize(text: String, applyOcrCorrections: Boolean): String {
        val mapped = if (applyOcrCorrections) {
            text.map { OCR_CHAR_REPLACEMENTS[it] ?: it }.joinToString("")
        } else {
            text
        }
        return mapped
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
