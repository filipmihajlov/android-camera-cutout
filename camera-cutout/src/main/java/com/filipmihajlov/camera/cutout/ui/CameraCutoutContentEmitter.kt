package com.filipmihajlov.camera.cutout.ui

import android.graphics.Rect
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import com.filipmihajlov.camera.cutout.domain.CameraCutoutInfo
import kotlin.math.roundToInt

@Composable
fun CameraCutoutContentEmitter(
    cutoutInfo: CameraCutoutInfo,
    modifier: Modifier = Modifier,
    emissionConfig: CameraCutoutEmissionConfig = CameraCutoutEmissionConfig(),
    contentAlignment: CameraCutoutEmissionAlignment = CameraCutoutEmissionAlignment.Center,
    content: @Composable () -> Unit,
) {
    if (!emissionConfig.enabled || !cutoutInfo.hasCutout) return

    val emissionState = rememberEmissionAnimationState(emissionConfig)
    val anchorBounds = cutoutInfo.primaryBounds ?: return
    val motion = emissionConfig.motion

    SubcomposeLayout(modifier = modifier) { constraints ->
        val looseConstraints = Constraints(
            minWidth = 0,
            maxWidth = constraints.maxWidth,
            minHeight = 0,
            maxHeight = constraints.maxHeight,
        )

        val placeables = subcompose("camera-cutout-emission-content") {
            Box(
                modifier = Modifier
                    .emissionLayer(
                        emissionState = emissionState,
                        motion = motion,
                    ),
            ) {
                content()
            }
        }
            .map { measurable -> measurable.measure(looseConstraints) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEach { placeable ->
                val anchorCenterX = anchorBounds.left + anchorBounds.width() / 2f
                val x = when (contentAlignment) {
                    CameraCutoutEmissionAlignment.Start -> anchorBounds.left.toFloat()
                    CameraCutoutEmissionAlignment.Center -> anchorCenterX - placeable.width / 2f
                    CameraCutoutEmissionAlignment.End -> anchorBounds.right - placeable.width.toFloat()
                }
                val y = when (motion) {
                    CameraCutoutEmissionMotion.TravelDown -> {
                        val startY = anchorBounds.bottom - placeable.height.toFloat()
                        startY + (emissionConfig.travelPx * emissionState.travelProgress)
                    }

                    is CameraCutoutEmissionMotion.InstaxPrint -> {
                        anchorBounds.bottom - (anchorBounds.height() * 0.12f) +
                            (emissionConfig.travelPx * 0.12f * emissionState.travelProgress)
                    }
                }

                placeable.place(
                    position = IntOffset(
                        x = x.roundToInt(),
                        y = y.roundToInt(),
                    ),
                )
            }
        }
    }
}

enum class CameraCutoutEmissionAlignment {
    Start,
    Center,
    End,
}

private val CameraCutoutInfo.primaryBounds: Rect?
    get() = bounds.minWithOrNull(
        compareBy<Rect> { it.top }
            .thenBy { it.left }
    )

private fun Modifier.emissionLayer(
    emissionState: CameraCutoutEmissionAnimationState,
    motion: CameraCutoutEmissionMotion,
): Modifier {
    return when (motion) {
        CameraCutoutEmissionMotion.TravelDown -> {
            graphicsLayer {
                alpha = 1f - emissionState.fadeProgress
            }
        }

        is CameraCutoutEmissionMotion.InstaxPrint -> {
            val revealProgress = emissionState.revealProgress.coerceIn(0.04f, 1f)
            val washAlpha = motion.initialWashAlpha.coerceIn(0f, 1f) *
                (1f - emissionState.developProgress)

            graphicsLayer {
                alpha = 1f - emissionState.fadeProgress
                scaleX = emissionState.scale
                scaleY = revealProgress * emissionState.scale
                transformOrigin = TransformOrigin(
                    pivotFractionX = 0.5f,
                    pivotFractionY = 0f,
                )
                shadowElevation = 12f * emissionState.revealProgress
            }.drawWithContent {
                drawContent()

                if (washAlpha > 0f) {
                    drawRect(
                        color = Color.White.copy(alpha = washAlpha),
                    )
                }
            }
        }
    }
}
