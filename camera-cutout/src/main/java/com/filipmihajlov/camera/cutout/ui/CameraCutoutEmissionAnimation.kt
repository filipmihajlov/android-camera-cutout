package com.filipmihajlov.camera.cutout.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

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

internal data class CameraCutoutEmissionAnimationState(
    val travelProgress: Float,
    val fadeProgress: Float,
)

@Composable
internal fun rememberEmissionAnimationState(
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
                        durationMillis = emissionConfig.fadeDurationMillisOrMinimum,
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

internal val CameraCutoutEmissionConfig.fadeDurationMillisOrMinimum: Int
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
