package com.sangyoon.vehiclenote.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sangyoon.vehiclenote.core.ui.theme.VnTypeBody
import com.sangyoon.vehiclenote.core.ui.theme.VnTypeBodySm
import com.sangyoon.vehiclenote.core.ui.theme.VnTypeMonoTime

private val LabelWidth = 84.dp

/**
 * 정보 카드 안의 `[label 84dp] [value]` 행.
 * 연락처·번호판 등 코드값은 mono = true 로 전달.
 */
@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    mono: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = VnTypeBodySm,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.width(LabelWidth),
        )
        Text(
            text = value,
            style = if (mono) VnTypeMonoTime else VnTypeBody,
            color = colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun InfoRowPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InfoRow(label = "차주명", value = "채상윤")
        InfoRow(label = "연락처", value = "010-4736-3559", mono = true)
        InfoRow(label = "소속", value = "개발팀")
        InfoRow(label = "차종", value = "벨로스터")
    }
}
