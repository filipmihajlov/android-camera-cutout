package com.filipmihajlov.camera.cutout.data

import android.view.WindowInsets
import com.filipmihajlov.camera.cutout.domain.CameraCutoutInfo
import com.filipmihajlov.camera.cutout.domain.CameraCutoutReader
import com.filipmihajlov.camera.cutout.domain.CameraCutoutSafeInsets
import com.filipmihajlov.camera.cutout.domain.NoCameraCutoutInfo

class AndroidCameraCutoutReader : CameraCutoutReader {
    override fun read(windowInsets: WindowInsets?): CameraCutoutInfo? {
        if (windowInsets == null) return null

        val cutout = windowInsets.displayCutout ?: return NoCameraCutoutInfo

        return CameraCutoutInfo(
            bounds = cutout.boundingRects,
            path = cutout.cutoutPath,
            safeInsets = CameraCutoutSafeInsets(
                top = cutout.safeInsetTop,
                left = cutout.safeInsetLeft,
                right = cutout.safeInsetRight,
                bottom = cutout.safeInsetBottom,
            ),
        )
    }
}
