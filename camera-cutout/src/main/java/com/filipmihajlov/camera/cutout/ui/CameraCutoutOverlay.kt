package com.filipmihajlov.camera.cutout.ui

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.filipmihajlov.camera.cutout.domain.CameraCutoutInfo

@Composable
fun CameraCutoutOverlay(
    cutoutInfo: CameraCutoutInfo,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 2.dp,
    emissionConfig: CameraCutoutEmissionConfig = CameraCutoutEmissionConfig(),
) {
    val emissionState = rememberEmissionAnimationState(emissionConfig)

    Canvas(modifier = modifier) {
        val strokeWidthPx = strokeWidth.toPx()

        if (emissionConfig.enabled) {
            drawCutoutEmission(
                cutoutInfo = cutoutInfo,
                color = color,
                travelProgress = emissionState.travelProgress,
                fadeProgress = emissionState.fadeProgress,
                travelPx = emissionConfig.travelPx,
            )
        }

        drawCutoutBorder(
            cutoutInfo = cutoutInfo,
            color = color,
            strokeWidthPx = strokeWidthPx,
        )
    }
}

private fun DrawScope.drawCutoutEmission(
    cutoutInfo: CameraCutoutInfo,
    color: Color,
    travelProgress: Float,
    fadeProgress: Float,
    travelPx: Float,
) {
    val translationY = travelPx * travelProgress
    val emissionColor = color.copy(alpha = 1f - fadeProgress)
    val cutoutPath = cutoutInfo.path

    if (cutoutPath != null && !cutoutPath.isEmpty) {
        val translatedPath = Path(cutoutPath).apply {
            transform(Matrix().apply { setTranslate(0f, translationY) })
        }

        drawPath(
            path = translatedPath.asComposePath(),
            color = emissionColor,
        )
        return
    }

    cutoutInfo.bounds.forEach { rect ->
        drawOval(
            color = emissionColor,
            topLeft = Offset(
                x = rect.left.toFloat(),
                y = rect.top + translationY,
            ),
            size = rect.size,
        )
    }
}

private fun DrawScope.drawCutoutBorder(
    cutoutInfo: CameraCutoutInfo,
    color: Color,
    strokeWidthPx: Float,
) {
    val cutoutPath = cutoutInfo.path

    if (cutoutPath != null && !cutoutPath.isEmpty) {
        drawPath(
            path = cutoutPath.asComposePath(),
            color = color,
            style = Stroke(width = strokeWidthPx),
        )
        return
    }

    val strokeInset = strokeWidthPx / 2f

    cutoutInfo.bounds.forEach { rect ->
        drawOval(
            color = color,
            topLeft = Offset(
                x = rect.left + strokeInset,
                y = rect.top + strokeInset,
            ),
            size = rect.innerStrokeSize(strokeWidthPx),
            style = Stroke(width = strokeWidthPx),
        )
    }
}

private val Rect.size: Size
    get() = Size(
        width = width().toFloat(),
        height = height().toFloat(),
    )

private fun Rect.innerStrokeSize(strokeWidthPx: Float): Size {
    return Size(
        width = (width() - strokeWidthPx).coerceAtLeast(0f),
        height = (height() - strokeWidthPx).coerceAtLeast(0f),
    )
}
