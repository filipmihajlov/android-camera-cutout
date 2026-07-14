package com.filipmihajlov.camera.cutout.domain

import android.graphics.Path
import android.graphics.Rect

data class CameraCutoutInfo(
    val bounds: List<Rect>,
    val path: Path?,
    val safeInsets: CameraCutoutSafeInsets,
) {
    val hasCutout: Boolean = bounds.isNotEmpty()
    val hasExactPath: Boolean = path != null && !path.isEmpty
}

data class CameraCutoutSafeInsets(
    val top: Int,
    val left: Int,
    val right: Int,
    val bottom: Int,
)

val NoCameraCutoutInfo = CameraCutoutInfo(
    bounds = emptyList(),
    path = null,
    safeInsets = CameraCutoutSafeInsets(
        top = 0,
        left = 0,
        right = 0,
        bottom = 0,
    ),
)
