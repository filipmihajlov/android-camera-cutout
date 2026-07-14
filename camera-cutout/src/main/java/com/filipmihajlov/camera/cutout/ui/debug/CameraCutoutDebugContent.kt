package com.filipmihajlov.camera.cutout.ui.debug

import android.graphics.Rect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.filipmihajlov.camera.cutout.domain.CameraCutoutInfo
import com.filipmihajlov.camera.cutout.domain.CameraCutoutSafeInsets
import com.filipmihajlov.camera.cutout.presentation.CameraCutoutUiState

@Composable
fun CameraCutoutDebugContent(
    uiState: CameraCutoutUiState,
    modifier: Modifier = Modifier,
) {
    val cutoutInfo = uiState.cutoutInfo

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Camera cutout",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = when {
                uiState.isWaitingForInsets -> "Waiting for window insets."
                cutoutInfo?.hasCutout == true -> "Display cutout detected."
                else -> "No display cutout detected."
            },
            style = MaterialTheme.typography.bodyLarge,
        )

        if (cutoutInfo != null) {
            Text(
                text = "Bounds: ${cutoutInfo.bounds.toReadableText()}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Shape: ${cutoutInfo.shapeDescription()}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Safe insets: ${cutoutInfo.safeInsets.toReadableText()}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun List<Rect>.toReadableText(): String {
    if (isEmpty()) return "none"

    return joinToString { rect ->
        "left=${rect.left}, top=${rect.top}, right=${rect.right}, bottom=${rect.bottom}"
    }
}

private fun CameraCutoutInfo.shapeDescription(): String {
    return when {
        hasExactPath -> "exact display cutout path"
        hasCutout -> "oval fallback inside bounds"
        else -> "none"
    }
}

private fun CameraCutoutSafeInsets.toReadableText(): String {
    return "top=$top, left=$left, right=$right, bottom=$bottom"
}

@Preview(showBackground = true)
@Composable
private fun CameraCutoutDebugContentPreview() {
    CameraCutoutDebugContent(
        uiState = CameraCutoutUiState(
            cutoutInfo = CameraCutoutInfo(
                bounds = listOf(Rect(480, 0, 600, 96)),
                path = null,
                safeInsets = CameraCutoutSafeInsets(
                    top = 96,
                    left = 0,
                    right = 0,
                    bottom = 0,
                ),
            ),
        ),
    )
}
