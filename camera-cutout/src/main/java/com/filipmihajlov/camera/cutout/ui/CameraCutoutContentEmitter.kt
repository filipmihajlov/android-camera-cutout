package com.filipmihajlov.camera.cutout.ui

import android.graphics.Rect
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    val translationY = emissionConfig.travelPx * emissionState.travelProgress
    val alpha = 1f - emissionState.fadeProgress

    SubcomposeLayout(modifier = modifier) { constraints ->
        val looseConstraints = Constraints(
            minWidth = 0,
            maxWidth = constraints.maxWidth,
            minHeight = 0,
            maxHeight = constraints.maxHeight,
        )
        val placeables = subcompose("camera-cutout-emission-content", content)
            .map { measurable -> measurable.measure(looseConstraints) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEach { placeable ->
                val anchorCenterX = anchorBounds.left + anchorBounds.width() / 2f
                val x = when (contentAlignment) {
                    CameraCutoutEmissionAlignment.Start -> anchorBounds.left.toFloat()
                    CameraCutoutEmissionAlignment.Center -> anchorCenterX - placeable.width / 2f
                    CameraCutoutEmissionAlignment.End -> anchorBounds.right - placeable.width.toFloat()
                }
                val startY = anchorBounds.bottom - placeable.height.toFloat()
                val y = startY + translationY

                placeable.placeWithLayer(
                    position = IntOffset(
                        x = x.roundToInt(),
                        y = y.roundToInt(),
                    ),
                ) {
                    this.alpha = alpha
                }
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
