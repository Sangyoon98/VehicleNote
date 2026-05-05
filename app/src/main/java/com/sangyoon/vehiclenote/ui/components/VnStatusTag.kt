package com.sangyoon.vehiclenote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sangyoon.vehiclenote.domain.model.RecordType
import com.sangyoon.vehiclenote.ui.theme.VnEntryBg
import com.sangyoon.vehiclenote.ui.theme.VnEntryInk
import com.sangyoon.vehiclenote.ui.theme.VnExitBg
import com.sangyoon.vehiclenote.ui.theme.VnExitInk
import com.sangyoon.vehiclenote.ui.theme.VnTypeCaption

enum class TagKind { Entry, Exit, Neutral }

private data class TagColors(val bg: Color, val text: Color)

@Composable
private fun colorsFor(kind: TagKind): TagColors {
    val colorScheme = MaterialTheme.colorScheme
    return when (kind) {
        TagKind.Entry   -> TagColors(bg = VnEntryBg, text = VnEntryInk)
        TagKind.Exit    -> TagColors(bg = VnExitBg, text = VnExitInk)
        TagKind.Neutral -> TagColors(bg = colorScheme.surfaceVariant, text = colorScheme.onSurfaceVariant)
    }
}

/** RecordType → TagKind 변환 헬퍼 */
fun RecordType.toTagKind() = when (this) {
    RecordType.ENTRY -> TagKind.Entry
    RecordType.EXIT  -> TagKind.Exit
}

/**
 * 입차/출차/중립 상태 배지.
 * 리스트 아이템, 상세 화면 등 전체에서 동일 스타일을 보장.
 */
@Composable
fun VnStatusTag(
    kind: TagKind,
    text: String,
    modifier: Modifier = Modifier,
) {
    val c = colorsFor(kind)
    Text(
        text = text,
        style = VnTypeCaption,
        color = c.text,
        modifier = modifier
            .background(c.bg, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF9F5)
@Composable
private fun VnStatusTagPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        VnStatusTag(TagKind.Entry, "입차완료")
        VnStatusTag(TagKind.Exit, "출차완료")
        VnStatusTag(TagKind.Neutral, "대기중")
    }
}
