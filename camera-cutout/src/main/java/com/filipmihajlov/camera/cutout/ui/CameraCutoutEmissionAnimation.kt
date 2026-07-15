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
    val motion: CameraCutoutEmissionMotion = CameraCutoutEmissionMotion.TravelDown,
)

enum class CameraCutoutEmissionRepeatMode {
    Once,
    Infinite,
}

sealed interface CameraCutoutEmissionMotion {
    data object TravelDown : CameraCutoutEmissionMotion

    data class InstaxPrint(
        val initialWashAlpha: Float = 0.95f,
        val developDurationMillis: Int = 900,
        val startScale: Float = 0.92f,
        val endScale: Float = 1f,
    ) : CameraCutoutEmissionMotion
}

internal data class CameraCutoutEmissionAnimationState(
    val travelProgress: Float,
    val fadeProgress: Float,
    val revealProgress: Float = 1f,
    val developProgress: Float = 1f,
    val scale: Float = 1f,
)

@Composable
internal fun rememberEmissionAnimationState(
    emissionConfig: CameraCutoutEmissionConfig,
): CameraCutoutEmissionAnimationState {
    if (!emissionConfig.enabled) {
        return CameraCutoutEmissionAnimationState(
            travelProgress = 0f,
            fadeProgress = 1f,
            revealProgress = 0f,
            developProgress = 0f,
        )
    }

    return when (emissionConfig.repeatMode) {
        CameraCutoutEmissionRepeatMode.Once -> {
            val cycleProgress = remember { Animatable(0f) }

            LaunchedEffect(
                emissionConfig.enabled,
                emissionConfig.travelPx,
                emissionConfig.durationMillis,
                emissionConfig.fadeDurationMillis,
                emissionConfig.repeatMode,
                emissionConfig.motion,
            ) {
                cycleProgress.snapTo(0f)
                cycleProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = emissionConfig.durationMillisOrMinimum,
                    ),
                )
            }

            emissionConfig.animationStateAt(cycleProgress.value)
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

            emissionConfig.animationStateAt(cycleProgress)
        }
    }
}

private fun CameraCutoutEmissionConfig.animationStateAt(
    cycleProgress: Float,
): CameraCutoutEmissionAnimationState {
    return when (val motion = motion) {
        CameraCutoutEmissionMotion.TravelDown -> {
            CameraCutoutEmissionAnimationState(
                travelProgress = (cycleProgress / travelFraction).coerceIn(0f, 1f),
                fadeProgress = ((cycleProgress - travelFraction) /
                    fadeFraction).coerceIn(0f, 1f),
            )
        }

        is CameraCutoutEmissionMotion.InstaxPrint -> {
            val revealDurationMillis = revealDurationMillis(motion)
            val developDurationMillis = developDurationMillis(motion)
            val fadeDurationMillis = fadeDurationMillisOrMinimum
            val elapsedMillis = durationMillisOrMinimum * cycleProgress.coerceIn(0f, 1f)
            val revealProgress = (elapsedMillis / revealDurationMillis).coerceIn(0f, 1f)
            val developProgress = ((elapsedMillis - revealDurationMillis) /
                developDurationMillis).coerceIn(0f, 1f)
            val fadeProgress = ((elapsedMillis - revealDurationMillis - developDurationMillis) /
                fadeDurationMillis).coerceIn(0f, 1f)
            val scale = motion.startScale + ((motion.endScale - motion.startScale) * revealProgress)

            CameraCutoutEmissionAnimationState(
                travelProgress = revealProgress,
                fadeProgress = fadeProgress,
                revealProgress = revealProgress,
                developProgress = developProgress,
                scale = scale,
            )
        }
    }
}

private val CameraCutoutEmissionConfig.durationMillisOrMinimum: Int
    get() = durationMillis.coerceAtLeast(2)

internal val CameraCutoutEmissionConfig.fadeDurationMillisOrMinimum: Int
    get() = fadeDurationMillis.coerceIn(
        minimumValue = 1,
        maximumValue = (durationMillisOrMinimum - 1).coerceAtLeast(1),
    )

private val CameraCutoutEmissionConfig.travelDurationMillis: Int
    get() = (durationMillisOrMinimum - fadeDurationMillisOrMinimum).coerceAtLeast(1)

private val CameraCutoutEmissionConfig.travelFraction: Float
    get() = travelDurationMillis.toFloat() /
        (travelDurationMillis + fadeDurationMillisOrMinimum)

private val CameraCutoutEmissionConfig.fadeFraction: Float
    get() = 1f - travelFraction

private fun CameraCutoutEmissionConfig.developDurationMillis(
    motion: CameraCutoutEmissionMotion.InstaxPrint,
): Float {
    return motion.developDurationMillis.coerceIn(
        minimumValue = 1,
        maximumValue = (durationMillisOrMinimum - fadeDurationMillisOrMinimum - 1)
            .coerceAtLeast(1),
    ).toFloat()
}

private fun CameraCutoutEmissionConfig.revealDurationMillis(
    motion: CameraCutoutEmissionMotion.InstaxPrint,
): Float {
    return (durationMillisOrMinimum -
        fadeDurationMillisOrMinimum -
        developDurationMillis(motion)).coerceAtLeast(1f)
}
