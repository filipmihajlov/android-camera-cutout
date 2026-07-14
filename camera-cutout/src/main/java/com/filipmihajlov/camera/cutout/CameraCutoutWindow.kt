package com.filipmihajlov.camera.cutout

import android.view.Window
import android.view.WindowManager

fun Window.enableCameraCutoutLayout() {
    attributes = attributes.apply {
        layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }
}
