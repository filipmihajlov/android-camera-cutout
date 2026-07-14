package com.filipmihajlov.camera.cutout.ui

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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

data class CameraCutoutEmissionConfig(
    val enabled: Boolean = false,
    val travelPx: Float = 100f,
    val durationMillis: Int = 900,
    val fadeDurationMillis: Int = durationMillis / 3,
    val repeatMode: CameraCutoutEmissionRepeatMode = CameraCutoutEmissionRepeatMode.Infinite,
)

enum class CameraCutoutEmissionRepeatMode {
    Once,
    Infinite,
}

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

private data class CameraCutoutEmissionAnimationState(
    val travelProgress: Float,
    val fadeProgress: Float,
)

@Composable
private fun rememberEmissionAnimationState(
    emissionConfig: CameraCutoutEmissionConfig,
): CameraCutoutEmissionAnimationState {
    if (!emissionConfig.enabled) {
        return CameraCutoutEmissionAnimationState(
            travelProgress = 0f,
            fadeProgress = 1f,
        )
    }

    return when (emissionConfig.repeatMode) {
        CameraCutoutEmissionRepeatMode.Once -> {
            val travelProgress = remember { Animatable(0f) }
            val fadeProgress = remember { Animatable(0f) }

            LaunchedEffect(
                emissionConfig.enabled,
                emissionConfig.travelPx,
                emissionConfig.durationMillis,
                emissionConfig.fadeDurationMillis,
                emissionConfig.repeatMode,
            ) {
                travelProgress.snapTo(0f)
                fadeProgress.snapTo(0f)
                travelProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = emissionConfig.travelDurationMillis,
                    ),
                )
                fadeProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = emissionConfig.fadeDurationMillis,
                    ),
                )
            }

            CameraCutoutEmissionAnimationState(
                travelProgress = travelProgress.value,
                fadeProgress = fadeProgress.value,
            )
        }

        CameraCutoutEmissionRepeatMode.Infinite -> {
            val cycleProgress = rememberInfiniteTransition(label = "cutout-emission")
                .animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = emissionConfig.durationMillis),
                        repeatMode = RepeatMode.Restart,
                    ),
                    label = "cutout-emission-progress",
                ).value

            CameraCutoutEmissionAnimationState(
                travelProgress = (cycleProgress / emissionConfig.travelFraction)
                    .coerceIn(0f, 1f),
                fadeProgress = ((cycleProgress - emissionConfig.travelFraction) /
                    emissionConfig.fadeFraction).coerceIn(0f, 1f),
            )
        }
    }
}

private val CameraCutoutEmissionConfig.fadeDurationMillisOrMinimum: Int
    get() = fadeDurationMillis.coerceIn(
        minimumValue = 1,
        maximumValue = (durationMillis - 1).coerceAtLeast(1),
    )

private val CameraCutoutEmissionConfig.travelDurationMillis: Int
    get() = (durationMillis - fadeDurationMillisOrMinimum).coerceAtLeast(1)

private val CameraCutoutEmissionConfig.travelFraction: Float
    get() = travelDurationMillis.toFloat() /
        (travelDurationMillis + fadeDurationMillisOrMinimum)

private val CameraCutoutEmissionConfig.fadeFraction: Float
    get() = 1f - travelFraction

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
